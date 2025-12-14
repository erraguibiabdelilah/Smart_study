package com.example.smart_study.services.flashCards;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FlashCardApiService {

    // Classe pour représenter une flashcard
    public static class FlashCard implements Serializable {
        private String question;
        private String answer;

        public FlashCard(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }

        public String getQuestion() {
            return question;
        }

        public String getAnswer() {
            return answer;
        }
    }

    // Classe pour configurer la génération
    public static class FlashCardConfig {
        private int maxQuestionWords = 20;  // Nombre max de mots pour la question
        private int maxAnswerWords = 40;    // Nombre max de mots pour la réponse
        private int numberOfCards = 15;       // Nombre de flashcards à générer
        private double temperature = 0.9;    // Créativité (0.2-1.5)

        public FlashCardConfig setMaxQuestionWords(int words) {
            this.maxQuestionWords = words;
            return this;
        }

        public FlashCardConfig setMaxAnswerWords(int words) {
            this.maxAnswerWords = words;
            return this;
        }

        public FlashCardConfig setNumberOfCards(int number) {
            this.numberOfCards = number;
            return this;
        }

        public FlashCardConfig setTemperature(double temp) {
            this.temperature = temp;
            return this;
        }
    }

    public interface FlashCardCallback {
        void onSuccess(List<FlashCard> flashCards);
        void onError(String error);
    }

    private static final String ENDPOINT = "https://models.inference.ai.azure.com/chat/completions";
    private static final String MODEL = "Grok-3-Mini";
    private static final String GITHUB_TOKEN = "xxxxxxxxxxxxxxxxxxxxxxx";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    /**
     * Génère des flashcards à partir d'un texte avec configuration personnalisée
     * @param text Le texte source pour générer les flashcards
     * @param config Configuration pour contrôler la sortie
     * @param callback Callback pour recevoir les résultats
     */
    public void generateFlashCards(String text, FlashCardConfig config, FlashCardCallback callback) {
        try {
            JSONObject jsonBody = createFlashCardJsonPayload(text, config);

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

                            List<FlashCard> flashCards = parseFlashCards(content);
                            callback.onSuccess(flashCards);
                        } catch (JSONException e) {
                            callback.onError("Erreur de parsing JSON: " + e.getMessage());
                        }
                    } else {
                        callback.onError("Erreur API (" + response.code() + ") : " + responseBody);
                    }
                }
            });

        } catch (JSONException e) {
            callback.onError("Erreur lors de la création du JSON: " + e.getMessage());
        }
    }

    /**
     * Version simplifiée avec configuration par défaut
     */
    public void generateFlashCards(String text, FlashCardCallback callback) {
        generateFlashCards(text, new FlashCardConfig(), callback);
    }

    private JSONObject createFlashCardJsonPayload(String text, FlashCardConfig config) throws JSONException {
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");

        // Prompt système optimisé pour générer des flashcards structurées
        String systemPrompt = "Tu es un expert en création de flashcards éducatives. " +
                "Tu dois générer des flashcards au format JSON strict à partir du texte fourni. " +
                "Respecte ABSOLUMENT les contraintes suivantes :\n" +
                "- Chaque question doit contenir MAXIMUM " + config.maxQuestionWords + " mots\n" +
                "- Chaque réponse doit contenir MAXIMUM " + config.maxAnswerWords + " mots\n" +
                "- Génère exactement " + config.numberOfCards + " flashcards\n" +
                "- Les questions doivent être claires et précises\n" +
                "- Les réponses doivent être concises et informatives\n\n" +
                "FORMAT DE SORTIE OBLIGATOIRE (JSON uniquement, sans texte supplémentaire) :\n" +
                "{\n" +
                "  \"flashcards\": [\n" +
                "    {\n" +
                "      \"question\": \"Question ici?\",\n" +
                "      \"answer\": \"Réponse ici.\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        systemMessage.put("content", systemPrompt);

        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", "Génère des flashcards à partir de ce texte :\n\n" + text);

        JSONArray messages = new JSONArray();
        messages.put(systemMessage);
        messages.put(userMsg);

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("model", MODEL);
        jsonBody.put("messages", messages);
        jsonBody.put("temperature", config.temperature);

        return jsonBody;
    }

    /**
     * Parse la réponse de l'API pour extraire les flashcards
     */
    private List<FlashCard> parseFlashCards(String jsonContent) throws JSONException {
        List<FlashCard> flashCards = new ArrayList<>();

        // Nettoyer le contenu si l'IA a ajouté des balises markdown
        String cleanedContent = jsonContent.trim();
        if (cleanedContent.startsWith("```json")) {
            cleanedContent = cleanedContent.substring(7);
        }
        if (cleanedContent.startsWith("```")) {
            cleanedContent = cleanedContent.substring(3);
        }
        if (cleanedContent.endsWith("```")) {
            cleanedContent = cleanedContent.substring(0, cleanedContent.length() - 3);
        }
        cleanedContent = cleanedContent.trim();

        JSONObject jsonResponse = new JSONObject(cleanedContent);
        JSONArray flashcardsArray = jsonResponse.getJSONArray("flashcards");

        for (int i = 0; i < flashcardsArray.length(); i++) {
            JSONObject card = flashcardsArray.getJSONObject(i);
            String question = card.getString("question");
            String answer = card.getString("answer");
            flashCards.add(new FlashCard(question, answer));
        }

        return flashCards;
    }
}
