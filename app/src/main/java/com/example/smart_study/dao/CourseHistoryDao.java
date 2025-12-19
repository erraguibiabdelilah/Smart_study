package com.example.smart_study.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.smart_study.beans.CourseHistoryModel;

import java.util.List;

@Dao
public interface CourseHistoryDao {

    // Insérer un nouvel élément d'historique
    @Insert
    long insert(CourseHistoryModel courseHistory);

    // Mettre à jour un élément d'historique
    @Update
    void update(CourseHistoryModel courseHistory);

    // Supprimer un élément d'historique
    @Delete
    void delete(CourseHistoryModel courseHistory);

    // Supprimer tous les éléments d'historique
    @Query("DELETE FROM course_history")
    void deleteAll();

    // Récupérer tous les éléments d'historique (triés par date décroissante)
    @Query("SELECT * FROM course_history ORDER BY generatedDate DESC")
    List<CourseHistoryModel> getAllCourseHistory();

    // Récupérer un élément par ID
    @Query("SELECT * FROM course_history WHERE id = :courseId")
    CourseHistoryModel getCourseById(int courseId);

    // Récupérer les éléments par type de génération
    @Query("SELECT * FROM course_history WHERE generationType = :type ORDER BY generatedDate DESC")
    List<CourseHistoryModel> getCoursesByType(String type);

    // Récupérer les éléments par sujet
    @Query("SELECT * FROM course_history WHERE subject LIKE :subject ORDER BY generatedDate DESC")
    List<CourseHistoryModel> getCoursesBySubject(String subject);

    // Récupérer les éléments réussis seulement
    @Query("SELECT * FROM course_history WHERE status = 'success' ORDER BY generatedDate DESC")
    List<CourseHistoryModel> getSuccessfulCourses();

    // Compter le nombre total d'éléments générés
    @Query("SELECT COUNT(*) FROM course_history")
    int getTotalCoursesCount();

    // Compter par type de génération
    @Query("SELECT COUNT(*) FROM course_history WHERE generationType = :type")
    int getCoursesCountByType(String type);

    // Compter les générations réussies
    @Query("SELECT COUNT(*) FROM course_history WHERE status = 'success'")
    int getSuccessfulCoursesCount();

    // Récupérer les N derniers éléments
    @Query("SELECT * FROM course_history ORDER BY generatedDate DESC LIMIT :limit")
    List<CourseHistoryModel> getRecentCourses(int limit);

    // Récupérer les éléments générés aujourd'hui
    @Query("SELECT * FROM course_history WHERE date(generatedDate) = date('now') ORDER BY generatedDate DESC")
    List<CourseHistoryModel> getTodayCourses();

    // Calculer le temps total de génération
    @Query("SELECT SUM(generationTime) FROM course_history WHERE status = 'success'")
    long getTotalGenerationTime();

    // Récupérer les sujets les plus utilisés
    @Query("SELECT subject, COUNT(*) as count FROM course_history WHERE subject IS NOT NULL AND subject != '' GROUP BY subject ORDER BY count DESC LIMIT 5")
    List<SubjectCount> getTopSubjects();

    // Classe interne pour les statistiques de sujet
    class SubjectCount {
        public String subject;
        public int count;
    }
}
