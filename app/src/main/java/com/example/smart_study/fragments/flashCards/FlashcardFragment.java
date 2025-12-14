package com.example.smart_study.fragments.flashCards;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.smart_study.R;
import com.example.smart_study.services.flashCards.FlashCardApiService;

import java.util.List;

public class FlashcardFragment extends Fragment {
    private CardView cardFront;
    private CardView cardBack;
    private boolean isShowingFront = true;

    // Variables pour afficher les données
    private TextView questionText;
    private TextView answerText;
    private TextView cardCounter;

    // Boutons de navigation
    private Button btnPrevious;
    private Button btnNext;

    // Data
    private List<FlashCardApiService.FlashCard> flashCardsList;
    private int currentCardIndex = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_flashcard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialiser les vues
        cardFront = view.findViewById(R.id.card_front);
        cardBack = view.findViewById(R.id.card_back);
        questionText = view.findViewById(R.id.question_text);
        answerText = view.findViewById(R.id.answer_text);
        cardCounter = view.findViewById(R.id.count);
        btnPrevious = view.findViewById(R.id.btn_previous);
        btnNext = view.findViewById(R.id.btn_next);

        // Préparer les cartes pour l'animation 3D
        float scale = getResources().getDisplayMetrics().density;
        cardFront.setCameraDistance(8000 * scale);
        cardBack.setCameraDistance(8000 * scale);

        // Initialiser la face arrière (rotation de 180°)
        cardBack.setRotationY(180f);

        // Ajouter les listeners de clic pour le flip
        cardFront.setOnClickListener(v -> flipCard());
        cardBack.setOnClickListener(v -> flipCard());

        // Ajouter les listeners pour la navigation
        btnPrevious.setOnClickListener(v -> previousCard());
        btnNext.setOnClickListener(v -> nextCard());

        // Charger les données des flashcards et mettre à jour les boutons
        loadFlashCardsFromArguments();
        updateNavigationButtons();
    }

    private void loadFlashCardsFromArguments() {
        if (getArguments() != null && getArguments().containsKey("flashCards")) {
            flashCardsList = (List<FlashCardApiService.FlashCard>) getArguments().getSerializable("flashCards");

            if (flashCardsList != null && !flashCardsList.isEmpty()) {
                displayCurrentCard();
                showToast("✓ " + flashCardsList.size() + " flashcards prêtes");
            } else {
                showErrorState("Aucune flashcard n'a été générée.");
            }
        } else {
            showErrorState("Erreur: Impossible de charger les flashcards.");
        }
    }

    private void flipCard() {
        if (isShowingFront) {
            // Flip de la face avant vers la face arrière
            cardFront.animate()
                    .rotationY(90f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        cardFront.setVisibility(View.GONE);
                        cardBack.setVisibility(View.VISIBLE);
                        cardBack.setRotationY(270f);
                        cardBack.animate()
                                .rotationY(360f)
                                .setDuration(200)
                                .start();
                    })
                    .start();
        } else {
            // Flip de la face arrière vers la face avant
            cardBack.animate()
                    .rotationY(270f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        cardBack.setVisibility(View.GONE);
                        cardFront.setVisibility(View.VISIBLE);
                        cardFront.setRotationY(90f);
                        cardFront.animate()
                                .rotationY(0f)
                                .setDuration(200)
                                .start();
                    })
                    .start();
        }
        isShowingFront = !isShowingFront;
    }

    private void displayCurrentCard() {
        if (flashCardsList != null && currentCardIndex < flashCardsList.size()) {
            FlashCardApiService.FlashCard card = flashCardsList.get(currentCardIndex);
            questionText.setText(card.getQuestion());
            answerText.setText(card.getAnswer());

            // Mettre à jour le compteur
            cardCounter.setText("Carte " + (currentCardIndex + 1) + " / " + flashCardsList.size());

            // S'assurer que la carte est sur la face avant
            if (!isShowingFront) {
                cardBack.setVisibility(View.GONE);
                cardFront.setVisibility(View.VISIBLE);
                cardFront.setRotationY(0f);
                cardBack.setRotationY(180f);
                isShowingFront = true;
            }

            updateNavigationButtons();
        }
    }

    private void updateNavigationButtons() {
        boolean hasCards = flashCardsList != null && !flashCardsList.isEmpty();
        btnPrevious.setEnabled(hasCards && currentCardIndex > 0);
        btnNext.setEnabled(hasCards && currentCardIndex < flashCardsList.size() - 1);
    }

    public void nextCard() {
        if (flashCardsList != null && currentCardIndex < flashCardsList.size() - 1) {
            currentCardIndex++;
            displayCurrentCard();
        } else {
            showToast("Dernière carte atteinte");
        }
    }

    public void previousCard() {
        if (currentCardIndex > 0) {
            currentCardIndex--;
            displayCurrentCard();
        } else {
            showToast("Première carte atteinte");
        }
    }

    private void showErrorState(String message) {
        questionText.setText("Erreur");
        answerText.setText(message);
        cardCounter.setText("0 / 0");
        showToast(message);
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
