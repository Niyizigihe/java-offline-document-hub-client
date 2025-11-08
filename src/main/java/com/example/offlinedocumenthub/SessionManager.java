package com.example.offlinedocumenthub;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class SessionManager {
    private static boolean loggedIn = false;
    private static int currentUserId = -1;
    private static String currentUsername = "";
    private static String currentRole = "";
    private static String currentFullName = "";
    private static LocalDateTime lastActivityTime;
    private static final int SESSION_TIMEOUT_MINUTES = 10;

    public static void login(int userId, String username, String role, String fullName) {
        loggedIn = true;
        currentUserId = userId;
        currentUsername = username;
        currentRole = role;
        currentFullName = fullName;
        updateLastActivityTime();
        System.out.println("=== SESSION: User logged in at " + LocalDateTime.now() + " ===");
    }

    public static void logout() {
        loggedIn = false;
        currentUserId = -1;
        currentUsername = "";
        currentRole = "";
        currentFullName = "";
        lastActivityTime = null;
    }

    public static void updateLastActivityTime() {
        lastActivityTime = LocalDateTime.now();
        System.out.println("=== SESSION: Activity updated at " + lastActivityTime + " ===");
    }

    public static boolean isSessionExpired() {
        if (!loggedIn || lastActivityTime == null) {
            System.out.println("=== SESSION: Not logged in or no activity time ===");
            return true;
        }

        long secondsSinceLastActivity = ChronoUnit.SECONDS.between(lastActivityTime, LocalDateTime.now());
        boolean expired = secondsSinceLastActivity >= (SESSION_TIMEOUT_MINUTES * 60);

        System.out.println("=== SESSION: Checking expiration - " + secondsSinceLastActivity +
                " seconds since last activity, expired: " + expired + " ===");

        if (expired) {
            logout();
        }
        return expired;
    }

    // In SessionManager - add this temporarily to verify
    public static int getSecondsUntilExpiration() {
        if (!loggedIn || lastActivityTime == null) {
            return 0;
        }

        long secondsSinceLastActivity = ChronoUnit.SECONDS.between(lastActivityTime, LocalDateTime.now());
        int secondsLeft = (int) Math.max(0, (SESSION_TIMEOUT_MINUTES * 60) - secondsSinceLastActivity);

        System.out.println("=== SESSION: " + secondsLeft + " seconds until expiration ===");
        return secondsLeft;
    }

    public static boolean isLoggedIn() {
        if (isSessionExpired()) {
            logout();
            return false;
        }
        return loggedIn;
    }

    public static int getCurrentUserId() {
        if (isSessionExpired()) {
            logout();
            return -1;
        }
        return currentUserId;
    }

    public static String getCurrentUsername() {
        if (isSessionExpired()) {
            logout();
            return "";
        }
        return currentUsername;
    }

    public static String getCurrentRole() {
        if (isSessionExpired()) {
            logout();
            return "";
        }
        return currentRole;
    }

    public static String getCurrentFullName() {
        if (isSessionExpired()) {
            logout();
            return "";
        }
        return currentFullName;
    }

    public static boolean isAdmin() {
        if (isSessionExpired()) {
            logout();
            return false;
        }
        return "admin".equals(currentRole);
    }

}