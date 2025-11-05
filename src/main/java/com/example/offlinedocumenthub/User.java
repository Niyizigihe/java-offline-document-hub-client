package com.example.offlinedocumenthub;

public class User {
    private int id;
    private String username;
    private String fullName;
    private String password;
    private String role;

    // Default constructor for JSON deserialization
    public User() {}

    public User(int id, String username, String fullName, String password, String role) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.password = password;
        this.role = role;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}