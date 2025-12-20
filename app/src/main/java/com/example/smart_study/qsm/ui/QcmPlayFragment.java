package com.example.smart_study.qsm.ui;

import android.os.*;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;

import com.example.smart_study.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class QcmPlayFragment extends Fragment {

    private TextView tvQuestion;
    private RadioGroup radioGroup;
    private Button btnNext;

    private String[] questions;
    private String[][] options;
    private int[] answers;

    private int index = 0;
    private int score = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater i,
                             @Nullable ViewGroup c,
                             @Nullable Bundle b) {
        return i.inflate(R.layout.fragment_qcm_play, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {

        tvQuestion = v.findViewById(R.id.tv_question);
        radioGroup = v.findViewById(R.id.radio_group);
        btnNext = v.findViewById(R.id.btn_next);

        String json = getArguments() != null
                ? getArguments().getString("qcm_json")
                : null;

        if (json == null) {
            showError();
            return;
        }

        if (!parseQcm(json)) {
            showError();
            return;
        }

        loadQuestion();

        btnNext.setOnClickListener(x -> checkAnswer());
    }

    private boolean parseQcm(String json) {
        try {
            JSONObject root = new JSONObject(json);

            JSONArray q = root.getJSONArray("questions");
            JSONArray o = root.getJSONArray("options");
            JSONArray a = root.getJSONArray("answers");

            if (q.length() != 20) return false;

            questions = new String[q.length()];
            options = new String[q.length()][4];
            answers = new int[q.length()];

            for (int i = 0; i < q.length(); i++) {
                questions[i] = q.getString(i);
                answers[i] = a.getInt(i);

                JSONArray opt = o.getJSONArray(i);
                for (int j = 0; j < 4; j++) {
                    options[i][j] = opt.getString(j);
                }
            }
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    private void loadQuestion() {

        if (index >= questions.length) {
            showResult();
            return;
        }

        tvQuestion.setText("Q" + (index + 1) + ". " + questions[index]);
        radioGroup.removeAllViews();

        for (int i = 0; i < 4; i++) {
            RadioButton rb = new RadioButton(getContext());
            rb.setText(options[index][i]);
            rb.setId(i);
            radioGroup.addView(rb);
        }

        btnNext.setText(index == questions.length - 1 ? "Terminer" : "Suivant");
    }

    private void checkAnswer() {

        int selected = radioGroup.getCheckedRadioButtonId();

        if (selected == -1) {
            Toast.makeText(getContext(),
                    "Choisissez une réponse", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selected == answers[index]) {
            score++;
            Toast.makeText(getContext(), "Bonne réponse ✅", Toast.LENGTH_SHORT).show();
        } else {
            // Affiche la vraie réponse si l'utilisateur se trompe
            String correctAnswer = options[index][answers[index]];
            Toast.makeText(getContext(),
                    "Mauvaise réponse ❌\nRéponse correcte : " + correctAnswer,
                    Toast.LENGTH_LONG).show();
        }

        index++;
        loadQuestion();
    }

    private void showResult() {
        tvQuestion.setText("Examen terminé 🎉\nScore : " + score + "/20");
        radioGroup.setVisibility(View.GONE);
        btnNext.setText("Retour");
        btnNext.setOnClickListener(x -> requireActivity().onBackPressed());
    }

    private void showError() {
        tvQuestion.setText("QCM invalide ❌");
        btnNext.setText("Retour");
        btnNext.setOnClickListener(x -> requireActivity().onBackPressed());
    }
}
