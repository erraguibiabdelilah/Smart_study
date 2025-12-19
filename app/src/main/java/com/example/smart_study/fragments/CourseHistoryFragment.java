package com.example.smart_study.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smart_study.R;
import com.example.smart_study.beans.CourseHistoryModel;
import com.example.smart_study.db.AppDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CourseHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;
    private CourseHistoryAdapter adapter;
    private String currentFilter = "all"; // "all", "course", "resume", "flashcard", "quiz"

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_history, container, false);

        initializeViews(view);
        setupBackButton(view);
        setupFilterButtons(view);
        setupRecyclerView(view);
        loadCourseHistoryData();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.courseHistoryRecyclerView);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        progressBar = view.findViewById(R.id.progressBar);
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

    private void setupFilterButtons(View view) {
        Button btnFilterAll = view.findViewById(R.id.btnFilterAll);
        Button btnFilterCourses = view.findViewById(R.id.btnFilterCourses);
        Button btnFilterResumes = view.findViewById(R.id.btnFilterResumes);
        Button btnFilterFlashcards = view.findViewById(R.id.btnFilterFlashcards);
        Button btnFilterQuizzes = view.findViewById(R.id.btnFilterQuizzes);

        // Set initial state
        updateButtonState(btnFilterAll, true);
        updateButtonState(btnFilterCourses, false);
        updateButtonState(btnFilterResumes, false);
        updateButtonState(btnFilterFlashcards, false);
        updateButtonState(btnFilterQuizzes, false);

        btnFilterAll.setOnClickListener(v -> {
            currentFilter = "all";
            updateFilterButtons(btnFilterAll, btnFilterCourses, btnFilterResumes, btnFilterFlashcards, btnFilterQuizzes);
            loadCourseHistoryData();
        });

        btnFilterCourses.setOnClickListener(v -> {
            currentFilter = "course";
            updateFilterButtons(btnFilterCourses, btnFilterAll, btnFilterResumes, btnFilterFlashcards, btnFilterQuizzes);
            loadCourseHistoryData();
        });

        btnFilterResumes.setOnClickListener(v -> {
            currentFilter = "resume";
            updateFilterButtons(btnFilterResumes, btnFilterAll, btnFilterCourses, btnFilterFlashcards, btnFilterQuizzes);
            loadCourseHistoryData();
        });

        btnFilterFlashcards.setOnClickListener(v -> {
            currentFilter = "flashcard";
            updateFilterButtons(btnFilterFlashcards, btnFilterAll, btnFilterCourses, btnFilterResumes, btnFilterQuizzes);
            loadCourseHistoryData();
        });

        btnFilterQuizzes.setOnClickListener(v -> {
            currentFilter = "quiz";
            updateFilterButtons(btnFilterQuizzes, btnFilterAll, btnFilterCourses, btnFilterResumes, btnFilterFlashcards);
            loadCourseHistoryData();
        });
    }

    private void updateFilterButtons(Button activeButton, Button... inactiveButtons) {
        updateButtonState(activeButton, true);
        for (Button button : inactiveButtons) {
            updateButtonState(button, false);
        }
    }

    private void updateButtonState(Button button, boolean isActive) {
        if (isActive) {
            button.setBackgroundResource(R.drawable.button_start_exam);
            button.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            button.setBackgroundResource(R.drawable.button_secondary_exam);
            button.setTextColor(getResources().getColor(R.color.primary_dark));
        }
    }

    private void setupRecyclerView(View view) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CourseHistoryAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void loadCourseHistoryData() {
        progressBar.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(requireContext());
                List<CourseHistoryModel> courses;

                switch (currentFilter) {
                    case "course":
                        courses = db.courseHistoryDao().getCoursesByType("course");
                        break;
                    case "resume":
                        courses = db.courseHistoryDao().getCoursesByType("resume");
                        break;
                    case "flashcard":
                        courses = db.courseHistoryDao().getCoursesByType("flashcard");
                        break;
                    case "quiz":
                        courses = db.courseHistoryDao().getCoursesByType("quiz");
                        break;
                    default:
                        courses = db.courseHistoryDao().getAllCourseHistory();
                        break;
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        if (courses == null || courses.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            emptyStateLayout.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyStateLayout.setVisibility(View.GONE);
                            adapter.updateData(courses);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("CourseHistoryFragment", "Erreur lors du chargement de l'historique: " + e.getMessage());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        emptyStateLayout.setVisibility(View.VISIBLE);
                    });
                }
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCourseHistoryData();
    }

    private class CourseHistoryAdapter extends RecyclerView.Adapter<CourseHistoryViewHolder> {

        private List<CourseHistoryModel> courseList = new ArrayList<>();
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());

        public void updateData(List<CourseHistoryModel> newCourses) {
            this.courseList = newCourses;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CourseHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_course_history, parent, false);
            return new CourseHistoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CourseHistoryViewHolder holder, int position) {
            CourseHistoryModel course = courseList.get(position);

            // Set basic information
            holder.tvGenerationType.setText(course.getGenerationTypeLabel());
            holder.tvCourseTitle.setText(course.getTitle() != null ? course.getTitle() : "Sans titre");
            holder.tvCourseSubject.setText(course.getSubject() != null ? course.getSubject() : "Sujet non spécifié");

            // Set date
            if (course.getGeneratedDate() != null) {
                holder.tvGenerationDate.setText(dateFormat.format(course.getGeneratedDate()));
            } else {
                holder.tvGenerationDate.setText("-");
            }

            // Set content preview
            holder.tvContentPreview.setText(course.getPreviewText());

            // Set status badge
            holder.tvStatusBadge.setText(course.getStatusLabel());
            if ("success".equals(course.getStatus())) {
                holder.tvStatusBadge.setBackgroundResource(R.drawable.rounded_bg_success);
            } else {
                holder.tvStatusBadge.setBackgroundResource(R.drawable.button_cancel_exam);
            }

            // Set generation time
            holder.tvGenerationTime.setText(course.getGenerationTimeFormatted());

            // Set type-specific information
            setupTypeSpecificInfo(holder, course);

            // Set click listener
            holder.itemView.setOnClickListener(v -> {
                // TODO: Open detail view for the course
                // CourseDetailFragment detailFragment = CourseDetailFragment.newInstance(course);
                // navigate to detail fragment
            });
        }

        private void setupTypeSpecificInfo(CourseHistoryViewHolder holder, CourseHistoryModel course) {
            // Hide all type-specific views first
            holder.tvSourceSize.setVisibility(View.GONE);
            holder.tvQuizCount.setVisibility(View.GONE);
            holder.tvFlashcardCount.setVisibility(View.GONE);

            switch (course.getGenerationType()) {
                case "course":
                case "resume":
                    holder.tvSourceSize.setVisibility(View.VISIBLE);
                    holder.tvSourceSize.setText(course.getSourceSize() + " caractères");
                    break;
                case "quiz":
                    if (course.getQuizQuestionsCount() > 0) {
                        holder.tvQuizCount.setVisibility(View.VISIBLE);
                        holder.tvQuizCount.setText(course.getQuizQuestionsCount() + " questions");
                    }
                    break;
                case "flashcard":
                    if (course.getFlashcards() != null && !course.getFlashcards().isEmpty()) {
                        holder.tvFlashcardCount.setVisibility(View.VISIBLE);
                        holder.tvFlashcardCount.setText(course.getFlashcards().size() + " cartes");
                    }
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return courseList.size();
        }
    }

    private static class CourseHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvGenerationType, tvGenerationDate, tvCourseTitle, tvCourseSubject, tvContentPreview;
        TextView tvStatusBadge, tvGenerationTime, tvSourceSize, tvQuizCount, tvFlashcardCount;

        public CourseHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGenerationType = itemView.findViewById(R.id.tvGenerationType);
            tvGenerationDate = itemView.findViewById(R.id.tvGenerationDate);
            tvCourseTitle = itemView.findViewById(R.id.tvCourseTitle);
            tvCourseSubject = itemView.findViewById(R.id.tvCourseSubject);
            tvContentPreview = itemView.findViewById(R.id.tvContentPreview);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvGenerationTime = itemView.findViewById(R.id.tvGenerationTime);
            tvSourceSize = itemView.findViewById(R.id.tvSourceSize);
            tvQuizCount = itemView.findViewById(R.id.tvQuizCount);
            tvFlashcardCount = itemView.findViewById(R.id.tvFlashcardCount);
        }
    }
}
