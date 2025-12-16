package com.example.smart_study.services.generateExams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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

public class ExamApiService {

    public static class ExamConfig {
        private int numberOfQuestions = 20;
        private int numberOfOptions = 4;
        private double temperature = 0.7;

        public ExamConfig setNumberOfQuestions(int number) { this.numberOfQuestions = number; return this; }
        public ExamConfig setNumberOfOptions(int number) { this.numberOfOptions = number; return this; }
        public ExamConfig setTemperature(double temp) { this.temperature = temp; return this; }
    }

    public interface ExamCallback {
        void onSuccess(List<ExamQuestionModel> questions);
        void onError(String error);
    }

    private static final String ENDPOINT = "https://models.inference.ai.azure.com/chat/completions";
    private static final String MODEL = "gpt-4.1";
    private static final String GITHUB_TOKEN = "xxxxxx"; // Remplacez par votre token

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .build();

    public void generateExam(String text, ExamConfig config, ExamCallback callback) {
        try {
            JSONObject jsonBody = createExamJsonPayload(text, config);

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
                    handleApiResponse(response, callback);
                }
            });
        } catch (JSONException e) {
            callback.onError("Erreur lors de la création du JSON: " + e.getMessage());
        }
    }

    private void handleApiResponse(Response response, ExamCallback callback) throws IOException {
        final String responseBody = response.body().string();
        if (!response.isSuccessful()) {
            callback.onError("Erreur API (" + response.code() + ") : " + responseBody);
            return;
        }

        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            String content = jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            List<ExamQuestionModel> questions = parseExamQuestions(content);
            callback.onSuccess(questions);
        } catch (JSONException e) {
            callback.onError("Erreur de parsing JSON: " + e.getMessage());
        }
    }

    private JSONObject createExamJsonPayload(String text, ExamConfig config) throws JSONException {
        String systemPrompt = String.format(
                "Tu es un expert en création d\'examens QCM. Génère exactement %d questions au format JSON strict à partir du texte. " +
                        "Chaque question doit avoir exactement %d options et un index de réponse correcte. FORMAT DE SORTIE OBLIGATOIRE: " +
                        "{ \"questions\": [ { \"question\": \"Question?\", \"options\": [\"Opt 1\", \"Opt 2\"], \"correctAnswerIndex\": 0 } ] }",
                config.numberOfQuestions, config.numberOfOptions
        );

        JSONObject systemMessage = new JSONObject().put("role", "system").put("content", systemPrompt);
        JSONObject userMessage = new JSONObject().put("role", "user").put("content", "Génère un examen QCM à partir de ce texte :\n\n" + text);

        JSONArray messages = new JSONArray().put(systemMessage).put(userMessage);

        return new JSONObject()
                .put("model", MODEL)
                .put("messages", messages)
                .put("temperature", config.temperature);
    }

    private List<ExamQuestionModel> parseExamQuestions(String jsonContent) throws JSONException {
        List<ExamQuestionModel> questions = new ArrayList<>();
        JSONObject jsonResponse = new JSONObject(cleanJsonString(jsonContent));
        JSONArray questionsArray = jsonResponse.getJSONArray("questions");

        for (int i = 0; i < questionsArray.length(); i++) {
            JSONObject q = questionsArray.getJSONObject(i);
            String questionText = q.getString("question");
            JSONArray optionsArray = q.getJSONArray("options");
            int correctIndex = q.getInt("correctAnswerIndex");

            List<String> options = new ArrayList<>();
            for (int j = 0; j < optionsArray.length(); j++) {
                options.add(optionsArray.getString(j));
            }
            questions.add(new ExamQuestionModel(questionText, options, correctIndex));
        }
        return questions;
    }

    private String cleanJsonString(String jsonContent) {
        String cleaned = jsonContent.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7, cleaned.lastIndexOf("```"));
        }
        return cleaned;
    }
}
