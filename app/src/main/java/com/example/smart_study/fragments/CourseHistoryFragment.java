package com.example.smart_study.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smart_study.R;
import com.example.smart_study.adapters.CourseHistoryAdapter;
import com.example.smart_study.beans.CourseHistory;
import com.example.smart_study.db.AppDatabase;

import java.util.List;

public class CourseHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private View emptyView;
    private CourseHistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_history, container, false);

        recyclerView = view.findViewById(R.id.courseHistoryRecyclerView);
        emptyView = view.findViewById(R.id.emptyStateLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CourseHistoryAdapter(getContext());
        recyclerView.setAdapter(adapter);

        loadCoursesFromDatabase();

        return view;
    }

    private void loadCoursesFromDatabase() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<CourseHistory> courses = db.courseHistoryDao().getAllCourses();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (courses == null || courses.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                        adapter.setCourses(courses);
                    }
                });
            }
        }).start();
    }
}
