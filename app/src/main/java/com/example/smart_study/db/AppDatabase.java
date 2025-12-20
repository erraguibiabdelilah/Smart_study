package com.example.smart_study.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.smart_study.beans.CourseHistory;
import com.example.smart_study.beans.ExamHistoryModel;
import com.example.smart_study.beans.ResumeModel;
import com.example.smart_study.dao.Converters;
import com.example.smart_study.dao.ExamHistoryDao;
import com.example.smart_study.dao.ResumeDao;

@Database(entities = {ExamHistoryModel.class, ResumeModel.class}, version = 2, exportSchema = false)
@TypeConverters(Converters.class)
@Database(entities = {ExamHistoryModel.class, CourseHistory.class}, version = 2, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract ExamHistoryDao examHistoryDao();
    public abstract ResumeDao resumeDao();
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
