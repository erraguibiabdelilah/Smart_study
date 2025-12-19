package com.example.smart_study.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smart_study.R;
import com.example.smart_study.services.flashCards.FlashCardApiService;


public class QcmFragment extends Fragment {

    private EditText inputMessage;
    private TextView outputText;
    private Button btnSend;

    private FlashCardApiService aiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // 1. On récupère la vue
        View view = inflater.inflate(R.layout.fragment_qcm, container, false);

        // 2. Initialisation des vues
        inputMessage = view.findViewById(R.id.inputMessage);
        outputText = view.findViewById(R.id.outputText);
        btnSend = view.findViewById(R.id.btnSend);

        // 3. Initialisation du service (à adapter selon ton implémentation)
        aiService = new FlashCardApiService(); // <- corrige si besoin

        // 4. Logique du bouton
        btnSend.setOnClickListener(v -> {
            String message = inputMessage.getText().toString().trim();

            if (message.isEmpty()) {
                outputText.setText("Veuillez entrer un message.");
                return;
            }

            outputText.setText("Génération en cours...");


        });

        return view;
    }
}
