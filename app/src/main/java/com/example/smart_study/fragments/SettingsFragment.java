package com.example.smart_study.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smart_study.R;
import com.example.smart_study.settings.SettingsAdapter;
import com.example.smart_study.settings.SettingsItem;

import java.util.Arrays;
import java.util.List;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configuration de l'email
        TextView emailText = view.findViewById(R.id.txtEmail);
        emailText.setText("user@example.com");

        // Configuration du RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_settings);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Création de la liste des éléments
        List<SettingsItem> settingsItems = Arrays.asList(
            new SettingsItem("Subscription", R.drawable.ic_star_filled, R.drawable.circle_blue),
            new SettingsItem("Terms & Conditions", R.drawable.ic_description_filled, R.drawable.circle_purple),
            new SettingsItem("Privacy Policy", R.drawable.ic_verified_user_filled, R.drawable.circle_green),
            new SettingsItem("Feedback", R.drawable.ic_chat_filled, R.drawable.circle_orange),
            new SettingsItem("Tell a Friend", R.drawable.ic_share_filled, R.drawable.circle_light_blue),
            new SettingsItem("Log Out", R.drawable.ic_logout_filled, R.drawable.circle_grey),
            new SettingsItem("Delete Account", R.drawable.ic_delete_filled, R.drawable.circle_red)
        );

        // Configuration de l'adapter
        SettingsAdapter adapter = new SettingsAdapter(settingsItems, item -> {
            // Gestion du clic sur un élément
            switch (item.getTitle()) {
                case "Subscription":
                    // Ouvrir l'écran d'abonnement
                    break;
                case "Terms & Conditions":
                    // Ouvrir les conditions générales
                    break;
                case "Privacy Policy":
                    // Ouvrir la politique de confidentialité
                    break;
                case "Feedback":
                    // Ouvrir l'écran de feedback
                    break;
                case "Tell a Friend":
                    // Ouvrir le partage
                    break;
                case "Log Out":
                    // Déconnexion
                    break;
                case "Delete Account":
                    // Supprimer le compte
                    break;
            }
        });

        recyclerView.setAdapter(adapter);
    }
}
