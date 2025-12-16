package com.example.smart_study.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smart_study.R;
import com.example.smart_study.fragments.exames.ExamesFragment;
import com.example.smart_study.fragments.exames.UplodeFileExam;
import com.example.smart_study.fragments.flashCards.UploadFileFragment;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout flashCard = view.findViewById(R.id.flashcard);
        LinearLayout cours = view.findViewById(R.id.cours);
        LinearLayout qcm = view.findViewById(R.id.qcm);
        LinearLayout resume = view.findViewById(R.id.resume);
        LinearLayout exam = view.findViewById(R.id.exam);

        flashCard.setOnClickListener(v -> {
            // Navigation vers UploadFileFragment
            UploadFileFragment uploadFileFragment = new UploadFileFragment();

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, uploadFileFragment)
                    .addToBackStack(null)
                    .commit();
        });

        cours.setOnClickListener(v -> {
            // Navigation vers cours
            CoursesFragment coursesFragment = new CoursesFragment();

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, coursesFragment)
                    .addToBackStack(null)
                    .commit();
        });

        qcm.setOnClickListener(v -> {
            // Navigation vers cours
            QcmFragment qcmFragment = new QcmFragment();

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, qcmFragment)
                    .addToBackStack(null)
                    .commit();
        });
        resume.setOnClickListener(v -> {
            // Navigation vers c
            ResumeFragment resumeFragment = new ResumeFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, resumeFragment)
                    .addToBackStack(null)
                    .commit();
        });
        exam.setOnClickListener(v -> {
            // Navigation vers c
            UplodeFileExam examesFragment = new UplodeFileExam();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, examesFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }
}
