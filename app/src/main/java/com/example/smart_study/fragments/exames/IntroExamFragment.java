package com.example.smart_study.fragments.exames;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smart_study.R;
import com.example.smart_study.services.generateExams.ExamQuestionModel;

import java.io.Serializable;
import java.util.List;

public class IntroExamFragment extends Fragment {

    Button btnStartExam;
    Button btnCancel;

    List<ExamQuestionModel> exams;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_intoexam, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupListeners();
    }

    private void initializeViews(View view) {
        btnStartExam = view.findViewById(R.id.btnStartExam);
        btnCancel = view.findViewById(R.id.btnCancel);

        if (getArguments() != null) {
            exams = (List<ExamQuestionModel>) getArguments().getSerializable("exams");
        }

        // Disable start button if there are no exams
        if (exams == null || exams.isEmpty()) {
            btnStartExam.setEnabled(false);
            btnStartExam.setAlpha(0.5f); // Make it look disabled
            Toast.makeText(requireContext(), "Aucune question d'examen n'a pu être chargée.", Toast.LENGTH_LONG).show();
        }
    }

    private void setupListeners() {
        btnStartExam.setOnClickListener(v -> {
            navigateToExamFragment();
        });

        btnCancel.setOnClickListener(v -> {
            navigateTouploadCours();
        });
    }

    private void navigateToExamFragment() {
        ExamesFragment examFragment = new ExamesFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable("exams", (Serializable) exams);
        examFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, examFragment)
                .addToBackStack(null)
                .commit();
    }
    private void navigateTouploadCours() {
        UplodeFileExam uplodeFileExam = new UplodeFileExam();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, uplodeFileExam)
                .addToBackStack(null)
                .commit();
    }


}
