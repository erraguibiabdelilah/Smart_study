package com.example.smart_study.qsm.ui;

import android.net.Uri;
import android.os.*;
import android.view.*;
import android.widget.*;

import androidx.activity.result.*;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;

import com.example.smart_study.R;
import com.example.smart_study.outils.MyPdfExtractor;
import com.example.smart_study.qsm.service.QcmApiService;

public class QcmUploadFragment extends Fragment {

    private ProgressBar progress;
    private TextView status;
    private Uri pdfUri;
    private Handler ui;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater i,
                             @Nullable ViewGroup c,
                             @Nullable Bundle b) {
        return i.inflate(R.layout.fragment_qcm, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {

        ui = new Handler(Looper.getMainLooper());

        Button btn = v.findViewById(R.id.btn_upload_pdf);
        progress = v.findViewById(R.id.uploadProgress);
        status = v.findViewById(R.id.statusText);

        ActivityResultLauncher<String> picker =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {
                            if (uri != null) {
                                pdfUri = uri;
                                startQcm();
                            }
                        });

        btn.setOnClickListener(x -> picker.launch("application/pdf"));
    }

    private void startQcm() {

        progress.setVisibility(View.VISIBLE);
        progress.setProgress(10);
        status.setText("Extraction du PDF...");

        new Thread(() -> {

            String text = MyPdfExtractor.extractText(requireContext(), pdfUri);

            if (text == null || text.length() < 300) {
                toast("PDF trop court ❌");
                return;
            }

            text = text.substring(0, Math.min(text.length(), 12000));

            ui.post(() -> {
                progress.setProgress(40);
                status.setText("Génération du QCM...");
            });

            new QcmApiService().generateQcm(text,
                    new QcmApiService.QcmCallback() {

                        @Override
                        public void onSuccess(String qcmJson) {

                            ui.post(() -> {
                                progress.setProgress(100);
                                status.setText("QCM prêt ✔");

                                Bundle bundle = new Bundle();
                                bundle.putString("qcm_json", qcmJson);

                                QcmPlayFragment f = new QcmPlayFragment();
                                f.setArguments(bundle);

                                requireActivity()
                                        .getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.fragment_container, f)
                                        .addToBackStack(null)
                                        .commit();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            toast(error);
                        }
                    });

        }).start();
    }

    private void toast(String msg) {
        ui.post(() ->
                Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show()
        );
    }
}
