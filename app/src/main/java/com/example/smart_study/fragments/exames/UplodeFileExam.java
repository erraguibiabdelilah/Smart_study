package com.example.smart_study.fragments.exames;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smart_study.R;
import com.example.smart_study.fragments.flashCards.ProcessingFragment;
import com.example.smart_study.outils.FileManager;

public class UplodeFileExam extends Fragment {

    private static final int PICK_FILE_REQUEST = 1;

    private Button btnSelectFile;
    private TextView fileNameText;
    private TextView statusText;
    private TextView result;

    private Uri selectedFileUri;
    private String selectedFileName;
    private String pdfContent;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_examuploadfile, container, false);

        btnSelectFile = view.findViewById(R.id.btnSelect);
        fileNameText = view.findViewById(R.id.fileName);
        statusText = view.findViewById(R.id.status);
        result = view.findViewById(R.id.resultas);

        btnSelectFile.setOnClickListener(v -> openFilePicker());

        return view;
    }
    // Ouvrir le sélecteur de fichiers
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(
                Intent.createChooser(intent, "Sélectionner un fichier PDF"),
                PICK_FILE_REQUEST
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                selectedFileUri = data.getData();
                Log.d("URI", String.valueOf(selectedFileUri));

                // Récupérer le nom du fichier
                selectedFileName = FileManager.getFileName(selectedFileUri);
                fileNameText.setText(selectedFileName);

                // Extraire le texte
                // pdfContent = MyPdfExtractor.extractText(requireContext(), selectedFileUri);
                // Log.d("CONTENU", pdfContent);

                if (selectedFileUri != null && selectedFileName !=null) {
                    Toast.makeText(requireContext(),
                            "cour upload avec succès ",
                            Toast.LENGTH_SHORT).show();
                    //result.setText(pdfContent);

                    // Navigation vers proceesingFragement
                    ProcessingFragmentExam processingFragment = new ProcessingFragmentExam();
                    Bundle bundle = new Bundle();
                    bundle.putString("pdfUri", String.valueOf(selectedFileUri));
                    processingFragment.setArguments(bundle);

                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, processingFragment)
                            .addToBackStack(null)
                            .commit();

                } else {
                    Toast.makeText(requireContext(), "Erreur extraction PDF",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Réinitialiser le formulaire
    private void resetForm() {
        fileNameText.setText("Aucun fichier sélectionné");
        statusText.setVisibility(View.GONE);
        selectedFileUri = null;
        selectedFileName = null;
        pdfContent = null;
    }
}
