package com.example.smart_study.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smart_study.R;


public class CoursesFragment extends Fragment {

    private EditText etCourseName;
    private SeekBar seekbarLevel;
    private EditText etObjectives;
    private Button btnGenerate;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_courses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etCourseName = view.findViewById(R.id.et_course_name);
        seekbarLevel = view.findViewById(R.id.seekbar_level);
        etObjectives = view.findViewById(R.id.et_objectives);
        btnGenerate = view.findViewById(R.id.btn_generate_course);

        btnGenerate.setOnClickListener(v -> generateCourse());
    }

    private void generateCourse() {
        String courseName = etCourseName.getText().toString().trim();
        String objectives = etObjectives.getText().toString().trim();
        int progress = seekbarLevel.getProgress();


        if (courseName.isEmpty()) {
            etCourseName.setError("Le nom du cours est requis");
            return;
        }

        if (objectives.isEmpty()) {
            etObjectives.setError("Les objectifs sont requis");
            return;
        }

        btnGenerate.setEnabled(false);
        btnGenerate.setText("Génération en cours...");

    }



}
