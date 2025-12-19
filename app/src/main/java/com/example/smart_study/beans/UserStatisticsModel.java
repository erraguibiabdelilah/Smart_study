package com.example.smart_study.beans;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "user_statistics")
public class UserStatisticsModel implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    // Statistiques générales
    private int totalExamsCompleted;
    private int totalCoursesGenerated;
    private int totalResumesGenerated;
    private int totalFlashcardsGenerated;
    private int totalStudyHours; // en minutes

    // Statistiques de performance
    private double averageExamScore;
    private int bestExamScore;
    private int totalCorrectAnswers;
    private int totalQuestionsAnswered;

    // Statistiques temporelles
    private Date lastStudyDate;
    private int currentStreak; // jours consécutifs d'étude
    private int longestStreak;
    private int studyDaysThisWeek;
    private int studyDaysThisMonth;

    // Statistiques par matière/sujet
    private String favoriteSubject;
    private String weakestSubject;

    // Niveau d'apprentissage (basé sur les performances)
    private String learningLevel; // "Débutant", "Intermédiaire", "Avancé", "Expert"

    // Constructeur
    public UserStatisticsModel() {
        this.totalExamsCompleted = 0;
        this.totalCoursesGenerated = 0;
        this.totalResumesGenerated = 0;
        this.totalFlashcardsGenerated = 0;
        this.totalStudyHours = 0;
        this.averageExamScore = 0.0;
        this.bestExamScore = 0;
        this.totalCorrectAnswers = 0;
        this.totalQuestionsAnswered = 0;
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.studyDaysThisWeek = 0;
        this.studyDaysThisMonth = 0;
        this.learningLevel = "Débutant";
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTotalExamsCompleted() {
        return totalExamsCompleted;
    }

    public void setTotalExamsCompleted(int totalExamsCompleted) {
        this.totalExamsCompleted = totalExamsCompleted;
    }

    public int getTotalCoursesGenerated() {
        return totalCoursesGenerated;
    }

    public void setTotalCoursesGenerated(int totalCoursesGenerated) {
        this.totalCoursesGenerated = totalCoursesGenerated;
    }

    public int getTotalResumesGenerated() {
        return totalResumesGenerated;
    }

    public void setTotalResumesGenerated(int totalResumesGenerated) {
        this.totalResumesGenerated = totalResumesGenerated;
    }

    public int getTotalFlashcardsGenerated() {
        return totalFlashcardsGenerated;
    }

    public void setTotalFlashcardsGenerated(int totalFlashcardsGenerated) {
        this.totalFlashcardsGenerated = totalFlashcardsGenerated;
    }

    public int getTotalStudyHours() {
        return totalStudyHours;
    }

    public void setTotalStudyHours(int totalStudyHours) {
        this.totalStudyHours = totalStudyHours;
    }

    public double getAverageExamScore() {
        return averageExamScore;
    }

    public void setAverageExamScore(double averageExamScore) {
        this.averageExamScore = averageExamScore;
    }

    public int getBestExamScore() {
        return bestExamScore;
    }

    public void setBestExamScore(int bestExamScore) {
        this.bestExamScore = bestExamScore;
    }

    public int getTotalCorrectAnswers() {
        return totalCorrectAnswers;
    }

    public void setTotalCorrectAnswers(int totalCorrectAnswers) {
        this.totalCorrectAnswers = totalCorrectAnswers;
    }

    public int getTotalQuestionsAnswered() {
        return totalQuestionsAnswered;
    }

    public void setTotalQuestionsAnswered(int totalQuestionsAnswered) {
        this.totalQuestionsAnswered = totalQuestionsAnswered;
    }

    public Date getLastStudyDate() {
        return lastStudyDate;
    }

    public void setLastStudyDate(Date lastStudyDate) {
        this.lastStudyDate = lastStudyDate;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(int longestStreak) {
        this.longestStreak = longestStreak;
    }

    public int getStudyDaysThisWeek() {
        return studyDaysThisWeek;
    }

    public void setStudyDaysThisWeek(int studyDaysThisWeek) {
        this.studyDaysThisWeek = studyDaysThisWeek;
    }

    public int getStudyDaysThisMonth() {
        return studyDaysThisMonth;
    }

    public void setStudyDaysThisMonth(int studyDaysThisMonth) {
        this.studyDaysThisMonth = studyDaysThisMonth;
    }

    public String getFavoriteSubject() {
        return favoriteSubject;
    }

    public void setFavoriteSubject(String favoriteSubject) {
        this.favoriteSubject = favoriteSubject;
    }

    public String getWeakestSubject() {
        return weakestSubject;
    }

    public void setWeakestSubject(String weakestSubject) {
        this.weakestSubject = weakestSubject;
    }

    public String getLearningLevel() {
        return learningLevel;
    }

    public void setLearningLevel(String learningLevel) {
        this.learningLevel = learningLevel;
    }

    // Méthodes utilitaires
    public double getAccuracyPercentage() {
        if (totalQuestionsAnswered == 0) return 0.0;
        return (double) totalCorrectAnswers / totalQuestionsAnswered * 100;
    }

    public String getStudyHoursFormatted() {
        int hours = totalStudyHours / 60;
        int minutes = totalStudyHours % 60;
        if (hours > 0) {
            return hours + "h " + minutes + "min";
        } else {
            return minutes + "min";
        }
    }

    public void updateLearningLevel() {
        if (totalExamsCompleted == 0) {
            learningLevel = "Débutant";
        } else if (averageExamScore >= 80 && totalExamsCompleted >= 10) {
            learningLevel = "Expert";
        } else if (averageExamScore >= 60 && totalExamsCompleted >= 5) {
            learningLevel = "Avancé";
        } else if (averageExamScore >= 40 || totalExamsCompleted >= 2) {
            learningLevel = "Intermédiaire";
        } else {
            learningLevel = "Débutant";
        }
    }
}
