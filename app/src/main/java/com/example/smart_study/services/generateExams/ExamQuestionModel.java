package com.example.smart_study.services.generateExams;

import java.io.Serializable;
import java.util.List;

public class ExamQuestionModel implements Serializable {
    private String question;
    private List<String> options;
    private int correctAnswerIndex;
    private int userSelectedAnswer = -1; // -1 signifie non répondu

    public ExamQuestionModel(String question, List<String> options, int correctAnswerIndex) {
        this.question = question;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public int getUserSelectedAnswer() {
        return userSelectedAnswer;
    }

    public void setUserSelectedAnswer(int userSelectedAnswer) {
        this.userSelectedAnswer = userSelectedAnswer;
    }

    public boolean isAnswered() {
        return userSelectedAnswer != -1;
    }

    public boolean isCorrect() {
        return userSelectedAnswer == correctAnswerIndex;
    }
}