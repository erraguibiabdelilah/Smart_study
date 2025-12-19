package com.example.smart_study.fragments.exames;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smart_study.R;
import com.example.smart_study.outils.MyPdfExtractor;
import com.example.smart_study.services.generateExams.ExamApiService;
import com.example.smart_study.services.generateExams.ExamQuestionModel;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProcessingFragmentExam extends Fragment {

    private ProgressBar progressBar;
    private TextView percentageText;
    private TextView statusText;
    private Handler handler;
    private int progress = 0;
    private Uri uripdf;
    private String pdfContent;
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
        uripdf = Uri.parse(uristring);
        handler = new Handler(Looper.getMainLooper());
        executorService = Executors.newSingleThreadExecutor();

        startProgressAnimation();
    }

    private void startProgressAnimation() {
        // Lancer l'extraction du PDF dans un thread séparé
        executorService.execute(() -> {
            try {
                // Extraction du texte (peut prendre du temps)
                pdfContent = MyPdfExtractor.extractText(requireContext(), uripdf);
                isExtractionComplete = true;
            } catch (Exception e) {
                e.printStackTrace();
                // Gérer l'erreur
                handler.post(() -> {
                    if (isAdded()) {
                        statusText.setText("❌ Error extracting PDF content");
                    }
                });
            }
        });

        // Animation de progression
        final int totalDuration = 8000; // 6 secondes
        final int steps = 100;
        final int delayPerStep = totalDuration / steps;

        Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (progress <= 100 && isAdded()) {
                    // Mettre à jour la barre de progression
                    progressBar.setProgress(progress);
                    percentageText.setText(progress + "%");
                    updateStatusMessage(progress);

                    progress++;
                    handler.postDelayed(this, delayPerStep);

                } else if (progress > 100 && isAdded()) {
                    // Attendre que l'extraction soit complète avant de continuer
                    if (isExtractionComplete) {
                        generateExams();
                    } else {
                        // Continuer à attendre
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
            statusText.setText("Processing text and preparing Exams...");
        } else {
            statusText.setText("Generating Exams...");
        }
    }

    private void generateExams() {
        if (pdfContent == null || pdfContent.isEmpty()) {
            statusText.setText("❌ No content extracted from PDF");
            return;
        }

        ExamApiService service = new ExamApiService();
        ExamApiService.ExamConfig config = new ExamApiService.ExamConfig()
                .setNumberOfQuestions(20)
                .setNumberOfOptions(4);

        service.generateExam(pdfContent, config, new ExamApiService.ExamCallback() {
            @Override
            public void onSuccess(List<ExamQuestionModel> exams) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (exams != null && !exams.isEmpty()) {
                            navigateToExamFragment(exams);
                        } else {
                            statusText.setText("No exam questions generated.");
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
/*
    private void navigateToExamFragment(List<ExamQuestionModel> exams) {
        ExamesFragment examFragment = new ExamesFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("exams", (Serializable) exams);
        examFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, examFragment)
                .addToBackStack(null)
                .commit();
    }*/

    private void navigateToExamFragment(List<ExamQuestionModel> exams) {
        IntroExamFragment introExamFragment = new IntroExamFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("exams", (Serializable) exams);
        introExamFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, introExamFragment)
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