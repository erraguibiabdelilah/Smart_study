package com.example.smart_study.fragments.resumes;

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
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.example.smart_study.R;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ResumeDisplayFragment extends Fragment {

    private static final int PERMISSION_REQUEST_CODE = 2001;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 2002;
    private static final String CHANNEL_ID = "resume_download_channel";
    private static final int NOTIFICATION_ID = 201;

    private WebView webView;
    private Button btnSavePdf;
    private String resumeContent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resume_display, container, false);

        webView = view.findViewById(R.id.webView);
        btnSavePdf = view.findViewById(R.id.btnSavePdf);

        if (getArguments() != null) {
            resumeContent = getArguments().getString("resume");
            if (resumeContent != null) {
                displayStyledResume(resumeContent);
            }
        }

        btnSavePdf.setOnClickListener(v -> checkPermissionAndSave());

        return view;
    }

    private void displayStyledResume(String resume) {
        String cleanedResume = resume.replaceAll("<s>", "").replaceAll("</s>", "").trim();

        String htmlResume = cleanedResume
                .replace("\n\n", "</p><p>")
                .replace("\n", "<br>");

        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta charset='UTF-8'>\n" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "<style>\n" +
                "body {\n" +
                "    font-family: 'Georgia', 'Times New Roman', serif;\n" +
                "    line-height: 1.8;\n" +
                "    color: #2c3e50;\n" +
                "    max-width: 800px;\n" +
                "    margin: 0 auto;\n" +
                "    padding: 30px;\n" +
                "    background: #ffffff;\n" +
                "}\n" +
                "h1 {\n" +
                "    color: #1a237e;\n" +
                "    border-bottom: 3px solid #3f51b5;\n" +
                "    padding-bottom: 10px;\n" +
                "    margin-bottom: 25px;\n" +
                "    font-size: 28px;\n" +
                "}\n" +
                "h2 {\n" +
                "    color: #283593;\n" +
                "    margin-top: 25px;\n" +
                "    margin-bottom: 12px;\n" +
                "    font-size: 20px;\n" +
                "    border-left: 4px solid #3f51b5;\n" +
                "    padding-left: 10px;\n" +
                "}\n" +
                "h3 {\n" +
                "    color: #3949ab;\n" +
                "    margin-top: 18px;\n" +
                "    margin-bottom: 10px;\n" +
                "    font-size: 16px;\n" +
                "}\n" +
                "p {\n" +
                "    margin-bottom: 12px;\n" +
                "    text-align: justify;\n" +
                "}\n" +
                "ul, ol {\n" +
                "    margin-left: 25px;\n" +
                "    margin-bottom: 15px;\n" +
                "}\n" +
                "li {\n" +
                "    margin-bottom: 8px;\n" +
                "    line-height: 1.6;\n" +
                "}\n" +
                "strong {\n" +
                "    color: #1a237e;\n" +
                "    font-weight: bold;\n" +
                "}\n" +
                "em {\n" +
                "    color: #5c6bc0;\n" +
                "    font-style: italic;\n" +
                "}\n" +
                ".section {\n" +
                "    margin-bottom: 25px;\n" +
                "    padding: 15px;\n" +
                "    background: #f8f9fa;\n" +
                "    border-radius: 5px;\n" +
                "}\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<h1>Résumé du Document</h1>\n" +
                "<div class='section'>" +
                "<p>" + htmlResume + "</p>" +
                "</div>" +
                "</body>\n" +
                "</html>";

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Téléchargements de CV";
            String description = "Notifications pour les téléchargements de CV/résumés";
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

    private void checkPermissionAndSave() {
        if (resumeContent == null || resumeContent.isEmpty()) {
            Toast.makeText(requireContext(), "❌ Aucun contenu de résumé disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        // Android < 10 : Permission de stockage nécessaire
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                return;
            }
        }

        // Android 13+ : Permission de notification nécessaire
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
                // On continue quand même, la notification ne sera juste pas affichée
            }
        }

        saveResumeAsPdf();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveResumeAsPdf();
            } else {
                Toast.makeText(requireContext(), "❌ Permission refusée. Impossible de sauvegarder le PDF.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void saveResumeAsPdf() {
        if (resumeContent == null || resumeContent.isEmpty()) {
            Toast.makeText(requireContext(), "❌ Aucun contenu de résumé disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri pdfUri = null;
        String fileName = "";

        try {
            fileName = "Resume_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";

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
                    Toast.makeText(requireContext(), "❌ Erreur lors de la création du fichier", Toast.LENGTH_SHORT).show();
                    return;
                }

                outputStream = requireContext().getContentResolver().openOutputStream(pdfUri);
                if (outputStream == null) {
                    Toast.makeText(requireContext(), "❌ Erreur lors de l'ouverture du flux de sortie", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                // Android 9 et inférieur
                File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (downloadDir == null || !downloadDir.exists()) {
                    Toast.makeText(requireContext(), "❌ Répertoire de téléchargement introuvable", Toast.LENGTH_SHORT).show();
                    return;
                }
                File pdfFile = new File(downloadDir, fileName);
                pdfUri = Uri.fromFile(pdfFile);
                outputStream = new FileOutputStream(pdfFile);
            }

            // Créer le document PDF avec iText5
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Définir les polices
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, new BaseColor(26, 35, 126));
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, new BaseColor(40, 53, 147));
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 11);
            Font smallFont = new Font(Font.FontFamily.HELVETICA, 10);

            // Titre
            Paragraph title = new Paragraph("Résumé du Document", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            // Date
            String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
            Paragraph date = new Paragraph("Généré le : " + currentDate, smallFont);
            date.setAlignment(Element.ALIGN_CENTER);
            date.setSpacingAfter(20);
            document.add(date);

            // Convertir le HTML en texte formaté pour le PDF
            String plainText = convertHtmlToPlainText(resumeContent);
            String[] paragraphs = plainText.split("\n\n");

            for (String para : paragraphs) {
                if (para.trim().isEmpty()) continue;

                // Détecter les titres (lignes courtes ou en majuscules)
                if (para.length() < 100 && para.trim().matches("^[A-ZÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞŸ].*")) {
                    Paragraph header = new Paragraph(para.trim(), headerFont);
                    header.setSpacingBefore(15);
                    header.setSpacingAfter(8);
                    document.add(header);
                } else {
                    // Traiter les listes
                    if (para.contains("•") || para.contains("-") || para.matches(".*\\d+\\..*")) {
                        String[] lines = para.split("\n");
                        for (String line : lines) {
                            line = line.trim();
                            if (line.isEmpty()) continue;
                            // Enlever les puces et numéros
                            line = line.replaceFirst("^[•\\-\\d+\\.]\\s*", "").trim();
                            if (!line.isEmpty()) {
                                Paragraph listItem = new Paragraph("• " + line, normalFont);
                                listItem.setIndentationLeft(20);
                                listItem.setSpacingAfter(5);
                                document.add(listItem);
                            }
                        }
                    } else {
                        Paragraph paragraph = new Paragraph(para.trim(), normalFont);
                        paragraph.setSpacingAfter(10);
                        paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                        document.add(paragraph);
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
            outputStream.close();

            // Afficher la notification
            showDownloadNotification(fileName, pdfUri);

            // Toast de succès
            Toast.makeText(requireContext(),
                    "✅ PDF sauvegardé avec succès",
                    Toast.LENGTH_LONG).show();

        } catch (DocumentException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(),
                    "❌ Erreur lors de la génération du PDF: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(),
                    "❌ Erreur lors de la sauvegarde du PDF: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private String convertHtmlToPlainText(String html) {
        if (html == null) return "";

        // Nettoyer le HTML
        String text = html
                .replaceAll("<s>", "")
                .replaceAll("</s>", "")
                .replaceAll("<br\\s*/?>", "\n")
                .replaceAll("<p>", "")
                .replaceAll("</p>", "\n\n")
                .replaceAll("<h1>", "\n\n")
                .replaceAll("</h1>", "\n\n")
                .replaceAll("<h2>", "\n\n")
                .replaceAll("</h2>", "\n\n")
                .replaceAll("<h3>", "\n\n")
                .replaceAll("</h3>", "\n\n")
                .replaceAll("<li>", "• ")
                .replaceAll("</li>", "\n")
                .replaceAll("<ul>", "")
                .replaceAll("</ul>", "\n")
                .replaceAll("<ol>", "")
                .replaceAll("</ol>", "\n")
                .replaceAll("<strong>", "")
                .replaceAll("</strong>", "")
                .replaceAll("<em>", "")
                .replaceAll("</em>", "")
                .replaceAll("<div[^>]*>", "")
                .replaceAll("</div>", "")
                .replaceAll("<[^>]+>", "") // Enlever tous les autres tags HTML
                .replaceAll("&nbsp;", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&quot;", "\"")
                .replaceAll("&#39;", "'");

        // Nettoyer les espaces multiples
        text = text.replaceAll("\\n{3,}", "\n\n");
        text = text.replaceAll(" +", " ");
        text = text.trim();

        return text;
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
                .setContentTitle("✅ CV sauvegardé")
                .setContentText(fileName)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Votre CV/résumé a été sauvegardé avec succès.\nAppuyez pour ouvrir le fichier."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Afficher la notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } catch (Exception e) {
            // Si la notification échoue, ce n'est pas grave, le fichier est déjà sauvegardé
            e.printStackTrace();
        }
    }
}