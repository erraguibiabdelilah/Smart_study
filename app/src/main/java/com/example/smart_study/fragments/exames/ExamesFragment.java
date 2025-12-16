package com.example.smart_study.fragments.exames;

import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.smart_study.R;
import com.example.smart_study.services.generateExams.ExamQuestionModel;

import java.util.List;
import java.util.Locale;

public class ExamesFragment extends Fragment {

    private List<ExamQuestionModel> examQuestions;
    private int currentQuestionIndex = 0;

    // Timer
    private CountDownTimer examTimer;
    private long totalTimeInMillis; // Durée totale de l'examen en millisecondes
    private long timeRemainingInMillis;
    private boolean isTimerRunning = false;
    private boolean examCompleted = false;

    // UI Components
    private TextView questionNumberText;
    private TextView questionText;
    private RadioGroup optionsGroup;
    private Button btnPrevious;
    private Button btnNext;
    private Button btnSubmit;
    private ProgressBar questionProgressBar;
    private TextView progressText;
    private LinearLayout resultContainer;
    private TextView scoreText;
    private TextView resultDetailsText;
    private Button btnRetry;
    private Button btnBack;

    // Timer UI
    private TextView timerText;
    private ProgressBar timerProgressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_exames, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            examQuestions = (List<ExamQuestionModel>) getArguments().getSerializable("exams");
            // Récupérer le temps de l'examen (en minutes)
            int examDuration = getArguments().getInt("exam_duration", 30);
            totalTimeInMillis = examDuration * 60 * 1000L;
        }

        if (examQuestions == null || examQuestions.isEmpty()) {
            Toast.makeText(requireContext(), R.string.no_exam_questions_available, Toast.LENGTH_SHORT).show();
            if (isAdded()) {
                requireActivity().onBackPressed();
            }
            return;
        }

        initializeViews(view);
        setupListeners();
        displayQuestion(currentQuestionIndex);
        startExamTimer();
    }

    private void initializeViews(View view) {
        questionNumberText = view.findViewById(R.id.questionNumberText);
        questionText = view.findViewById(R.id.questionText);
        optionsGroup = view.findViewById(R.id.optionsGroup);
        btnPrevious = view.findViewById(R.id.btnPrevious);
        btnNext = view.findViewById(R.id.btnNext);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        questionProgressBar = view.findViewById(R.id.questionProgressBar);
        progressText = view.findViewById(R.id.progressText);
        resultContainer = view.findViewById(R.id.resultContainer);
        scoreText = view.findViewById(R.id.scoreText);
        resultDetailsText = view.findViewById(R.id.resultDetailsText);
        btnRetry = view.findViewById(R.id.btnRetry);
        btnBack = view.findViewById(R.id.btnBack);

        timerText = view.findViewById(R.id.timerText);
        timerProgressBar = view.findViewById(R.id.timerProgressBar);

        questionProgressBar.setMax(examQuestions.size());
        timerProgressBar.setMax(100);
    }

    private void setupListeners() {
        btnPrevious.setOnClickListener(v -> {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                displayQuestion(currentQuestionIndex);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentQuestionIndex < examQuestions.size() - 1) {
                currentQuestionIndex++;
                displayQuestion(currentQuestionIndex);
            }
        });

        btnSubmit.setOnClickListener(v -> {
            showConfirmSubmitDialog();
        });

        btnRetry.setOnClickListener(v -> resetExam());

        btnBack.setOnClickListener(v -> {
            if (isAdded()) {
                requireActivity().onBackPressed();
            }
        });

        optionsGroup.setOnCheckedChangeListener((group, checkedId) -> {
            View radioButton = group.findViewById(checkedId);
            int selectedIndex = group.indexOfChild(radioButton);
            if (selectedIndex != -1) {
                examQuestions.get(currentQuestionIndex).setUserSelectedAnswer(selectedIndex);
            }
        });
    }

    private void startExamTimer() {
        timeRemainingInMillis = totalTimeInMillis;
        isTimerRunning = true;
        examCompleted = false;

        examTimer = new CountDownTimer(timeRemainingInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemainingInMillis = millisUntilFinished;
                updateTimerUI();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                handleTimeUp();
            }
        }.start();
    }

    private void updateTimerUI() {
        int minutes = (int) (timeRemainingInMillis / 1000) / 60;
        int seconds = (int) (timeRemainingInMillis / 1000) % 60;

        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        timerText.setText(timeFormatted);

        // Mettre à jour la barre de progression circulaire
        int progress = (int) ((timeRemainingInMillis * 100) / totalTimeInMillis);
        timerProgressBar.setProgress(progress);

        // Changer la couleur selon le temps restant
        if (timeRemainingInMillis < 5 * 60 * 1000) { // Moins de 5 minutes
            timerText.setTextColor(Color.parseColor("#EF4444")); // Rouge
            if (timeRemainingInMillis % 2000 < 1000) {
                timerText.setAlpha(0.3f);
            } else {
                timerText.setAlpha(1.0f);
            }
        } else if (timeRemainingInMillis < 10 * 60 * 1000) { // Moins de 10 minutes
            timerText.setTextColor(Color.parseColor("#F59E0B")); // Orange
            timerText.setAlpha(1.0f);
        } else {
            timerText.setTextColor(Color.parseColor("#1E3A8A")); // Bleu
            timerText.setAlpha(1.0f);
        }
    }

    private void handleTimeUp() {
        if (!isAdded() || examCompleted) return;

        examCompleted = true;

        // IMPORTANT: Marquer toutes les questions non répondues comme fausses
        markUnansweredQuestionsAsWrong();

        Toast.makeText(requireContext(), "⏰ Temps écoulé ! L'examen est terminé automatiquement.", Toast.LENGTH_LONG).show();

        // Désactiver toutes les interactions
        disableExamInteraction();

        // Afficher les résultats automatiquement
        showResults();
    }

    /**
     * Marque toutes les questions non répondues comme fausses (réponse = -1)
     * Cela garantit qu'elles seront comptées comme incorrectes dans le calcul du score
     */
    private void markUnansweredQuestionsAsWrong() {
        for (ExamQuestionModel question : examQuestions) {
            if (question.getUserSelectedAnswer() == -1) {
                // La question n'a pas été répondue, elle restera à -1
                // Dans le calcul du score, -1 sera considéré comme faux
                // car isCorrect() retournera false si userSelectedAnswer != correctAnswer
            }
        }
    }

    private void disableExamInteraction() {
        optionsGroup.setEnabled(false);
        for (int i = 0; i < optionsGroup.getChildCount(); i++) {
            optionsGroup.getChildAt(i).setEnabled(false);
        }
        btnPrevious.setEnabled(false);
        btnNext.setEnabled(false);
        btnSubmit.setEnabled(false);
    }

    private void showConfirmSubmitDialog() {
        // Compter les questions non répondues
        int unansweredCount = 0;
        for (ExamQuestionModel question : examQuestions) {
            if (question.getUserSelectedAnswer() == -1) {
                unansweredCount++;
            }
        }

        String message;
        if (unansweredCount > 0) {
            message = String.format(Locale.getDefault(),
                    "⚠️ Attention : Vous avez %d question(s) non répondue(s).\n\n" +
                            "Les questions non répondues seront comptées comme fausses.\n\n" +
                            "Voulez-vous vraiment terminer l'examen ?",
                    unansweredCount);
        } else {
            message = "✅ Toutes les questions ont été répondues.\n\nVoulez-vous terminer l'examen ?";
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmer la soumission")
                .setMessage(message)
                .setPositiveButton("Oui, terminer", (dialog, which) -> {
                    examCompleted = true;
                    stopTimer();
                    showResults();
                })
                .setNegativeButton("Non, continuer", null)
                .setCancelable(true)
                .show();
    }

    private void stopTimer() {
        if (examTimer != null) {
            examTimer.cancel();
            isTimerRunning = false;
        }
    }

    private void displayQuestion(int index) {
        ExamQuestionModel question = examQuestions.get(index);

        questionNumberText.setText(getString(R.string.question_header, index + 1, examQuestions.size()));
        questionText.setText(question.getQuestion());

        questionProgressBar.setProgress(index + 1);
        progressText.setText(getString(R.string.question_header, index + 1, examQuestions.size()));

        optionsGroup.removeAllViews();

        List<String> options = question.getOptions();
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{new int[]{}},
                new int[]{ContextCompat.getColor(requireContext(), R.color.radio_button_text_color)}
        );
        for (int i = 0; i < options.size(); i++) {
            RadioButton radioButton = new RadioButton(requireContext());
            radioButton.setText(options.get(i));
            radioButton.setId(View.generateViewId());
            radioButton.setTextSize(16);
            radioButton.setPadding(20, 20, 20, 20);
            radioButton.setTextColor(colorStateList);

            if (question.getUserSelectedAnswer() == i) {
                radioButton.setChecked(true);
            }

            optionsGroup.addView(radioButton);
        }

        btnPrevious.setEnabled(index > 0);
        btnNext.setVisibility(index < examQuestions.size() - 1 ? View.VISIBLE : View.GONE);
        btnSubmit.setVisibility(index == examQuestions.size() - 1 ? View.VISIBLE : View.GONE);
    }

    private void showResults() {
        if (!isAdded()) return;

        int correctAnswers = 0;
        int totalQuestions = examQuestions.size();
        int unansweredQuestions = 0;

        // Calculer les réponses correctes et les questions non répondues
        for (ExamQuestionModel question : examQuestions) {
            if (question.getUserSelectedAnswer() == -1) {
                unansweredQuestions++;
                // Les questions non répondues sont automatiquement fausses
            } else if (question.isCorrect()) {
                correctAnswers++;
            }
        }

        double percentage = (totalQuestions > 0) ? (correctAnswers * 100.0) / totalQuestions : 0;

        // Calculer le temps utilisé
        long timeUsed = totalTimeInMillis - timeRemainingInMillis;
        int minutesUsed = (int) (timeUsed / 1000) / 60;
        int secondsUsed = (int) (timeUsed / 1000) % 60;
        String timeUsedFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutesUsed, secondsUsed);

        // Masquer l'interface de l'examen
        questionNumberText.setVisibility(View.GONE);
        questionText.setVisibility(View.GONE);
        optionsGroup.setVisibility(View.GONE);
        btnPrevious.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);
        btnSubmit.setVisibility(View.GONE);
        questionProgressBar.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);
        timerText.setVisibility(View.GONE);
        timerProgressBar.setVisibility(View.GONE);

        // Afficher les résultats
        resultContainer.setVisibility(View.VISIBLE);
        scoreText.setText(String.format(Locale.getDefault(), "%.0f%%", percentage));

        String resultMessage;
        String emoji;
        if (percentage >= 80) {
            resultMessage = "Excellent ! Vous maîtrisez le sujet !";
            emoji = "🎉";
        } else if (percentage >= 60) {
            resultMessage = "Bon travail ! Continuez vos efforts !";
            emoji = "👍";
        } else if (percentage >= 40) {
            resultMessage = "Passable. Il faut réviser davantage.";
            emoji = "📚";
        } else {
            resultMessage = "Vous devez améliorer vos connaissances.";
            emoji = "💪";
        }

        // Construire le message détaillé
        StringBuilder detailsBuilder = new StringBuilder();
        detailsBuilder.append(emoji).append(" ").append(resultMessage).append("\n\n");
        detailsBuilder.append("📊 Résultats : ").append(correctAnswers).append(" / ").append(totalQuestions).append(" questions correctes\n\n");

        if (unansweredQuestions > 0) {
            detailsBuilder.append("⚠️ Questions non répondues : ").append(unansweredQuestions).append("\n\n");
        }

        detailsBuilder.append("⏱️ Temps utilisé : ").append(timeUsedFormatted);

        // Ajouter un message si le temps était écoulé
        if (timeRemainingInMillis <= 0) {
            detailsBuilder.append("\n\n⏰ Examen terminé par expiration du temps");
        }

        resultDetailsText.setText(detailsBuilder.toString());

        // Animation du score
        animateScore(percentage);
    }

    private void animateScore(double targetPercentage) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, (float) targetPercentage);
        animator.setDuration(2000);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            scoreText.setText(String.format(Locale.getDefault(), "%.0f%%", value));
        });
        animator.start();
    }

    private void resetExam() {
        // Réinitialiser toutes les réponses
        for (ExamQuestionModel question : examQuestions) {
            question.setUserSelectedAnswer(-1);
        }
        currentQuestionIndex = 0;
        examCompleted = false;

        // Masquer les résultats
        resultContainer.setVisibility(View.GONE);

        // Réafficher l'interface de l'examen
        questionNumberText.setVisibility(View.VISIBLE);
        questionText.setVisibility(View.VISIBLE);
        optionsGroup.setVisibility(View.VISIBLE);
        btnPrevious.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.VISIBLE);
        questionProgressBar.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);
        timerText.setVisibility(View.VISIBLE);
        timerProgressBar.setVisibility(View.VISIBLE);

        // Réactiver les interactions
        optionsGroup.setEnabled(true);
        btnPrevious.setEnabled(true);
        btnNext.setEnabled(true);
        btnSubmit.setEnabled(true);

        // Redémarrer l'examen
        displayQuestion(0);
        startExamTimer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Arrêter le timer si l'utilisateur quitte l'écran
        // IMPORTANT: L'examen sera considéré comme abandonné
        if (isTimerRunning && !examCompleted) {
            stopTimer();
            Toast.makeText(requireContext(), "⚠️ Timer arrêté - Examen en pause", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Optionnel: Reprendre le timer si nécessaire
        // Pour l'instant, on ne reprend pas automatiquement pour éviter la triche
    }
}
