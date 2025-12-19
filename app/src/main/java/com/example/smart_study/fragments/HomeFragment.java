package com.example.smart_study.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smart_study.R;
import com.example.smart_study.db.AppDatabase;
import com.example.smart_study.fragments.exames.UplodeFileExam;
import com.example.smart_study.fragments.flashCards.UploadFileFragment;

import java.util.Calendar;
import java.util.Date;

public class HomeFragment extends Fragment {

    private ProgressBar progressCircle;
    private TextView progressText;
    private TextView weeklyStatsText;
    private TextView progressTitleText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialisation des vues de statistiques
        progressCircle = view.findViewById(R.id.progress_circle);
        // ID correct correspondant à fragment_home.xml
        progressText = view.findViewById(R.id.tv_progress_percentage); 
        weeklyStatsText = view.findViewById(R.id.tv_weekly_stats);
        progressTitleText = view.findViewById(R.id.tv_progress_title);

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
        
        // Charger les statistiques
        loadWeeklyStatistics();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadWeeklyStatistics();
    }

    private void loadWeeklyStatistics() {
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(requireContext());
                
                // Calculer la date d'il y a 7 jours
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_YEAR, -7);
                Date oneWeekAgoDate = cal.getTime();
                long oneWeekAgoMillis = oneWeekAgoDate.getTime();

                // Récupérer la moyenne
                double avgScore = db.examHistoryDao().getWeeklyAverageScore(oneWeekAgoMillis);
                
                // Récupérer le nombre d'examens réussis (>50%)
                int successfulExams = db.examHistoryDao().getWeeklySuccessfulExams(oneWeekAgoMillis);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Mettre à jour l'UI
                        if (progressCircle != null) {
                            progressCircle.setProgress((int) avgScore);
                        }
                        
                        if (progressText != null) {
                            progressText.setText((int) avgScore + "%");
                        }
                        
                        if (weeklyStatsText != null) {
                            weeklyStatsText.setText(successfulExams + " examens réussis\ncette semaine.");
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
