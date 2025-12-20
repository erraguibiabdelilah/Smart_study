package com.example.smart_study.fragments.resumes;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smart_study.R;
import com.example.smart_study.beans.ResumeModel;
import com.example.smart_study.db.AppDatabase;
import com.example.smart_study.outils.MyPdfExtractor;
import com.example.smart_study.services.resumes.ResumeApiService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProcessingFragmentResume extends Fragment {

    private ProgressBar progressBar;
    private TextView percentageText;
    private TextView statusText;
    private Handler handler;
    private int progress = 0;
    private Uri uripdf;
    private String pdfContent;
    private String pdfFileName;
    private ExecutorService executorService;
    private boolean isExtractionComplete = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_processingexam, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar1);
        percentageText = view.findViewById(R.id.percentageText1);
        statusText = view.findViewById(R.id.statusText1);
        String uristring = getArguments().getString("pdfUri");
        pdfFileName = getArguments().getString("pdfFileName", "Document");
        uripdf = Uri.parse(uristring);
        handler = new Handler(Looper.getMainLooper());
        executorService = Executors.newSingleThreadExecutor();

        startProgressAnimation();
    }

    private void startProgressAnimation() {
        executorService.execute(() -> {
            try {
                pdfContent = MyPdfExtractor.extractText(requireContext(), uripdf);
                isExtractionComplete = true;
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> {
                    if (isAdded()) {
                        statusText.setText("❌ Error extracting PDF content");
                    }
                });
            }
        });

        final int totalDuration = 8000;
        final int steps = 100;
        final int delayPerStep = totalDuration / steps;

        Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (progress <= 100 && isAdded()) {
                    progressBar.setProgress(progress);
                    percentageText.setText(progress + "%");
                    updateStatusMessage(progress);

                    progress++;
                    handler.postDelayed(this, delayPerStep);

                } else if (progress > 100 && isAdded()) {
                    if (isExtractionComplete) {
                        generateResume();
                    } else {
                        statusText.setText("Finalizing extraction...");
                        handler.postDelayed(this, 100);
                    }
                }
            }
        };

        handler.post(progressRunnable);
    }

    private void updateStatusMessage(int progress) {
        if (progress < 30) {
            statusText.setText("Extracting PDF content and analyzing structure...");
        } else if (progress < 70) {
            statusText.setText("Extracting PDF content and analyzing structure...");
        } else if (progress < 100) {
            statusText.setText("Processing text and preparing resume...");
        } else {
            statusText.setText("Generating resume...");
        }
    }

    private void generateResume() {
        if (pdfContent == null || pdfContent.isEmpty()) {
            statusText.setText("❌ No content extracted from PDF");
            return;
        }

        ResumeApiService service = new ResumeApiService();
        ResumeApiService.ResumeConfig config = new ResumeApiService.ResumeConfig()
                .setFormat("professional")
                .setLanguage("french")
                .setTemperature(0.7);

        service.generateResume(pdfContent, config, new ResumeApiService.ResumeCallback() {
            @Override
            public void onSuccess(String resume) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (resume != null && !resume.isEmpty()) {
                            // Save resume to database
                            saveResumeToDatabase(resume);
                            navigateToResumeDisplayFragment(resume);
                        } else {
                            statusText.setText("No resume generated.");
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> statusText.setText("Error: " + error));
                }
            }
        });
    }

    private void saveResumeToDatabase(String resumeContent) {
        new Thread(() -> {
            try {
                // Extract title from resume content (first line or first 50 chars)
                String title = extractTitleFromResume(resumeContent);
                
                ResumeModel resumeModel = new ResumeModel();
                resumeModel.setTitle(title);
                resumeModel.setContent(resumeContent);
                resumeModel.setSourceFileName(pdfFileName);

                AppDatabase db = AppDatabase.getInstance(requireContext());
                long id = db.resumeDao().insert(resumeModel);
                
                Log.d("ResumeSave", "Resume saved with ID: " + id);
            } catch (Exception e) {
                Log.e("ResumeSave", "Error saving resume to database: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private String extractTitleFromResume(String resumeContent) {
        if (resumeContent == null || resumeContent.isEmpty()) {
            return "Résumé - " + pdfFileName;
        }

        // Remove HTML tags
        String cleanContent = resumeContent
                .replaceAll("<[^>]+>", "")
                .replaceAll("&nbsp;", " ")
                .trim();

        // Try to find the first meaningful line (not empty, at least 5 chars)
        String[] lines = cleanContent.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.length() >= 5 && line.length() <= 100) {
                // Use first meaningful line as title
                return line;
            }
        }

        // If no good line found, use first 50 chars
        if (cleanContent.length() > 50) {
            return cleanContent.substring(0, 50).trim() + "...";
        }

        return cleanContent.length() > 0 ? cleanContent : "Résumé - " + pdfFileName;
    }

    private void navigateToResumeDisplayFragment(String resume) {
        ResumeDisplayFragment resumeDisplayFragment = new ResumeDisplayFragment();
        Bundle bundle = new Bundle();
        bundle.putString("resume", resume);
        resumeDisplayFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, resumeDisplayFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}