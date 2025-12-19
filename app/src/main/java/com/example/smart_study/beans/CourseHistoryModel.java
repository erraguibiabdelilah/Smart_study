package com.example.smart_study.beans;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.smart_study.dao.Converters;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity(tableName = "course_history")
@TypeConverters(Converters.class)
public class CourseHistoryModel implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    // Type de génération: "course", "resume", "flashcard", "quiz"
    private String generationType;

    // Titre du contenu généré
    private String title;

    // Sujet/matière du contenu
    private String subject;

    // Description du contenu
    private String description;

    // Date de génération
    private Date generatedDate;

    // Contenu généré (texte complet du cours, résumé, etc.)
    private String generatedContent;

    // Pour les flashcards: liste des cartes
    private List<String> flashcards;

    // Pour les quiz: nombre de questions générées
    private int quizQuestionsCount;

    // Source du contenu (nom du fichier PDF uploadé, URL, etc.)
    private String source;

    // Taille du contenu source (en caractères ou pages)
    private int sourceSize;

    // Temps de génération (en millisecondes)
    private long generationTime;

    // Statut de génération: "success", "failed", "pending"
    private String status;

    // Message d'erreur si la génération a échoué
    private String errorMessage;

    // Constructeur
    public CourseHistoryModel() {
        this.generatedDate = new Date();
        this.status = "pending";
        this.generationTime = 0;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGenerationType() {
        return generationType;
    }

    public void setGenerationType(String generationType) {
        this.generationType = generationType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(Date generatedDate) {
        this.generatedDate = generatedDate;
    }

    public String getGeneratedContent() {
        return generatedContent;
    }

    public void setGeneratedContent(String generatedContent) {
        this.generatedContent = generatedContent;
    }

    public List<String> getFlashcards() {
        return flashcards;
    }

    public void setFlashcards(List<String> flashcards) {
        this.flashcards = flashcards;
    }

    public int getQuizQuestionsCount() {
        return quizQuestionsCount;
    }

    public void setQuizQuestionsCount(int quizQuestionsCount) {
        this.quizQuestionsCount = quizQuestionsCount;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getSourceSize() {
        return sourceSize;
    }

    public void setSourceSize(int sourceSize) {
        this.sourceSize = sourceSize;
    }

    public long getGenerationTime() {
        return generationTime;
    }

    public void setGenerationTime(long generationTime) {
        this.generationTime = generationTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    // Méthodes utilitaires
    public String getGenerationTypeLabel() {
        switch (generationType) {
            case "course":
                return "Cours";
            case "resume":
                return "Résumé";
            case "flashcard":
                return "Flashcards";
            case "quiz":
                return "Quiz";
            default:
                return "Contenu";
        }
    }

    public String getStatusLabel() {
        switch (status) {
            case "success":
                return "Réussi";
            case "failed":
                return "Échoué";
            case "pending":
                return "En cours";
            default:
                return "Inconnu";
        }
    }

    public boolean isSuccessful() {
        return "success".equals(status);
    }

    public String getGenerationTimeFormatted() {
        if (generationTime < 1000) {
            return generationTime + "ms";
        } else {
            return (generationTime / 1000) + "s";
        }
    }

    public String getPreviewText() {
        if (generatedContent != null && generatedContent.length() > 100) {
            return generatedContent.substring(0, 100) + "...";
        }
        return generatedContent;
    }
}
