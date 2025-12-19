package com.example.smart_study.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smart_study.R;
import com.example.smart_study.beans.UserStatisticsModel;
import com.example.smart_study.db.AppDatabase;

public class StatisticsFragment extends Fragment {

    private TextView tvLearningLevel, tvTotalExams, tvAverageScore, tvBestScore, tvAccuracy;
    private TextView tvCurrentStreak, tvLongestStreak, tvTotalStudyHours, tvStudyDaysWeek;
    private TextView tvTotalCourses, tvTotalResumes, tvTotalFlashcards;
    private LinearLayout emptyStateLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        initializeViews(view);
        setupBackButton(view);
        loadStatisticsData();

        return view;
    }

    private void initializeViews(View view) {
        // Learning Level
        tvLearningLevel = view.findViewById(R.id.tvLearningLevel);

        // Performance Statistics
        tvTotalExams = view.findViewById(R.id.tvTotalExams);
        tvAverageScore = view.findViewById(R.id.tvAverageScore);
        tvBestScore = view.findViewById(R.id.tvBestScore);
        tvAccuracy = view.findViewById(R.id.tvAccuracy);

        // Study Habits
        tvCurrentStreak = view.findViewById(R.id.tvCurrentStreak);
        tvLongestStreak = view.findViewById(R.id.tvLongestStreak);
        tvTotalStudyHours = view.findViewById(R.id.tvTotalStudyHours);
        tvStudyDaysWeek = view.findViewById(R.id.tvStudyDaysWeek);

        // Generated Content
        tvTotalCourses = view.findViewById(R.id.tvTotalCourses);
        tvTotalResumes = view.findViewById(R.id.tvTotalResumes);
        tvTotalFlashcards = view.findViewById(R.id.tvTotalFlashcards);

        // Empty State
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
    }

    private void setupBackButton(View view) {
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }
    }

    private void loadStatisticsData() {
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(requireContext());

                // Get user statistics
                UserStatisticsModel stats = db.userStatisticsDao().getUserStatistics();

                // Get exam statistics from exam history
                int totalExams = db.examHistoryDao().getExamCount();
                double averageScore = db.examHistoryDao().getAverageScore();

                // Get course history statistics
                int totalCourses = db.courseHistoryDao().getCoursesCountByType("course");
                int totalResumes = db.courseHistoryDao().getCoursesCountByType("resume");
                int totalFlashcards = db.courseHistoryDao().getCoursesCountByType("flashcard");

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (stats != null || totalExams > 0 || (totalCourses + totalResumes + totalFlashcards) > 0) {
                            displayStatistics(stats, totalExams, averageScore, totalCourses, totalResumes, totalFlashcards);
                            emptyStateLayout.setVisibility(View.GONE);
                        } else {
                            emptyStateLayout.setVisibility(View.VISIBLE);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("StatisticsFragment", "Erreur lors du chargement des statistiques: " + e.getMessage());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        emptyStateLayout.setVisibility(View.VISIBLE);
                    });
                }
            }
        }).start();
    }

    private void displayStatistics(UserStatisticsModel stats, int totalExams, double averageScore,
                                 int totalCourses, int totalResumes, int totalFlashcards) {
        // Learning Level
        if (stats != null) {
            tvLearningLevel.setText(stats.getLearningLevel() != null ? stats.getLearningLevel() : "Débutant");
        } else {
            tvLearningLevel.setText("Débutant");
        }

        // Performance Statistics
        tvTotalExams.setText(String.valueOf(totalExams));
        tvAverageScore.setText(String.format("%.1f%%", averageScore));

        if (stats != null) {
            tvBestScore.setText(stats.getBestExamScore() + "%");
            tvAccuracy.setText(String.format("%.1f%%", stats.getAccuracyPercentage()));

            // Study Habits
            tvCurrentStreak.setText(String.valueOf(stats.getCurrentStreak()));
            tvLongestStreak.setText(String.valueOf(stats.getLongestStreak()));
            tvTotalStudyHours.setText(stats.getStudyHoursFormatted());
            tvStudyDaysWeek.setText(String.valueOf(stats.getStudyDaysThisWeek()));
        } else {
            tvBestScore.setText("0%");
            tvAccuracy.setText("0%");
            tvCurrentStreak.setText("0");
            tvLongestStreak.setText("0");
            tvTotalStudyHours.setText("0min");
            tvStudyDaysWeek.setText("0");
        }

        // Generated Content
        tvTotalCourses.setText(String.valueOf(totalCourses));
        tvTotalResumes.setText(String.valueOf(totalResumes));
        tvTotalFlashcards.setText(String.valueOf(totalFlashcards));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStatisticsData();
    }
}
