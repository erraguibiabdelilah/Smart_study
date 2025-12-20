package com.example.smart_study.qsm.service;

import okhttp3.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class QcmApiService {

    public interface QcmCallback {
        void onSuccess(String qcmJson);
        void onError(String error);
    }

    private static final String ENDPOINT =
            "https://openrouter.ai/api/v1/chat/completions";

    private static final String MODEL =
            "mistralai/mistral-7b-instruct";

    // ⚠️ IMPORTANT : DO NOT SHIP TOKEN IN FINAL PROJECT
    private static final String API_KEY =
            "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";

    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    // =========================================================

    public void generateQcm(String text, QcmCallback callback) {

        try {
            JSONObject payload = buildPayload(text);

            Request request = new Request.Builder()
                    .url(ENDPOINT)
                    .post(RequestBody.create(payload.toString(), JSON))
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Erreur réseau ❌");
                }

                @Override
                public void onResponse(Call call, Response response)
                        throws IOException {

                    if (!response.isSuccessful()) {
                        callback.onError("Erreur API ❌ " + response.code());
                        return;
                    }

                    String body = response.body() != null
                            ? response.body().string()
                            : "";

                    try {
                        JSONObject root = new JSONObject(body);
                        JSONArray choices = root.getJSONArray("choices");

                        if (choices.length() == 0) {
                            callback.onError("Réponse IA vide ❌");
                            return;
                        }

                        JSONObject message =
                                choices.getJSONObject(0)
                                        .getJSONObject("message");

                        String content = message.getString("content");

                        // 🔥 CLEAN JSON EXTRACTION
                        content = content
                                .replace("```json", "")
                                .replace("```", "")
                                .trim();

                        // Validate JSON
                        new JSONObject(content);

                        callback.onSuccess(content);

                    } catch (Exception e) {
                        callback.onError("JSON invalide ❌");
                    }
                }
            });

        } catch (Exception e) {
            callback.onError("Erreur interne ❌");
        }
    }

    // =========================================================

    private JSONObject buildPayload(String text) throws Exception {

        JSONArray messages = new JSONArray();

        // SYSTEM PROMPT
        JSONObject system = new JSONObject();
        system.put("role", "system");
        system.put("content",
                "Tu es un générateur d'examens QCM.\n" +
                        "RÈGLES STRICTES:\n" +
                        "- Génère EXACTEMENT 20 questions\n" +
                        "- 4 choix par question\n" +
                        "- Une seule bonne réponse\n" +
                        "- Répond UNIQUEMENT en JSON valide\n" +
                        "- AUCUN texte hors JSON\n\n" +
                        "FORMAT OBLIGATOIRE:\n" +
                        "{\n" +
                        "  \"questions\": [\"Q1\", \"Q2\", ...],\n" +
                        "  \"options\": [\n" +
                        "     [\"A\",\"B\",\"C\",\"D\"],\n" +
                        "     ...\n" +
                        "  ],\n" +
                        "  \"answers\": [0,1,2,3,...]\n" +
                        "}");

        // USER PROMPT
        JSONObject user = new JSONObject();
        user.put("role", "user");
        user.put("content",
                "Crée un examen QCM à partir du texte suivant:\n\n"
                        + text);

        messages.put(system);
        messages.put(user);

        JSONObject json = new JSONObject();
        json.put("model", MODEL);
        json.put("messages", messages);
        json.put("temperature", 0.2);
        json.put("stream", false);

        return json;
    }
}
