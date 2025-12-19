package com.example.smart_study.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.smart_study.beans.ExamHistoryModel;
import com.example.smart_study.beans.UserStatisticsModel;
import com.example.smart_study.beans.CourseHistoryModel;
import com.example.smart_study.dao.Converters;
import com.example.smart_study.dao.ExamHistoryDao;
import com.example.smart_study.dao.UserStatisticsDao;
import com.example.smart_study.dao.CourseHistoryDao;

@Database(entities = {ExamHistoryModel.class, UserStatisticsModel.class, CourseHistoryModel.class}, version = 2, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract ExamHistoryDao examHistoryDao();
    public abstract UserStatisticsDao userStatisticsDao();
    public abstract CourseHistoryDao courseHistoryDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "smart_study_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}