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
import com.example.smart_study.qsm.ui.QcmUploadFragment; // <- Nouveau import

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
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressCircle = view.findViewById(R.id.progress_circle);
        progressText = view.findViewById(R.id.tv_progress_percentage);
        weeklyStatsText = view.findViewById(R.id.tv_weekly_stats);
        progressTitleText = view.findViewById(R.id.tv_progress_title);

        LinearLayout flashCard = view.findViewById(R.id.flashcard);
        LinearLayout cours = view.findViewById(R.id.cours);
        LinearLayout qcm = view.findViewById(R.id.qcm);
        LinearLayout resume = view.findViewById(R.id.resume);
        LinearLayout exam = view.findViewById(R.id.exam);

        flashCard.setOnClickListener(v -> {
            UploadFileFragment uploadFileFragment = new UploadFileFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, uploadFileFragment)
                    .addToBackStack(null)
                    .commit();
        });

        cours.setOnClickListener(v -> {
            CoursesFragment coursesFragment = new CoursesFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, coursesFragment)
                    .addToBackStack(null)
                    .commit();
        });

        qcm.setOnClickListener(v -> {
            QcmUploadFragment qcmUploadFragment = new QcmUploadFragment(); // <- Changement ici
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, qcmUploadFragment)
                    .addToBackStack(null)
                    .commit();
        });

        resume.setOnClickListener(v -> {
            ResumeFragment resumeFragment = new ResumeFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, resumeFragment)
                    .addToBackStack(null)
                    .commit();
        });

        exam.setOnClickListener(v -> {
            UplodeFileExam examesFragment = new UplodeFileExam();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, examesFragment)
                    .addToBackStack(null)
                    .commit();
        });

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

                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_YEAR, -7);
                Date oneWeekAgoDate = cal.getTime();
                long oneWeekAgoMillis = oneWeekAgoDate.getTime();

                double avgScore = db.examHistoryDao().getWeeklyAverageScore(oneWeekAgoMillis);
                int successfulExams = db.examHistoryDao().getWeeklySuccessfulExams(oneWeekAgoMillis);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (progressCircle != null) progressCircle.setProgress((int) avgScore);
                        if (progressText != null) progressText.setText((int) avgScore + "%");
                        if (weeklyStatsText != null)
                            weeklyStatsText.setText(successfulExams + " examens réussis\ncette semaine.");
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
