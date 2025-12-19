package com.example.smart_study.beans;

public class User {
    private int id;
    private String fullName;
    private String username;
    private String Password ;
    private String createdAt;

    public User(String fullName, String username, String password, String createdAt) {
        this.fullName = fullName;
        this.username = username;
        Password = password;
        this.createdAt = createdAt;
    }

    public User() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User(String fullName, String username, String password) {
        this.fullName = fullName;
        this.username = username;
        Password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "fullName='" + fullName + '\'' +
                ", username='" + username + '\'' +
                ", Password='" + Password + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
