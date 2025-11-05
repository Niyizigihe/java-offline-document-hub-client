package com.example.offlinedocumenthub;

public class SessionManager {
    private static boolean loggedIn = false;
    private static int currentUserId = -1;
    private static String currentUsername = "";
    private static String currentRole = "";
    private static String currentFullName = "";

    public static void login(int userId, String username, String role, String fullName) {
        loggedIn = true;
        currentUserId = userId;
        currentUsername = username;
        currentRole = role;
        currentFullName = fullName;
    }

    public static void logout() {
        loggedIn = false;
        currentUserId = -1;
        currentUsername = "";
        currentRole = "";
        currentFullName = "";
    }

    public static boolean isLoggedIn() {
        return loggedIn;
    }

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static String getCurrentRole() {
        return currentRole;
    }

    public static String getCurrentFullName() {
        return currentFullName;
    }

    public static boolean isAdmin() {
        return "admin".equals(currentRole);
    }
}