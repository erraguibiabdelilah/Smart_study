package com.example.smart_study.qsm.model;

import java.util.List;

public class QsmQuestion {

    private String question;
    private String explanation;
    private List<String> choices;
    private int correctIndex;

    // ✅ Empty constructor (REQUIRED for safety)
    public QsmQuestion() {
    }

    // ✅ Constructor USED by Repository
    public QsmQuestion(String question,
                       String explanation,
                       List<String> choices,
                       int correctIndex) {
        this.question = question;
        this.explanation = explanation;
        this.choices = choices;
        this.correctIndex = correctIndex;
    }

    // ✅ Getters
    public String getQuestion() {
        return question;
    }

    public String getExplanation() {
        return explanation;
    }

    public List<String> getChoices() {
        return choices;
    }

    public int getCorrectIndex() {
        return correctIndex;
    }
}
