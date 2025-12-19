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
import com.example.smart_study.fragments.StatisticsFragment;
import com.example.smart_study.fragments.CourseHistoryFragment;

public class LibraryFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.liberary, container, false);
        
        // Initialiser les clics sur les éléments
        LinearLayout statisticsLayout = view.findViewById(R.id.statistics);
        LinearLayout examLayout = view.findViewById(R.id.exam);
        LinearLayout coursLayout = view.findViewById(R.id.cours);
        LinearLayout resumeLayout = view.findViewById(R.id.resume);
        View btnBack = view.findViewById(R.id.btnBack);

        // Navigation pour "Statistiques d'Apprentissage"
        if (statisticsLayout != null) {
            statisticsLayout.setOnClickListener(v -> {
                StatisticsFragment statisticsFragment = new StatisticsFragment();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, statisticsFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }

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

        // Navigation pour "Historique des Cours"
        if (coursLayout != null) {
            coursLayout.setOnClickListener(v -> {
                CourseHistoryFragment courseHistory = new CourseHistoryFragment();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, courseHistory)
                        .addToBackStack(null)
                        .commit();
            });
        }

        // Navigation pour "Résumés de Cours" (redirige vers l'historique des cours avec filtre résumé)
        if (resumeLayout != null) {
            resumeLayout.setOnClickListener(v -> {
                CourseHistoryFragment courseHistory = new CourseHistoryFragment();
                // TODO: Passer un argument pour filtrer sur les résumés
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, courseHistory)
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
