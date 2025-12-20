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

public class LibraryFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.liberary, container, false);

        LinearLayout examLayout = view.findViewById(R.id.exam);
        LinearLayout coursLayout = view.findViewById(R.id.cours);
        View btnBack = view.findViewById(R.id.btnBack);

        if (examLayout != null) {
            examLayout.setOnClickListener(v -> {
                ExamHistoryFragment examHistory = new ExamHistoryFragment();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, examHistory)
                        .addToBackStack(null)
                        .commit();
            });
        }

        if (coursLayout != null) {
            coursLayout.setOnClickListener(v -> {
                CourseHistoryFragment courseHistoryFragment = new CourseHistoryFragment();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, courseHistoryFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }

        // Navigation pour "Résumés de Cours"
        if (resumeLayout != null) {
            resumeLayout.setOnClickListener(v -> {
                ResumeHistoryFragment resumeHistory = new ResumeHistoryFragment();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, resumeHistory)
                        .addToBackStack(null)
                        .commit();
            });
        }
        
        // Bouton retour
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        return view;
    }
}
