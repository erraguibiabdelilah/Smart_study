package com.example.smart_study.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.smart_study.beans.ResumeModel;

import java.util.List;

@Dao
public interface ResumeDao {

    // Insérer un nouveau résumé
    @Insert
    long insert(ResumeModel resume);

    // Mettre à jour un résumé
    @Update
    void update(ResumeModel resume);

    // Supprimer un résumé
    @Delete
    void delete(ResumeModel resume);

    // Supprimer tous les résumés
    @Query("DELETE FROM resume_history")
    void deleteAll();

    // Récupérer tous les résumés (triés par date décroissante)
    @Query("SELECT * FROM resume_history ORDER BY createdDate DESC")
    List<ResumeModel> getAllResumes();

    // Récupérer un résumé par ID
    @Query("SELECT * FROM resume_history WHERE id = :resumeId")
    ResumeModel getResumeById(int resumeId);

    // Récupérer les résumés par titre
    @Query("SELECT * FROM resume_history WHERE title LIKE :title ORDER BY createdDate DESC")
    List<ResumeModel> getResumesByTitle(String title);

    // Compter le nombre total de résumés
    @Query("SELECT COUNT(*) FROM resume_history")
    int getResumeCount();

    // Récupérer les N derniers résumés
    @Query("SELECT * FROM resume_history ORDER BY createdDate DESC LIMIT :limit")
    List<ResumeModel> getRecentResumes(int limit);
}

