package com.example.smart_study.beans;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "resume_history")
public class ResumeModel implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String content;
    private Date createdDate;
    private String sourceFileName; // Original PDF filename if available

    // Constructeur
    public ResumeModel() {
        this.createdDate = new Date();
    }

    // Constructeur avec paramètres
    public ResumeModel(String title, String content, String sourceFileName) {
        this.title = title;
        this.content = content;
        this.sourceFileName = sourceFileName;
        this.createdDate = new Date();
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }
}

