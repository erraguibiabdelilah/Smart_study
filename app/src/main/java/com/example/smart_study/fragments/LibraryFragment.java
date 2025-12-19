package com.example.smart_study.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.view.View.OnClickListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smart_study.R;
import com.example.smart_study.fragments.ExamHistoryFragment;

public class LibraryFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.liberary, container, false);
        
        // Initialiser les clics sur les éléments
        LinearLayout examLayout = view.findViewById(R.id.exam);
        LinearLayout coursLayout = view.findViewById(R.id.cours);
        LinearLayout resumeLayout = view.findViewById(R.id.resume);
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
        
        // Navigation pour "Historique des Cours" (à implémenter si la page existe)
        /*
        if (coursLayout != null) {
            coursLayout.setOnClickListener(v -> {
                // Fragment coursHistory = new CoursHistoryFragment();
                // navigateTo(coursHistory);
            });
        }
        */

        // Navigation pour "Résumés de Cours" (à implémenter si la page existe)
        /*
        if (resumeLayout != null) {
            resumeLayout.setOnClickListener(v -> {
                // Fragment resumeHistory = new ResumeHistoryFragment();
                // navigateTo(resumeHistory);
            });
        }
        */
        
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
