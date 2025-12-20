package com.example.smart_study.fragments;

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
import com.example.smart_study.fragments.resumes.ProcessingFragmentResume;
import com.example.smart_study.outils.FileManager;

public class ResumeFragment extends Fragment {

    private static final int PICK_FILE_REQUEST = 1;

    private Button btnSelectFile;
    private TextView fileNameText;
    private TextView statusText;
    private TextView result;

    private Uri selectedFileUri;
    private String selectedFileName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resume, container, false);

        btnSelectFile = view.findViewById(R.id.btnSelectFile);
        fileNameText = view.findViewById(R.id.fileNameText);
        statusText = view.findViewById(R.id.statusText);
        result = view.findViewById(R.id.result);

        btnSelectFile.setOnClickListener(v -> openFilePicker());

        return view;
    }

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

                selectedFileName = FileManager.getFileName(selectedFileUri);
                fileNameText.setText(selectedFileName);

                if (selectedFileUri != null && selectedFileName != null) {
                    Toast.makeText(requireContext(),
                            "Fichier uploadé avec succès",
                            Toast.LENGTH_SHORT).show();

                    ProcessingFragmentResume processingFragment = new ProcessingFragmentResume();
                    Bundle bundle = new Bundle();
                    bundle.putString("pdfUri", String.valueOf(selectedFileUri));
                    bundle.putString("pdfFileName", selectedFileName);
                    processingFragment.setArguments(bundle);

                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, processingFragment)
                            .addToBackStack(null)
                            .commit();

                } else {
                    Toast.makeText(requireContext(), "Erreur lors de la sélection du fichier",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}