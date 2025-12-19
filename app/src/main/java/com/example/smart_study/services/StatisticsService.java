package com.example.smart_study.services;

import android.content.Context;
import android.util.Log;

import com.example.smart_study.beans.CourseHistoryModel;
import com.example.smart_study.beans.ExamHistoryModel;
import com.example.smart_study.beans.UserStatisticsModel;
import com.example.smart_study.db.AppDatabase;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StatisticsService {

    private static StatisticsService instance;
    private final AppDatabase db;

    private StatisticsService(Context context) {
        this.db = AppDatabase.getInstance(context);
    }

    public static synchronized StatisticsService getInstance(Context context) {
        if (instance == null) {
            instance = new StatisticsService(context);
        }
        return instance;
    }

    /**
     * Met à jour les statistiques après la completion d'un examen
     */
    public void updateStatisticsAfterExam(ExamHistoryModel exam) {
        new Thread(() -> {
            try {
                // Récupérer les statistiques actuelles ou créer de nouvelles
                UserStatisticsModel stats = db.userStatisticsDao().getUserStatistics();
                if (stats == null) {
                    stats = new UserStatisticsModel();
                }

                // Mettre à jour les statistiques d'examen
                stats.setTotalExamsCompleted(stats.getTotalExamsCompleted() + 1);
                stats.setTotalCorrectAnswers(stats.getTotalCorrectAnswers() + exam.getCorrectAnswers());
                stats.setTotalQuestionsAnswered(stats.getTotalQuestionsAnswered() + exam.getTotalQuestions());

                // Recalculer la moyenne
                double newAverage = calculateExamAverage();
                stats.setAverageExamScore(newAverage);

                // Mettre à jour le meilleur score
                if (exam.getScorePercentage() > stats.getBestExamScore()) {
                    stats.setBestExamScore((int) exam.getScorePercentage());
                }

                // Mettre à jour la dernière date d'étude
                stats.setLastStudyDate(new Date());

                // Mettre à jour les séries
                updateStreak(stats);

                // Mettre à jour les jours d'étude de la semaine
                updateWeeklyStats(stats);

                // Recalculer le niveau d'apprentissage
                stats.updateLearningLevel();

                // Sauvegarder les statistiques
                if (db.userStatisticsDao().getStatisticsCount() == 0) {
                    db.userStatisticsDao().insert(stats);
                } else {
                    db.userStatisticsDao().update(stats);
                }

                Log.d("StatisticsService", "Statistiques mises à jour après examen");
            } catch (Exception e) {
                Log.e("StatisticsService", "Erreur lors de la mise à jour des statistiques après examen: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Met à jour les statistiques après la génération de contenu
     */
    public void updateStatisticsAfterGeneration(CourseHistoryModel course) {
        new Thread(() -> {
            try {
                // Récupérer les statistiques actuelles ou créer de nouvelles
                UserStatisticsModel stats = db.userStatisticsDao().getUserStatistics();
                if (stats == null) {
                    stats = new UserStatisticsModel();
                }

                // Mettre à jour selon le type de génération
                switch (course.getGenerationType()) {
                    case "course":
                        stats.setTotalCoursesGenerated(stats.getTotalCoursesGenerated() + 1);
                        break;
                    case "resume":
                        stats.setTotalResumesGenerated(stats.getTotalResumesGenerated() + 1);
                        break;
                    case "flashcard":
                        stats.setTotalFlashcardsGenerated(stats.getTotalFlashcardsGenerated() + 1);
                        break;
                    case "quiz":
                        // Les quiz sont gérés séparément dans les examens
                        break;
                }

                // Mettre à jour la dernière date d'étude
                stats.setLastStudyDate(new Date());

                // Mettre à jour les séries
                updateStreak(stats);

                // Mettre à jour les jours d'étude de la semaine
                updateWeeklyStats(stats);

                // Sauvegarder les statistiques
                if (db.userStatisticsDao().getStatisticsCount() == 0) {
                    db.userStatisticsDao().insert(stats);
                } else {
                    db.userStatisticsDao().update(stats);
                }

                Log.d("StatisticsService", "Statistiques mises à jour après génération de " + course.getGenerationType());
            } catch (Exception e) {
                Log.e("StatisticsService", "Erreur lors de la mise à jour des statistiques après génération: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Ajoute une entrée à l'historique des cours générés
     */
    public void addToCourseHistory(CourseHistoryModel course) {
        new Thread(() -> {
            try {
                db.courseHistoryDao().insert(course);
                Log.d("StatisticsService", "Cours ajouté à l'historique: " + course.getTitle());
            } catch (Exception e) {
                Log.e("StatisticsService", "Erreur lors de l'ajout à l'historique des cours: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Calcule la moyenne des scores d'examen
     */
    private double calculateExamAverage() {
        return db.examHistoryDao().getAverageScore();
    }

    /**
     * Met à jour les statistiques de série (streak)
     */
    private void updateStreak(UserStatisticsModel stats) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar lastStudy = Calendar.getInstance();
        if (stats.getLastStudyDate() != null) {
            lastStudy.setTime(stats.getLastStudyDate());
            lastStudy.set(Calendar.HOUR_OF_DAY, 0);
            lastStudy.set(Calendar.MINUTE, 0);
            lastStudy.set(Calendar.SECOND, 0);
            lastStudy.set(Calendar.MILLISECOND, 0);
        } else {
            // Première fois, streak commence à 1
            stats.setCurrentStreak(1);
            return;
        }

        long diffInDays = (today.getTimeInMillis() - lastStudy.getTimeInMillis()) / (24 * 60 * 60 * 1000);

        if (diffInDays == 1) {
            // Étude consécutive
            stats.setCurrentStreak(stats.getCurrentStreak() + 1);
            if (stats.getCurrentStreak() > stats.getLongestStreak()) {
                stats.setLongestStreak(stats.getCurrentStreak());
            }
        } else if (diffInDays == 0) {
            // Même jour, ne rien changer
        } else {
            // Série rompue
            stats.setCurrentStreak(1);
        }
    }

    /**
     * Met à jour les statistiques hebdomadaires
     */
    private void updateWeeklyStats(UserStatisticsModel stats) {
        Calendar today = Calendar.getInstance();
        Calendar weekStart = Calendar.getInstance();
        weekStart.set(Calendar.DAY_OF_WEEK, weekStart.getFirstDayOfWeek());

        // Compter les jours d'étude cette semaine
        int studyDaysThisWeek = 0;
        List<ExamHistoryModel> recentExams = db.examHistoryDao().getRecentExams(100); // Récupérer les 100 derniers examens

        for (ExamHistoryModel exam : recentExams) {
            if (exam.getCompletedDate() != null) {
                Calendar examDate = Calendar.getInstance();
                examDate.setTime(exam.getCompletedDate());

                if (examDate.after(weekStart) || examDate.equals(weekStart)) {
                    studyDaysThisWeek++;
                }
            }
        }

        // TODO: Ajouter la logique pour compter les jours avec génération de cours
        // Pour l'instant, on se base seulement sur les examens

        stats.setStudyDaysThisWeek(Math.min(studyDaysThisWeek, 7)); // Maximum 7 jours
    }

    /**
     * Réinitialise les statistiques (pour les tests ou reset utilisateur)
     */
    public void resetStatistics() {
        new Thread(() -> {
            try {
                db.userStatisticsDao().deleteAllStatistics();
                db.courseHistoryDao().deleteAll();
                Log.d("StatisticsService", "Statistiques réinitialisées");
            } catch (Exception e) {
                Log.e("StatisticsService", "Erreur lors de la réinitialisation des statistiques: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Récupère les statistiques actuelles
     */
    public void getCurrentStatistics(StatisticsCallback callback) {
        new Thread(() -> {
            try {
                UserStatisticsModel stats = db.userStatisticsDao().getUserStatistics();
                int totalCourses = db.courseHistoryDao().getTotalCoursesCount();
                int successfulCourses = db.courseHistoryDao().getSuccessfulCoursesCount();

                if (callback != null) {
                    callback.onStatisticsLoaded(stats, totalCourses, successfulCourses);
                }
            } catch (Exception e) {
                Log.e("StatisticsService", "Erreur lors de la récupération des statistiques: " + e.getMessage());
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        }).start();
    }

    public interface StatisticsCallback {
        void onStatisticsLoaded(UserStatisticsModel stats, int totalCourses, int successfulCourses);
        void onError(String error);
    }
}
