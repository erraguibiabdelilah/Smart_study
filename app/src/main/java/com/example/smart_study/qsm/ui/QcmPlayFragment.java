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
    private LinearLayout optionsContainer; // <-- remplace le RadioGroup
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
        optionsContainer = v.findViewById(R.id.radio_group); // on garde le même id pour ne rien casser
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
        optionsContainer.removeAllViews();

        for (int i = 0; i < 4; i++) {
            TextView tv = new TextView(getContext());
            tv.setText(options[index][i]);
            tv.setTextSize(16f);
            tv.setPadding(24, 24, 24, 24);
            tv.setBackgroundResource(R.drawable.option_background); // fond neutre arrondi
            tv.setTextColor(getResources().getColor(R.color.black));
            tv.setClickable(true);

            final int selected = i;
            tv.setOnClickListener(v -> checkAnswer(selected, tv));

            optionsContainer.addView(tv);
        }

        btnNext.setText(index == questions.length - 1 ? "Terminer" : "Suivant");
        btnNext.setEnabled(false); // désactive tant que l'utilisateur n'a pas cliqué
    }

    private void checkAnswer(int selected, TextView clickedView) {

        // désactive toutes les options
        for (int i = 0; i < optionsContainer.getChildCount(); i++) {
            optionsContainer.getChildAt(i).setClickable(false);
        }

        if (selected == answers[index]) {
            score++;
            clickedView.setBackgroundColor(getResources().getColor(R.color.green));
        } else {
            clickedView.setBackgroundColor(getResources().getColor(R.color.red));
            // mettre la vraie réponse en vert
            TextView correctView = (TextView) optionsContainer.getChildAt(answers[index]);
            correctView.setBackgroundColor(getResources().getColor(R.color.green));
        }

        btnNext.setEnabled(true);
        btnNext.setOnClickListener(v -> {
            index++;
            loadQuestion();
        });
    }

    private void showResult() {
        tvQuestion.setText("Examen terminé 🎉\nScore : " + score + "/20");
        optionsContainer.setVisibility(View.GONE);
        btnNext.setText("Retour");
        btnNext.setEnabled(true);
        btnNext.setOnClickListener(x -> requireActivity().onBackPressed());
    }

    private void showError() {
        tvQuestion.setText("QCM invalide ❌");
        btnNext.setText("Retour");
        btnNext.setOnClickListener(x -> requireActivity().onBackPressed());
    }
}
