package com.example.smart_study.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.smart_study.beans.UserStatisticsModel;

@Dao
public interface UserStatisticsDao {

    // Insérer ou mettre à jour les statistiques utilisateur
    @Insert
    long insert(UserStatisticsModel statistics);

    // Mettre à jour les statistiques
    @Update
    void update(UserStatisticsModel statistics);

    // Récupérer les statistiques utilisateur (il devrait y en avoir qu'une seule)
    @Query("SELECT * FROM user_statistics LIMIT 1")
    UserStatisticsModel getUserStatistics();

    // Vérifier si les statistiques existent
    @Query("SELECT COUNT(*) FROM user_statistics")
    int getStatisticsCount();

    // Supprimer toutes les statistiques
    @Query("DELETE FROM user_statistics")
    void deleteAllStatistics();

    // Récupérer le niveau d'apprentissage actuel
    @Query("SELECT learningLevel FROM user_statistics LIMIT 1")
    String getCurrentLearningLevel();

    // Récupérer les statistiques de streak
    @Query("SELECT currentStreak, longestStreak FROM user_statistics LIMIT 1")
    StreakData getStreakData();

    // Classe interne pour les données de streak
    class StreakData {
        public int currentStreak;
        public int longestStreak;
    }
}
