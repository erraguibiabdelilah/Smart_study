package com.example.smart_study.fragments;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smart_study.R;
import com.example.smart_study.beans.CourseHistory;
import com.example.smart_study.db.AppDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CoursesFragment extends Fragment {

    private EditText etCourseName, etObjectives;
    private SeekBar seekbarLevel;
    private Button btnGenerate, btnDownload, enregistrerBtn;
    private ProgressBar progressLoading;

    private String savedCourse;
    private String savedDescription;

    private final String OPENROUTER_API_KEY = "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";

    private ActivityResultLauncher<Intent> savePdfLauncher;
    private ActivityResultLauncher<Intent> downloadAndSavePdfLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_courses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressLoading = view.findViewById(R.id.progress_loading);
        etCourseName = view.findViewById(R.id.et_course_name);
        etObjectives = view.findViewById(R.id.et_objectives);
        seekbarLevel = view.findViewById(R.id.seekbar_level);
        btnGenerate = view.findViewById(R.id.btn_generate_course);
        btnDownload = view.findViewById(R.id.btn_download_course);
        enregistrerBtn = view.findViewById(R.id.enregistrerBtn);
        btnDownload.setVisibility(View.GONE); // Hide initially
        enregistrerBtn.setVisibility(View.GONE);

        savePdfLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        generatePdf(uri);
                    }
                }
        );

        downloadAndSavePdfLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        generatePdf(uri);
                        saveCourseToHistory(uri);
                    }
                }
        );

        btnGenerate.setOnClickListener(v -> generateCourse());

        btnDownload.setOnClickListener(v -> {
            if (savedDescription != null && !savedDescription.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.setType("application/pdf");
                intent.putExtra(Intent.EXTRA_TITLE, savedCourse + ".pdf");
                savePdfLauncher.launch(intent);
            } else {
                Toast.makeText(requireContext(), "Aucun contenu à télécharger", Toast.LENGTH_SHORT).show();
            }
        });

        enregistrerBtn.setOnClickListener(v -> {
            if (savedDescription != null && !savedDescription.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.setType("application/pdf");
                intent.putExtra(Intent.EXTRA_TITLE, savedCourse + ".pdf");
                downloadAndSavePdfLauncher.launch(intent);
            } else {
                Toast.makeText(requireContext(), "Aucun contenu à enregistrer", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveCourseToHistory(Uri pdfUri) {
        if (savedCourse == null || pdfUri == null) {
            Toast.makeText(getContext(), "Error: No course data to save.", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(requireContext());
                CourseHistory courseHistory = new CourseHistory(savedCourse, pdfUri.toString(), new Date());
                db.courseHistoryDao().insert(courseHistory);
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Course saved to history.", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to save course.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void generateCourse() {
        String courseName = etCourseName.getText().toString().trim();
        String objectives = etObjectives.getText().toString().trim();
        String level = getLevelFromSeekBar(seekbarLevel.getProgress());

        if (courseName.isEmpty()) {
            etCourseName.setError("Le nom du cours est requis");
            return;
        }

        if (objectives.isEmpty()) {
            etObjectives.setError("La description est requise");
            return;
        }

        savedCourse = courseName;

        progressLoading.setVisibility(View.VISIBLE);
        btnGenerate.setEnabled(false);
        btnGenerate.setText("Génération en cours...");

        fetchOpenRouterContent(courseName, level, objectives);
    }

    private String getLevelFromSeekBar(int progress) {
        switch (progress) {
            case 0: return "Débutant";
            case 1: return "Intermédiaire";
            case 2: return "Avancé";
            case 3: return "Expert";
            default: return "Intermédiaire";
        }
    }

    private void fetchOpenRouterContent(String course, String level, String description) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "gpt-4o-mini");

            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content",
                    "Provide a detailed and structured explanation about the topic \"" + course + "\". " +
                            "Focus specifically on: " + description + ". " +
                            "The explanation should match a \"" + level + "\" level. " +
                            "Use **bold headings** and bullet points where appropriate. " +
                            "Make it long enough to fill about 4 PDF pages. " +
                            "Do not include greetings, metadata, or unrelated information."
            );

            messages.put(userMessage);
            jsonBody.put("messages", messages);

            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url("https://openrouter.ai/api/v1/chat/completions")
                    .header("Authorization", "Bearer " + OPENROUTER_API_KEY)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    requireActivity().runOnUiThread(() -> resetButton("Erreur réseau"));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        requireActivity().runOnUiThread(() -> resetButton("Erreur API"));
                        return;
                    }

                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);

                        savedDescription = jsonObject
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content")
                                .replace("**", "")
                                .replace("#", "")
                                .replaceAll("^-\\s*", "")
                                .replaceAll("^\\*\\s*", "")
                                .replaceAll("(\\r?\\n){2,}", "\n")
                                .trim();

                        requireActivity().runOnUiThread(() -> {
                            progressLoading.setVisibility(View.GONE);
                            btnGenerate.setEnabled(true);
                            btnGenerate.setText("Générer le cours");
                            btnDownload.setVisibility(View.VISIBLE);
                            enregistrerBtn.setVisibility(View.VISIBLE);
                            Toast.makeText(requireContext(), "Cours généré, vous pouvez maintenant télécharger ou enregistrer.", Toast.LENGTH_LONG).show();
                        });

                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() -> resetButton("Erreur parsing"));
                    }
                }
            });

        } catch (Exception e) {
            resetButton("Erreur interne");
        }
    }

    private void generatePdf(Uri uri) {
        try {
            OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);

            PdfDocument pdfDocument = new PdfDocument();
            Paint paint = new Paint();

            int pageWidth = 595;
            int pageHeight = 842;
            int margin = 40;
            int lineSpacing = 20;

            String[] paragraphs = savedDescription.split("\n");
            int y = 50;
            int pageNumber = 1;

            PdfDocument.Page page = pdfDocument.startPage(
                    new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            );
            Canvas canvas = page.getCanvas();

            paint.setTextSize(22);
            paint.setFakeBoldText(true);
            canvas.drawText(savedCourse, margin, y, paint);
            y += 40;

            paint.setTextSize(14);
            paint.setFakeBoldText(false);

            for (String para : paragraphs) {
                para = para.trim();
                if (para.isEmpty()) continue;

                if (para.length() < 60 && !para.endsWith(".")) {
                    paint.setTextSize(18);
                    paint.setFakeBoldText(true);
                    y += 20;
                    canvas.drawText(para, margin, y, paint);
                    y += 30;
                    paint.setTextSize(14);
                    paint.setFakeBoldText(false);
                    continue;
                }

                if (para.startsWith("-") || para.startsWith("*")) {
                    para = "• " + para.substring(1).trim();
                }

                String[] words = para.split("\\s+");
                StringBuilder line = new StringBuilder();

                for (String word : words) {
                    String testLine = line + (line.length() == 0 ? "" : " ") + word;
                    if (paint.measureText(testLine) > pageWidth - 2 * margin) {
                        canvas.drawText(line.toString(), margin, y, paint);
                        y += lineSpacing;
                        line = new StringBuilder(word);

                        if (y > pageHeight - margin) {
                            pdfDocument.finishPage(page);
                            pageNumber++;
                            page = pdfDocument.startPage(
                                    new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                            );
                            canvas = page.getCanvas();
                            y = margin;
                        }
                    } else {
                        line = new StringBuilder(testLine);
                    }
                }

                canvas.drawText(line.toString(), margin, y, paint);
                y += lineSpacing + 10;
            }

            pdfDocument.finishPage(page);
            pdfDocument.writeTo(outputStream);
            pdfDocument.close();
            outputStream.close();

            Toast.makeText(requireContext(), "PDF enregistré ✔", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Erreur PDF", Toast.LENGTH_LONG).show();
        }
    }

    private void resetButton(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        progressLoading.setVisibility(View.GONE);
        btnGenerate.setEnabled(true);
        btnGenerate.setText("Générer le cours");
    }
}
