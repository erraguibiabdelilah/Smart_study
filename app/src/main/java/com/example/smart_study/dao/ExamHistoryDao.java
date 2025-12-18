package com.example.smart_study.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.smart_study.beans.ExamHistoryModel;

import java.util.List;

@Dao
public interface ExamHistoryDao {

    // Insérer un nouvel examen dans l'historique
    @Insert
    long insert(ExamHistoryModel examHistory);

    // Mettre à jour un examen
    @Update
    void update(ExamHistoryModel examHistory);

    // Supprimer un examen
    @Delete
    void delete(ExamHistoryModel examHistory);

    // Supprimer tous les examens
    @Query("DELETE FROM exam_history")
    void deleteAll();

    // Récupérer tous les examens (triés par date décroissante)
    @Query("SELECT * FROM exam_history ORDER BY completedDate DESC")
    List<ExamHistoryModel> getAllExams();

    // Récupérer un examen par ID
    @Query("SELECT * FROM exam_history WHERE id = :examId")
    ExamHistoryModel getExamById(int examId);

    // Récupérer les examens par titre
    @Query("SELECT * FROM exam_history WHERE examTitle LIKE :title ORDER BY completedDate DESC")
    List<ExamHistoryModel> getExamsByTitle(String title);

    // Compter le nombre total d'examens
    @Query("SELECT COUNT(*) FROM exam_history")
    int getExamCount();

    // Récupérer les N derniers examens
    @Query("SELECT * FROM exam_history ORDER BY completedDate DESC LIMIT :limit")
    List<ExamHistoryModel> getRecentExams(int limit);

    // Calculer la moyenne des scores
    @Query("SELECT AVG(scorePercentage) FROM exam_history")
    double getAverageScore();
}