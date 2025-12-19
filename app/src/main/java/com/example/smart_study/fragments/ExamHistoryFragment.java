package com.example.smart_study.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smart_study.R;
import com.example.smart_study.beans.ExamHistoryModel;
import com.example.smart_study.db.AppDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExamHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private View emptyView;
    private ExamHistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exam_history, container, false);

        recyclerView = view.findViewById(R.id.historyRecyclerView);
        emptyView = view.findViewById(R.id.emptyStateLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ExamHistoryAdapter();
        recyclerView.setAdapter(adapter);

        loadDataFromDatabase();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDataFromDatabase();
    }
    private void loadDataFromDatabase() {
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(requireContext());
                List<ExamHistoryModel> exams = db.examHistoryDao().getAllExams();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (exams == null || exams.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                            adapter.updateData(exams);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("LibraryFragment", "Erreur DB: " + e.getMessage());
            }
        }).start();
    }

    private class ExamHistoryAdapter extends RecyclerView.Adapter<ExamHistoryViewHolder> {

        private List<ExamHistoryModel> examList = new ArrayList<>();
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());

        public void updateData(List<ExamHistoryModel> newExams) {
            this.examList = newExams;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ExamHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_exam_history, parent, false);
            return new ExamHistoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ExamHistoryViewHolder holder, int position) {
            ExamHistoryModel exam = examList.get(position);

            holder.tvExamTitle.setText(exam.getExamTitle());

            if (exam.getCompletedDate() != null) {
                holder.tvExamDate.setText(dateFormat.format(exam.getCompletedDate()));
            } else {
                holder.tvExamDate.setText("-");
            }

            holder.tvCorrectAnswers.setText(exam.getCorrectAnswers() + "/" + exam.getTotalQuestions());
            holder.tvScorePercentage.setText((int) exam.getScorePercentage() + "%");
            holder.scoreProgressBar.setProgress((int) exam.getScorePercentage());

            if (exam.getTimeUsed() != null) {
                holder.tvTimeUsed.setText(exam.getTimeUsed());
            } else {
                holder.tvTimeUsed.setText("-");
            }

            holder.itemView.setOnClickListener(v -> {
                ExamDetailFragment detailFragment = ExamDetailFragment.newInstance(exam);

                // IMPORTANT: Utilisation du bon ID de conteneur
                // Vérifiez que R.id.fragment_container est bien l'ID du FrameLayout dans MainActivity
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, detailFragment)
                            .addToBackStack(null) // Permet de revenir en arrière avec le bouton retour
                            .commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return examList.size();
        }
    }

    private static class ExamHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvExamTitle, tvExamDate, tvCorrectAnswers, tvScorePercentage, tvTimeUsed;
        ProgressBar scoreProgressBar;

        public ExamHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExamTitle = itemView.findViewById(R.id.examTitle);
            tvExamDate = itemView.findViewById(R.id.examDate);
            tvCorrectAnswers = itemView.findViewById(R.id.correctAnswers);
            tvScorePercentage = itemView.findViewById(R.id.scorePercentage);
            tvTimeUsed = itemView.findViewById(R.id.timeUsed);
            scoreProgressBar = itemView.findViewById(R.id.scoreProgressBar);
        }
    }
}
