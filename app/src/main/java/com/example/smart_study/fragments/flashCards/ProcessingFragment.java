package com.example.smart_study.fragments.flashCards;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smart_study.R;
import com.example.smart_study.outils.MyPdfExtractor;
import com.example.smart_study.services.flashCards.FlashCardApiService;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProcessingFragment extends Fragment {

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
        return inflater.inflate(R.layout.fragment_processing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        percentageText = view.findViewById(R.id.percentageText);
        statusText = view.findViewById(R.id.statusText);
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
        final int totalDuration = 6000; // 4 secondes
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
                        generateFlashCards();
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
            statusText.setText("Processing text and preparing flashcards...");
        } else {
            statusText.setText("Generating flashcards...");
        }
    }
    private void generateFlashCards() {
        if (pdfContent == null || pdfContent.isEmpty()) {
            statusText.setText("❌ No content extracted from PDF");
            return;
        }
        FlashCardApiService service = new FlashCardApiService();
        FlashCardApiService.FlashCardConfig config = new FlashCardApiService.FlashCardConfig()
                .setMaxQuestionWords(20)
                .setMaxAnswerWords(30)
                .setNumberOfCards(15);

        service.generateFlashCards(pdfContent, config, new FlashCardApiService.FlashCardCallback() {
            @Override
            public void onSuccess(List<FlashCardApiService.FlashCard> flashCards) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (flashCards != null && !flashCards.isEmpty()) {
                            navigateToFlashcardFragment(flashCards);
                        } else {
                            statusText.setText("No flashcards generated.");
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

    private void navigateToFlashcardFragment(List<FlashCardApiService.FlashCard> flashCards) {
        FlashcardFragment flashcardFragment = new FlashcardFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("flashCards", (Serializable) flashCards);
        flashcardFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, flashcardFragment)
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
