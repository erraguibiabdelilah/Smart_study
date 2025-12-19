package com.example.smart_study.fragments;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smart_study.R;
import com.example.smart_study.beans.ExamHistoryModel;
import com.example.smart_study.services.generateExams.ExamQuestionModel;
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

public class ExamDetailFragment extends Fragment {

    private static final String ARG_EXAM = "arg_exam";
    private ExamHistoryModel examHistory;

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1002;
    private static final String CHANNEL_ID = "exam_download_channel";
    private static final int NOTIFICATION_ID = 101;

    public static ExamDetailFragment newInstance(ExamHistoryModel exam) {
        ExamDetailFragment fragment = new ExamDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EXAM, exam);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            examHistory = (ExamHistoryModel) getArguments().getSerializable(ARG_EXAM);
        }
        createNotificationChannel();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exam_detail, container, false);

        // Header
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Bouton de téléchargement
        view.findViewById(R.id.btnDownloadPdf).setOnClickListener(v -> checkPermissionAndDownload());

        // Populate Summary
        TextView tvExamSubject = view.findViewById(R.id.tvExamSubject);
        TextView tvDetailScore = view.findViewById(R.id.tvDetailScore);
        TextView tvDetailDate = view.findViewById(R.id.tvDetailDate);

        if (examHistory != null) {
            tvExamSubject.setText(examHistory.getExamTitle());
            tvDetailScore.setText((int) examHistory.getScorePercentage() + "%");
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
            if (examHistory.getCompletedDate() != null) {
                tvDetailDate.setText(dateFormat.format(examHistory.getCompletedDate()));
            }
        }

        // Setup RecyclerView for questions
        RecyclerView rvQuestions = view.findViewById(R.id.rvDetailQuestions);
        rvQuestions.setLayoutManager(new LinearLayoutManager(getContext()));
        
        if (examHistory != null && examHistory.getQuestions() != null) {
            QuestionsDetailAdapter adapter = new QuestionsDetailAdapter(examHistory.getQuestions());
            rvQuestions.setAdapter(adapter);
        }

        return view;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Téléchargements de PDF";
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

    private void checkPermissionAndDownload() {
        // Android < 10 : Permission de stockage nécessaire
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                return;
            }
        }
        
        // Android 13+ : Permission de notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
                // On continue quand même le téléchargement même sans notif
            }
        }

        generateAndDownloadPdf();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Permission de stockage (Android < 10)
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generateAndDownloadPdf(); // On a la permission, on génère
            } else {
                Toast.makeText(requireContext(), "Permission de stockage refusée.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            // Permission de notification (Android 13+)
            // Qu'elle soit acceptée ou refusée, on lance le téléchargement (le fichier sera créé, seule la notif dépend de la permission)
            generateAndDownloadPdf();
        }
    }

    private void generateAndDownloadPdf() {
        if (examHistory == null) return;

        Uri pdfUri = null;
        String fileName = "";

        try {
            fileName = "Correction_" + examHistory.getExamTitle().replaceAll("\\s+", "_") + "_" +
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

            // Sujet
            Paragraph subject = new Paragraph(examHistory.getExamTitle(), headerFont);
            subject.setAlignment(Element.ALIGN_CENTER);
            subject.setSpacingAfter(5);
            document.add(subject);

            // Date
            String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
            Paragraph date = new Paragraph("Généré le : " + currentDate, smallFont);
            date.setAlignment(Element.ALIGN_CENTER);
            date.setSpacingAfter(20);
            document.add(date);

            // Résumé des résultats (Tableau)
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingAfter(20);

            addSummaryCell(summaryTable, "Score", (int)examHistory.getScorePercentage() + "%");
            addSummaryCell(summaryTable, "Questions correctes", examHistory.getCorrectAnswers() + " / " + examHistory.getTotalQuestions());
            addSummaryCell(summaryTable, "Temps utilisé", examHistory.getTimeUsed() != null ? examHistory.getTimeUsed() : "-");

            document.add(summaryTable);

            // Ligne de séparation
            document.add(new Paragraph("\n"));

            // Questions et corrections
            List<ExamQuestionModel> examQuestions = examHistory.getQuestions();
            if (examQuestions != null) {
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
                        
                        // Déterminer la couleur et le style
                        Font optionFont = smallFont;
                        
                        // Colorier la bonne réponse en vert
                        if (j == question.getCorrectAnswerIndex()) {
                            optionFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, new BaseColor(0, 150, 0));
                        }
                        // Colorier la réponse de l'utilisateur en rouge si incorrecte
                        else if (j == question.getUserSelectedAnswer() && !question.isCorrect()) {
                            optionFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, new BaseColor(200, 0, 0));
                        }

                        Paragraph option = new Paragraph(optionPrefix + options.get(j), optionFont);
                        option.setIndentationLeft(20);
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

                    // Ligne de séparation entre les questions
                    if (i < examQuestions.size() - 1) {
                        Paragraph separator = new Paragraph("_______________________________________", smallFont);
                        separator.setAlignment(Element.ALIGN_CENTER);
                        separator.setSpacingBefore(10);
                        separator.setSpacingAfter(10);
                        document.add(separator);
                    }
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

    private void addSummaryCell(PdfPTable table, String label, String value) {
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 10);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, boldFont));
        labelCell.setBackgroundColor(new BaseColor(230, 230, 230));
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, normalFont));
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }
    
    private String getOptionLetter(int index) {
        if (index >= 0 && index < 26) {
            return String.valueOf((char) ('A' + index));
        }
        return "?";
    }

    private void showDownloadNotification(String fileName, Uri fileUri) {
        // Pour éviter les crashs sur Android 13+ sans permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                // Si la permission n'est pas accordée, on ne peut pas afficher la notif
                // Mais le fichier a déjà été téléchargé, donc c'est ok.
                return;
            }
        }

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
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("✅ PDF téléchargé")
                .setContentText(fileName)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Votre correction d'examen a été téléchargée avec succès.\nAppuyez pour ouvrir le fichier."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Afficher la notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // =========================================================================
    // ADAPTER POUR LES QUESTIONS
    // =========================================================================
    private class QuestionsDetailAdapter extends RecyclerView.Adapter<QuestionsDetailViewHolder> {

        private List<ExamQuestionModel> questions;

        public QuestionsDetailAdapter(List<ExamQuestionModel> questions) {
            this.questions = questions;
        }

        @NonNull
        @Override
        public QuestionsDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // CORRECTION: Utilisation de R.layout.item_exam_detail_question car le fichier XML ne se termine pas par .xml dans R.layout
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_exam_detail_question, parent, false);
            return new QuestionsDetailViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull QuestionsDetailViewHolder holder, int position) {
            ExamQuestionModel question = questions.get(position);
            holder.bind(question, position + 1);
        }

        @Override
        public int getItemCount() {
            return questions.size();
        }
    }

    // =========================================================================
    // VIEWHOLDER POUR LES QUESTIONS
    // =========================================================================
    private class QuestionsDetailViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestionText, tvStatus;
        LinearLayout layoutOptions;

        public QuestionsDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestionText = itemView.findViewById(R.id.tvQuestionText);
            layoutOptions = itemView.findViewById(R.id.layoutOptions);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        public void bind(ExamQuestionModel question, int questionNumber) {
            tvQuestionText.setText(questionNumber + ". " + question.getQuestion());
            
            // Nettoyer les options précédentes
            layoutOptions.removeAllViews();

            List<String> options = question.getOptions();
            int userSelected = question.getUserSelectedAnswer();
            int correctIndex = question.getCorrectAnswerIndex();

            for (int i = 0; i < options.size(); i++) {
                String optionText = options.get(i);
                TextView optionView = new TextView(itemView.getContext());
                optionView.setText(optionText);
                optionView.setPadding(16, 16, 16, 16);
                optionView.setTextSize(14);
                
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 8, 0, 8);
                optionView.setLayoutParams(params);

                // Logique de couleur
                if (i == correctIndex) {
                    // Bonne réponse : Vert
                    optionView.setBackgroundResource(R.drawable.rounded_bg_success); // Ou couleur verte
                    optionView.setTextColor(Color.parseColor("#065F46")); // Vert foncé
                    optionView.setBackgroundColor(Color.parseColor("#D1FAE5")); // Vert clair
                } else if (i == userSelected && i != correctIndex) {
                    // Mauvaise réponse sélectionnée par l'utilisateur : Rouge
                    optionView.setBackgroundColor(Color.parseColor("#FEE2E2")); // Rouge clair
                    optionView.setTextColor(Color.parseColor("#B91C1C")); // Rouge foncé
                } else {
                    // Option neutre
                    optionView.setBackgroundColor(Color.parseColor("#F3F4F6")); // Gris
                    optionView.setTextColor(Color.BLACK);
                }

                // Marquer la sélection utilisateur
                if (i == userSelected) {
                    optionView.setText(optionView.getText() + " (Votre choix)");
                    optionView.setTypeface(null, android.graphics.Typeface.BOLD);
                }

                layoutOptions.addView(optionView);
            }

            // Statut global de la question
            if (question.isCorrect()) {
                tvStatus.setText("Correct");
                tvStatus.setTextColor(Color.parseColor("#10B981"));
            } else {
                tvStatus.setText("Incorrect");
                tvStatus.setTextColor(Color.parseColor("#EF4444"));
            }
        }
    }
}
