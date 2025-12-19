
 => Créer la classe dans le dossier service : ex : CoursApiGeneration : 3tiwh l chat bach i adapter likom lcode 3la 7sab lbesoin  )
 contenu :

package com.example.test_apiiis_mobile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GitHubAIService {
    public interface AIResponseCallback {
        void onSuccess(String result);
        void onError(String error);
    }

    private static final String ENDPOINT = "https://models.inference.ai.azure.com/chat/completions";
    private static final String MODEL = "Grok-3-Mini";
    private static final String GITHUB_TOKEN = "github_token";

    private final OkHttpClient client = new OkHttpClient();

    public void generateContent(String userMessage, AIResponseCallback callback) {
        try {
            JSONObject jsonBody = createJsonPayload(userMessage);

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

            Request request = new Request.Builder()
                    .url(ENDPOINT)
                    .addHeader("Authorization", "Bearer " + GITHUB_TOKEN)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Erreur réseau : " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseBody = response.body().string();
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            String content = jsonResponse.getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");
                            callback.onSuccess(content);
                        } catch (JSONException e) {
                            callback.onError("Erreur de parsing JSON : " + e.getMessage());
                        }
                    } else {
                        callback.onError("Erreur API (" + response.code() + ") : " + responseBody);
                    }
                }
            });

        } catch (JSONException e) {
            callback.onError("Erreur lors de la création du JSON : " + e.getMessage());
        }
    }

    private JSONObject createJsonPayload(String userMessage) throws JSONException {
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");

        // =================================================================================================
        // 1. CONTRÔLE DU COMPORTEMENT (LE PLUS IMPORTANT)
        // Le message "system" donne des instructions à l'IA sur son rôle et son style.
        // Modifiez cette ligne pour changer la nature de la réponse.
        
        // EXEMPLES :
        // - Pour un traducteur Français -> Anglais : "Tu es un traducteur expert. Traduis le texte suivant en anglais."
        // - Pour un poète : "Réponds à la demande suivante sous la forme d'un court poème."
        // - Pour un assistant qui répond en Français : "Tu es un assistant serviable qui répond toujours en français."
        // =================================================================================================
        systemMessage.put("content", "You are a helpful assistant."); // <-- MODIFIEZ CETTE LIGNE

        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);

        JSONArray messages = new JSONArray();
        messages.put(systemMessage);
        messages.put(userMsg);

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("model", MODEL);
        jsonBody.put("messages", messages);

        // =================================================================================================
        // 2. CONTRÔLE DE LA CRÉATIVITÉ
        // Une valeur proche de 0 (ex: 0.2) rendra les réponses plus prévisibles et factuelles.
        // Une valeur proche de 1.0 ou plus rendra les réponses plus créatives et variées.
        // =================================================================================================
        jsonBody.put("temperature", 1.0); // <-- MODIFIEZ CETTE LIGNE (ex: 0.2 pour des faits, 1.5 pour de la créativité)

        return jsonBody;
    }
}


2==> Remarque : remplacez github_token par votre token GitHub (aller dans Settings → Developer Tools → Token (classic)).
