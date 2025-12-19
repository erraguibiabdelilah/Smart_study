package com.example.smart_study.beans;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.smart_study.dao.Converters;
import com.example.smart_study.services.generateExams.ExamQuestionModel;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity(tableName = "exam_history")
@TypeConverters(Converters.class)
public class ExamHistoryModel implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String examTitle;
    private Date completedDate;
    private int totalQuestions;
    private int correctAnswers;
    private double scorePercentage;
    private String timeUsed;
    private long timeRemainingMillis;
    private boolean wasTimeExpired;

    // Liste des questions avec les réponses
    private List<ExamQuestionModel> questions;

    // Constructeur
    public ExamHistoryModel() {
        this.completedDate = new Date();
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }

    public Date getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(Date completedDate) {
        this.completedDate = completedDate;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public double getScorePercentage() {
        return scorePercentage;
    }

    public void setScorePercentage(double scorePercentage) {
        this.scorePercentage = scorePercentage;
    }

    public String getTimeUsed() {
        return timeUsed;
    }

    public void setTimeUsed(String timeUsed) {
        this.timeUsed = timeUsed;
    }

    public long getTimeRemainingMillis() {
        return timeRemainingMillis;
    }

    public void setTimeRemainingMillis(long timeRemainingMillis) {
        this.timeRemainingMillis = timeRemainingMillis;
    }

    public boolean isWasTimeExpired() {
        return wasTimeExpired;
    }

    public void setWasTimeExpired(boolean wasTimeExpired) {
        this.wasTimeExpired = wasTimeExpired;
    }

    public List<ExamQuestionModel> getQuestions() {
        return questions;
    }

    public void setQuestions(List<ExamQuestionModel> questions) {
        this.questions = questions;
    }


    public String getStatusMessage() {
        if (scorePercentage >= 80) {
            return "Excellent";
        } else if (scorePercentage >= 60) {
            return "Bien";
        } else if (scorePercentage >= 40) {
            return "Passable";
        } else {
            return "À améliorer";
        }
    }
}