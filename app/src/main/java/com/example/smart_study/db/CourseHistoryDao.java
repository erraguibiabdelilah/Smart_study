package com.example.smart_study.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.smart_study.beans.CourseHistory;

import java.util.List;

@Dao
public interface CourseHistoryDao {

    @Insert
    void insert(CourseHistory courseHistory);

    @Query("SELECT * FROM course_history ORDER BY savedDate DESC")
    List<CourseHistory> getAllCourses();

    @Delete
    void delete(CourseHistory courseHistory);
}
