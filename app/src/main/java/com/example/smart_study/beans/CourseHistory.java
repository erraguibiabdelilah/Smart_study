package com.example.smart_study.beans;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.smart_study.db.DateConverter;

import java.util.Date;

@Entity(tableName = "course_history")
@TypeConverters(DateConverter.class)
public class CourseHistory {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String courseTitle;
    private String pdfUri;
    private Date savedDate;

    public CourseHistory(String courseTitle, String pdfUri, Date savedDate) {
        this.courseTitle = courseTitle;
        this.pdfUri = pdfUri;
        this.savedDate = savedDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getPdfUri() {
        return pdfUri;
    }

    public void setPdfUri(String pdfUri) {
        this.pdfUri = pdfUri;
    }

    public Date getSavedDate() {
        return savedDate;
    }

    public void setSavedDate(Date savedDate) {
        this.savedDate = savedDate;
    }
}
