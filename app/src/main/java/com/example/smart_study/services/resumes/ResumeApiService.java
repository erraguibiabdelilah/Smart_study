package com.example.smart_study.services.resumes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ResumeApiService {

    public static class ResumeConfig {
        private String format = "professional"; // professional, academic, creative
        private String language = "french"; // french, english
        private double temperature = 0.7;
        private boolean includeSummary = true;
        private boolean includeSkills = true;
        private boolean includeExperience = true;
        private boolean includeEducation = true;

        public ResumeConfig setFormat(String format) {
            this.format = format;
            return this;
        }

        public ResumeConfig setLanguage(String language) {
            this.language = language;
            return this;
        }

        public ResumeConfig setTemperature(double temp) {
            this.temperature = temp;
            return this;
        }

        public ResumeConfig setIncludeSummary(boolean include) {
            this.includeSummary = include;
            return this;
        }

        public ResumeConfig setIncludeSkills(boolean include) {
            this.includeSkills = include;
            return this;
        }

        public ResumeConfig setIncludeExperience(boolean include) {
            this.includeExperience = include;
            return this;
        }

        public ResumeConfig setIncludeEducation(boolean include) {
            this.includeEducation = include;
            return this;
        }
    }

    public interface ResumeCallback {
        void onSuccess(String resume);
        void onError(String error);
    }

    private static final String ENDPOINT = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODEL = "mistralai/mistral-7b-instruct";
    private static final String OPENROUTER_API_KEY = "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".trim();

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .build();

    /**
     * Génère un CV/résumé à partir d'informations fournies
     * @param userInfo Les informations de l'utilisateur (expérience, éducation, compétences, etc.)
     * @param config Configuration pour personnaliser le CV
     * @param callback Callback pour recevoir le résultat
     */
    public void generateResume(String userInfo, ResumeConfig config, ResumeCallback callback) {
        try {
            JSONObject jsonBody = createResumeJsonPayload(userInfo, config);

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

            Request request = new Request.Builder()
                    .url(ENDPOINT)
                    .addHeader("Authorization", "Bearer " + OPENROUTER_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("HTTP-Referer", "https://smartstudy.app")
                    .addHeader("X-Title", "Smart Study App")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Erreur réseau : " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    handleApiResponse(response, callback);
                }
            });
        } catch (JSONException e) {
            callback.onError("Erreur lors de la création du JSON: " + e.getMessage());
        }
    }

    /**
     * Version simplifiée avec configuration par défaut
     */
    public void generateResume(String userInfo, ResumeCallback callback) {
        generateResume(userInfo, new ResumeConfig(), callback);
    }

    private void handleApiResponse(Response response, ResumeCallback callback) throws IOException {
        final String responseBody = response.body() != null ? response.body().string() : null;

        // Validate response body
        if (responseBody == null || responseBody.trim().isEmpty()) {
            callback.onError("Erreur: Réponse API vide ou nulle");
            return;
        }

        if (!response.isSuccessful()) {
            callback.onError("Erreur API (" + response.code() + ") : " + responseBody);
            return;
        }

        try {
            // Validate JSON structure
            if (responseBody.trim().isEmpty()) {
                callback.onError("Erreur: Réponse JSON vide");
                return;
            }

            JSONObject jsonResponse = new JSONObject(responseBody);

            // Validate choices array exists
            if (!jsonResponse.has("choices")) {
                callback.onError("Erreur: Structure JSON invalide - 'choices' manquant. Réponse: " + (responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody));
                return;
            }

            JSONArray choices = jsonResponse.getJSONArray("choices");
            if (choices.length() == 0) {
                callback.onError("Erreur: Aucun choix dans la réponse");
                return;
            }

            JSONObject firstChoice = choices.getJSONObject(0);
            if (!firstChoice.has("message")) {
                callback.onError("Erreur: Structure JSON invalide - 'message' manquant");
                return;
            }

            JSONObject message = firstChoice.getJSONObject("message");
            if (!message.has("content")) {
                callback.onError("Erreur: Structure JSON invalide - 'content' manquant");
                return;
            }

            String content = message.getString("content");
            if (content == null || content.trim().isEmpty()) {
                callback.onError("Erreur: Contenu de la réponse vide");
                return;
            }

            callback.onSuccess(content);
        } catch (JSONException e) {
            callback.onError("Erreur de parsing JSON: " + e.getMessage() + " | Réponse: " + (responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody));
        } catch (Exception e) {
            callback.onError("Erreur inattendue: " + e.getMessage());
        }
    }

    private JSONObject createResumeJsonPayload(String userInfo, ResumeConfig config) throws JSONException {

        String systemPrompt =
                "Tu es un expert pédagogique spécialisé dans la synthèse de cours pour étudiants. "
                        + "Ton objectif est de produire un résumé de cours clair, simple et facile à lire sur mobile. "

                        + "RÈGLES IMPORTANTES : "
                        + "- Utilise des phrases courtes et simples. "
                        + "- Évite les longs paragraphes. "
                        + "- Explique de manière progressive et pédagogique. "
                        + "- Utilise des listes quand c’est possible. "

                        + "STRUCTURE OBLIGATOIRE DU RÉSUMÉ : "
                        + "1) Titre du cours, "
                        + "2) Objectifs du cours, "
                        + "3) Concepts clés (sections claires), "
                        + "4) Points importants à retenir, "
                        + "5) Résumé final très court. "

                        + "FORMAT HTML MOBILE-FRIENDLY STRICT : "
                        + "Utilise uniquement : <div>, <h2>, <h3>, <p>, <ul>, <li>, <strong>. "

                        + "STYLE INLINE OBLIGATOIRE : "
                        + "- font-family: Arial, sans-serif; "
                        + "- font-size: 15px; "
                        + "- line-height: 1.6; "
                        + "- padding: 16px; "
                        + "- max-width: 600px; "
                        + "- margin: auto; "
                        + "- background: #ffffff; "
                        + "- color: #222222. "

                        + "CONTENEUR PRINCIPAL OBLIGATOIRE : "
                        + "<div style='padding:16px;font-family:Arial,sans-serif;max-width:600px;margin:auto;color:#222;'> "

                        + "Le résultat doit être clair, structuré, pédagogique et parfait pour une application mobile.";

        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);

        String userPrompt =
                "Voici le contenu du cours. "
                        + "Génère un résumé clair, structuré et facile à mémoriser "
                        + "en respectant STRICTEMENT les règles ci-dessus :\n\n"
                        + userInfo;

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", userPrompt);

        JSONArray messages = new JSONArray();
        messages.put(systemMessage);
        messages.put(userMessage);

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("model", MODEL);
        jsonBody.put("messages", messages);
        jsonBody.put("temperature", config.temperature);
        jsonBody.put("stream", false);

        return jsonBody;
    }

}
