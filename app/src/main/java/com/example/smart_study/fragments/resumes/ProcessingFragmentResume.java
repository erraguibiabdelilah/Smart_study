package com.example.smart_study.fragments.resumes;

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