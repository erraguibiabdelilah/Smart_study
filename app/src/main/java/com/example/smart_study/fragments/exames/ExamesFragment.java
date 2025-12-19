package com.example.smart_study.fragments.exames;
import android.Manifest;
import android.animation.ValueAnimator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
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

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.smart_study.R;
import com.example.smart_study.beans.ExamHistoryModel;
import com.example.smart_study.db.AppDatabase;
import com.example.smart_study.services.generateExams.ExamQuestionModel;
// Pour iText 5
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExamesFragment extends Fragment {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;
    private static final String CHANNEL_ID = "exam_pdf_downloads";
    private static final int NOTIFICATION_ID = 1001;

    private List<ExamQuestionModel> examQuestions;
    private int currentQuestionIndex = 0;
    // Timer
    private CountDownTimer examTimer;
    private long totalTimeInMillis;
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
    private Button btnDownloadPdf;
    private Button btnSaveExam; // NOUVEAU

    // Timer UI
    private TextView timerText;
    private ProgressBar timerProgressBar;

    // Résultats pour le PDF et la DB
    private int correctAnswersCount;
    private double scorePercentage;
    private String timeUsedFormatted;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_exames, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Créer le canal de notification
        createNotificationChannel();

        if (getArguments() != null) {
            examQuestions = (List<ExamQuestionModel>) getArguments().getSerializable("exams");
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
        btnDownloadPdf = view.findViewById(R.id.btnDownloadPdf);
        btnSaveExam = view.findViewById(R.id.btnSaveExam); // NOUVEAU

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

        btnSubmit.setOnClickListener(v -> showConfirmSubmitDialog());

        btnRetry.setOnClickListener(v -> resetExam());

        btnBack.setOnClickListener(v -> {
            if (isAdded()) {
                requireActivity().onBackPressed();
            }
        });

        // Bouton de téléchargement PDF
        btnDownloadPdf.setOnClickListener(v -> {
            if (checkPermissions()) {
                generateAndDownloadPdf();
            } else {
                requestPermissions();
            }
        });

        // NOUVEAU : Bouton de sauvegarde
        btnSaveExam.setOnClickListener(v -> saveExamToHistory());

        optionsGroup.setOnCheckedChangeListener((group, checkedId) -> {
            View radioButton = group.findViewById(checkedId);
            int selectedIndex = group.indexOfChild(radioButton);
            if (selectedIndex != -1) {
                examQuestions.get(currentQuestionIndex).setUserSelectedAnswer(selectedIndex);
            }
        });
    }

    private void saveExamToHistory() {
        // Désactiver le bouton pour éviter les doubles clics
        btnSaveExam.setEnabled(false);
        btnSaveExam.setText("Sauvegarde...");

        // Préparer les données
        ExamHistoryModel history = new ExamHistoryModel();
        history.setExamTitle("Examen du " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()));
        history.setCompletedDate(new Date());
        history.setTotalQuestions(examQuestions.size());
        history.setCorrectAnswers(correctAnswersCount);
        history.setScorePercentage(scorePercentage);
        history.setTimeUsed(timeUsedFormatted);
        history.setTimeRemainingMillis(timeRemainingInMillis);
        history.setWasTimeExpired(timeRemainingInMillis <= 0);
        history.setQuestions(examQuestions);

        // Sauvegarder dans la DB (sur un thread séparé)
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(requireContext());
                db.examHistoryDao().insert(history);

                // Mettre à jour l'UI sur le thread principal
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "✅ Examen sauvegardé dans l'historique !", Toast.LENGTH_LONG).show();
                    btnSaveExam.setText("Sauvegardé");
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "❌ Erreur de sauvegarde : " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnSaveExam.setEnabled(true);
                    btnSaveExam.setText("Sauvegarder");
                });
                e.printStackTrace();
            }
        }).start();
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

        int progress = (int) ((timeRemainingInMillis * 100) / totalTimeInMillis);
        timerProgressBar.setProgress(progress);

        if (timeRemainingInMillis < 5 * 60 * 1000) {
            timerText.setTextColor(Color.parseColor("#EF4444"));
            if (timeRemainingInMillis % 2000 < 1000) {
                timerText.setAlpha(0.3f);
            } else {
                timerText.setAlpha(1.0f);
            }
        } else if (timeRemainingInMillis < 10 * 60 * 1000) {
            timerText.setTextColor(Color.parseColor("#F59E0B"));
            timerText.setAlpha(1.0f);
        } else {
            timerText.setTextColor(Color.parseColor("#1E3A8A"));
            timerText.setAlpha(1.0f);
        }
    }

    private void handleTimeUp() {
        if (!isAdded() || examCompleted) return;

        examCompleted = true;
        markUnansweredQuestionsAsWrong();
        Toast.makeText(requireContext(), "⏰ Temps écoulé ! L'examen est terminé automatiquement.", Toast.LENGTH_LONG).show();
        disableExamInteraction();
        showResults();
    }

    private void markUnansweredQuestionsAsWrong() {
        for (ExamQuestionModel question : examQuestions) {
            if (question.getUserSelectedAnswer() == -1) {
                // La question restera à -1 (non répondue = faux)
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

        correctAnswersCount = 0;
        int totalQuestions = examQuestions.size();
        int unansweredQuestions = 0;

        for (ExamQuestionModel question : examQuestions) {
            if (question.getUserSelectedAnswer() == -1) {
                unansweredQuestions++;
            } else if (question.isCorrect()) {
                correctAnswersCount++;
            }
        }

        scorePercentage = (totalQuestions > 0) ? (correctAnswersCount * 100.0) / totalQuestions : 0;

        // Calculer le temps utilisé
        long timeUsed = totalTimeInMillis - timeRemainingInMillis;
        int minutesUsed = (int) (timeUsed / 1000) / 60;
        int secondsUsed = (int) (timeUsed / 1000) % 60;
        timeUsedFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutesUsed, secondsUsed);

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
        scoreText.setText(String.format(Locale.getDefault(), "%.0f%%", scorePercentage));

        String resultMessage;
        String emoji;
        if (scorePercentage >= 80) {
            resultMessage = "Excellent ! Vous maîtrisez le sujet !";
            emoji = "";
        } else if (scorePercentage >= 60) {
            resultMessage = "Bon travail ! Continuez vos efforts !";
            emoji = "";
        } else if (scorePercentage >= 40) {
            resultMessage = "Passable. Il faut réviser davantage.";
            emoji = "";
        } else {
            resultMessage = "Vous devez améliorer vos connaissances.";
            emoji = "";
        }

        StringBuilder detailsBuilder = new StringBuilder();
        detailsBuilder.append(emoji).append(" ").append(resultMessage).append("\n\n");
        detailsBuilder.append("Résultats : ").append(correctAnswersCount).append(" / ").append(totalQuestions).append(" questions correctes\n\n");

        if (unansweredQuestions > 0) {
            detailsBuilder.append(" Questions non répondues : ").append(unansweredQuestions).append("\n\n");
        }

        detailsBuilder.append(" Temps utilisé : ").append(timeUsedFormatted);

        if (timeRemainingInMillis <= 0) {
            detailsBuilder.append("\n\n Examen terminé par expiration du temps");
        }

        resultDetailsText.setText(detailsBuilder.toString());
        
        // Réactiver le bouton de sauvegarde pour permettre une nouvelle sauvegarde si nécessaire
        btnSaveExam.setEnabled(true);
        btnSaveExam.setText("💾 Sauvegarder");

        animateScore(scorePercentage);
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

    // ==================== GESTION DES PERMISSIONS ====================

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Téléchargements PDF Examens";
            String description = "Notifications pour les téléchargements de corrections d'examens";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);

            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private boolean checkPermissions() {
        boolean storagePermission = true;
        boolean notificationPermission = true;

        // Vérifier permission de stockage
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            storagePermission = ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }

        // Vérifier permission de notification (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission = ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }

        return storagePermission && notificationPermission;
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ : Demander permission de notification
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST_CODE);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // Android 9 et inférieur : Demander permission de stockage
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE || requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generateAndDownloadPdf();
            } else {
                Toast.makeText(requireContext(), "Permission refusée. Impossible de télécharger le PDF.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // ==================== GÉNÉRATION DU PDF ====================

    private void generateAndDownloadPdf() {
        Uri pdfUri = null;
        String fileName = "";

        try {
            fileName = "Correction_Examen_" +
                    new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";

            OutputStream outputStream;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ : Utiliser MediaStore
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                pdfUri = requireContext().getContentResolver().insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                if (pdfUri == null) {
                    Toast.makeText(requireContext(), "Erreur lors de la création du fichier", Toast.LENGTH_SHORT).show();
                    return;
                }

                outputStream = requireContext().getContentResolver().openOutputStream(pdfUri);
            } else {
                // Android 9 et inférieur
                File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File pdfFile = new File(downloadDir, fileName);
                pdfUri = Uri.fromFile(pdfFile);
                outputStream = new FileOutputStream(pdfFile);
            }

            // Créer le document PDF avec iText5
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Définir les polices
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 11);
            Font smallFont = new Font(Font.FontFamily.HELVETICA, 10);
            Font tinyFont = new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC);

            // Titre
            Paragraph title = new Paragraph("CORRECTION D'EXAMEN", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            // Date
            String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
            Paragraph date = new Paragraph("Date : " + currentDate, smallFont);
            date.setAlignment(Element.ALIGN_CENTER);
            date.setSpacingAfter(20);
            document.add(date);

            // Résumé des résultats (Tableau)
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingAfter(20);

            addSummaryCell(summaryTable, "Score", String.format(Locale.getDefault(), "%.0f%%", scorePercentage));
            addSummaryCell(summaryTable, "Questions correctes", correctAnswersCount + " / " + examQuestions.size());
            addSummaryCell(summaryTable, "Temps utilisé", timeUsedFormatted);

            document.add(summaryTable);

            // Ligne de séparation
            document.add(new Paragraph("\n"));

            // Questions et corrections
            for (int i = 0; i < examQuestions.size(); i++) {
                ExamQuestionModel question = examQuestions.get(i);

                // Numéro de la question
                Paragraph questionNumber = new Paragraph("Question " + (i + 1), headerFont);
                questionNumber.setSpacingBefore(15);
                questionNumber.setSpacingAfter(5);
                document.add(questionNumber);

                // Texte de la question
                Paragraph questionText = new Paragraph(question.getQuestion(), normalFont);
                questionText.setSpacingAfter(10);
                document.add(questionText);

                // Options
                List<String> options = question.getOptions();
                for (int j = 0; j < options.size(); j++) {
                    String optionPrefix = getOptionLetter(j) + ") ";
                    Paragraph option = new Paragraph(optionPrefix + options.get(j), smallFont);
                    option.setIndentationLeft(20);

                    // Colorier la bonne réponse en vert
                    if (j == question.getCorrectAnswerIndex()) {
                        Font greenFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
                        greenFont.setColor(new BaseColor(0, 150, 0));
                        option = new Paragraph(optionPrefix + options.get(j), greenFont);
                        option.setIndentationLeft(20);
                    }

                    // Colorier la réponse de l'utilisateur en rouge si incorrecte
                    if (j == question.getUserSelectedAnswer() && !question.isCorrect()) {
                        Font redFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);
                        redFont.setColor(new BaseColor(200, 0, 0));
                        option = new Paragraph(optionPrefix + options.get(j), redFont);
                        option.setIndentationLeft(20);
                    }

                    document.add(option);
                }

                // Réponse de l'utilisateur et explication
                String userAnswerText = question.getUserSelectedAnswer() == -1
                        ? "Non répondue"
                        : getOptionLetter(question.getUserSelectedAnswer());

                String correctAnswerText = getOptionLetter(question.getCorrectAnswerIndex());
                String statusEmoji = question.isCorrect() ? "[CORRECT]" : "[INCORRECT]";

                Font answerFont = question.isCorrect()
                        ? new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, new BaseColor(0, 150, 0))
                        : new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, new BaseColor(200, 0, 0));

                Paragraph answerInfo = new Paragraph(
                        statusEmoji + " Votre réponse : " + userAnswerText + "\n" +
                                "Bonne réponse : " + correctAnswerText,
                        answerFont
                );
                answerInfo.setSpacingBefore(10);
                answerInfo.setSpacingAfter(5);
                answerInfo.setIndentationLeft(20);
                document.add(answerInfo);

                /*
                // Explication si disponible (COMMENTÉ car getExplanation() n'existe pas)
                if (question.getExplanation() != null && !question.getExplanation().isEmpty()) {
                    Font grayFont = new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, new BaseColor(100, 100, 100));
                    Paragraph explanation = new Paragraph("Explication : " + question.getExplanation(), grayFont);
                    explanation.setIndentationLeft(20);
                    explanation.setSpacingAfter(10);
                    document.add(explanation);
                }
                */

                // Ligne de séparation entre les questions
                if (i < examQuestions.size() - 1) {
                    Paragraph separator = new Paragraph("_______________________________________", smallFont);
                    separator.setAlignment(Element.ALIGN_CENTER);
                    separator.setSpacingBefore(10);
                    separator.setSpacingAfter(10);
                    document.add(separator);
                }
            }

            // Pied de page
            document.add(new Paragraph("\n\n"));
            Font footerFont = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, new BaseColor(150, 150, 150));
            Paragraph footer = new Paragraph("Document généré par Smart Study", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            // Fermer le document
            document.close();

            // Afficher la notification
            showDownloadNotification(fileName, pdfUri);

            // Toast de succès
            Toast.makeText(requireContext(),
                    "✅ PDF téléchargé avec succès",
                    Toast.LENGTH_LONG).show();

        } catch (DocumentException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(),
                    "❌ Erreur lors de la génération du PDF: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(),
                    "❌ Erreur lors de la génération du PDF: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void showDownloadNotification(String fileName, Uri fileUri) {
        // Intent pour ouvrir le PDF
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                requireContext(),
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Construire la notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done) // Icône de téléchargement
                .setContentTitle("✅ PDF téléchargé")
                .setContentText(fileName)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Votre correction d'examen a été téléchargée avec succès.\nAppuyez pour ouvrir le fichier."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true) // La notification disparaît quand on clique dessus
                .setVibrate(new long[]{0, 500, 200, 500}) // Vibration
                .setLights(Color.BLUE, 1000, 1000); // LED bleue

        // Afficher la notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());

        // Vérifier la permission de notification pour Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(NOTIFICATION_ID, builder.build());
            }
        } else {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private void addSummaryCell(PdfPTable table, String label, String value) {
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 10);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, boldFont));
        labelCell.setBackgroundColor(new BaseColor(230, 230, 230));
        labelCell.setPadding(5);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, normalFont));
        valueCell.setPadding(5);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private String getOptionLetter(int index) {
        return String.valueOf((char) ('A' + index));
    }

    // ==================== AUTRES MÉTHODES ====================
    private void resetExam() {
        for (ExamQuestionModel question : examQuestions) {
            question.setUserSelectedAnswer(-1);
        }
        currentQuestionIndex = 0;
        examCompleted = false;

        resultContainer.setVisibility(View.GONE);

        questionNumberText.setVisibility(View.VISIBLE);
        questionText.setVisibility(View.VISIBLE);
        optionsGroup.setVisibility(View.VISIBLE);
        btnPrevious.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.VISIBLE);
        questionProgressBar.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);
        timerText.setVisibility(View.VISIBLE);
        timerProgressBar.setVisibility(View.VISIBLE);

        optionsGroup.setEnabled(true);
        btnPrevious.setEnabled(true);
        btnNext.setEnabled(true);
        btnSubmit.setEnabled(true);

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
        if (isTimerRunning && !examCompleted) {
            stopTimer();
            Toast.makeText(requireContext(), " Timer arrêté - Examen en pause", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}