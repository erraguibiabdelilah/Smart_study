package com.example.smart_study.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smart_study.R;
import com.example.smart_study.beans.ResumeModel;
import com.example.smart_study.db.AppDatabase;
import com.example.smart_study.fragments.resumes.ResumeDisplayFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ResumeHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private View emptyView;
    private ResumeHistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resume_history, container, false);

        recyclerView = view.findViewById(R.id.historyRecyclerView);
        emptyView = view.findViewById(R.id.emptyStateLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ResumeHistoryAdapter();
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
                List<ResumeModel> resumes = db.resumeDao().getAllResumes();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (resumes == null || resumes.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                            adapter.updateData(resumes);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("ResumeHistoryFragment", "Erreur DB: " + e.getMessage());
            }
        }).start();
    }

    private class ResumeHistoryAdapter extends RecyclerView.Adapter<ResumeHistoryViewHolder> {

        private List<ResumeModel> resumeList = new ArrayList<>();
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());

        public void updateData(List<ResumeModel> newResumes) {
            this.resumeList = newResumes;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ResumeHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_resume_history, parent, false);
            return new ResumeHistoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ResumeHistoryViewHolder holder, int position) {
            ResumeModel resume = resumeList.get(position);

            holder.tvResumeTitle.setText(resume.getTitle());

            if (resume.getCreatedDate() != null) {
                holder.tvResumeDate.setText(dateFormat.format(resume.getCreatedDate()));
            } else {
                holder.tvResumeDate.setText("-");
            }

            if (resume.getSourceFileName() != null && !resume.getSourceFileName().isEmpty()) {
                holder.tvSourceFile.setText(resume.getSourceFileName());
                holder.tvSourceFile.setVisibility(View.VISIBLE);
            } else {
                holder.tvSourceFile.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                ResumeDisplayFragment resumeDisplayFragment = new ResumeDisplayFragment();
                Bundle bundle = new Bundle();
                bundle.putString("resume", resume.getContent());
                resumeDisplayFragment.setArguments(bundle);

                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, resumeDisplayFragment)
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return resumeList.size();
        }
    }

    private static class ResumeHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvResumeTitle, tvResumeDate, tvSourceFile;

        public ResumeHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvResumeTitle = itemView.findViewById(R.id.resumeTitle);
            tvResumeDate = itemView.findViewById(R.id.resumeDate);
            tvSourceFile = itemView.findViewById(R.id.sourceFile);
        }
    }
}

