package com.example.offlinedocumenthub;

import com.example.offlinedocumenthub.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.*;

public class ApiClient {
    static String baseUrl = "http://localhost:8080/api";
    static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // Store the authentication token after login
    private static String authToken = null;

    public static void setServerAddress(String host) {
        baseUrl = "http://" + host + ":8080/api";
    }

    private static void updateActivityTime() {
        if (SessionManager.isLoggedIn()) {
            SessionManager.updateLastActivityTime();
        }
    }
    // Clear auth token on logout
    public static void logout() {
        try {
            if (authToken != null) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/logout"))
                        .header("Authorization", "Bearer " + authToken)
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();

                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            authToken = null;
        }
    }

    // Helper method to add auth header to requests
    public static HttpRequest.Builder addAuthHeader(HttpRequest.Builder builder) {
        if (authToken != null) {
            return builder.header("Authorization", "Bearer " + authToken);
        }
        return builder;
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static HttpClient getHttpClient() {
        return httpClient;
    }

    // Login method - UPDATED to store auth token
    public static boolean login(String username, String password) {
        try {
            Map<String, String> formData = new HashMap<>();
            formData.put("username", username);
            formData.put("password", password);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/login"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(buildFormDataFromMap(formData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                ApiResponse<Map<String, Object>> apiResponse = objectMapper.readValue(
                        responseBody,
                        new TypeReference<ApiResponse<Map<String, Object>>>() {}
                );

                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    Map<String, Object> userData = apiResponse.getData();
                    int userId = ((Number) userData.get("userId")).intValue();
                    String role = (String) userData.get("role");
                    String fullName = (String) userData.get("fullName");

                    // Store the auth token
                    authToken = (String) userData.get("authToken");

                    // Set session
                    SessionManager.login(userId, username, role, fullName);
                    updateActivityTime();
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // In ApiClient.java - add this method with the other document methods
    public static boolean updateDocument(Document document) {
        try {
            System.out.println("Updating document ID: " + document.getDocId() + " with title: " + document.getTitle());

            String jsonBody = objectMapper.writeValueAsString(document);

            HttpRequest request = addAuthHeader(HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/documents/" + document.getDocId()))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Update document response: " + response.statusCode());
            System.out.println("Update document response body: " + response.body());

            if (response.statusCode() == 401) {
                authToken = null;
                showAuthError();
            }

            updateActivityTime();
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("ERROR updating document: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Activity Logs methods - UPDATED with auth
    // Activity Logs methods - FIXED
    public static List<ActivityLog> getActivityLogs() {
        try {
            HttpRequest request = addAuthHeader(HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/activity-logs"))
                    .GET())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Activity logs response: " + response.statusCode());

            if (response.statusCode() == 200) {
                updateActivityTime();
                // FIXED: Use the properly configured objectMapper
                return objectMapper.readValue(response.body(), new TypeReference<List<ActivityLog>>() {});
            } else if (response.statusCode() == 401) {
                authToken = null;
                System.err.println("Authentication failed for activity logs");
            }
        } catch (Exception e) {
            System.err.println("ERROR in getActivityLogs: " + e.getMessage());
            e.printStackTrace();
        }
        return List.of();
    }

    // Test connection method
    public static boolean testConnection() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/system/health"))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    // Document methods - UPDATED with auth
    // Add better error handling and debugging
    // Document methods - FIXED with proper ObjectMapper usage
    public static List<Document> getDocuments() {
        try {
            HttpRequest request = addAuthHeader(HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/documents"))
                    .GET())
                    .build();

            System.out.println("Sending request to: " + baseUrl + "/documents");
            System.out.println("Auth token: " + (authToken != null ? "Present" : "Missing"));

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response status: " + response.statusCode());
            System.out.println("Response body: " + response.body());

            if (response.statusCode() == 200) {
                updateActivityTime();
                // FIXED: Use the properly configured objectMapper
                return objectMapper.readValue(response.body(), new TypeReference<List<Document>>() {});
            } else if (response.statusCode() == 401) {
                authToken = null;
                showAuthError();
            } else {
                System.err.println("Server returned error: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("ERROR in getDocuments: " + e.getMessage());
            e.printStackTrace();
        }
        return List.of();
    }

    private static void showAuthError() {
        // You can show an alert or log the authentication error
        System.err.println("Authentication failed. Please login again.");
    }

    public static boolean createDocument(String title, String filePath, File file) {
        try {
            // Create multipart form data for file upload
            var boundary = "-------------" + System.currentTimeMillis();

            // Build multipart request
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8), true);

            // Add form fields
            writer.append("--" + boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"title\"").append("\r\n");
            writer.append("Content-Type: text/plain; charset=UTF-8").append("\r\n");
            writer.append("\r\n");
            writer.append(title).append("\r\n");
            writer.flush();

            // Add file
            writer.append("--" + boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"").append("\r\n");
            writer.append("Content-Type: application/octet-stream").append("\r\n");
            writer.append("Content-Transfer-Encoding: binary").append("\r\n");
            writer.append("\r\n");
            writer.flush();

            Files.copy(file.toPath(), byteArrayOutputStream);
            writer.append("\r\n");
            writer.flush();

            // End of multipart
            writer.append("--" + boundary + "--").append("\r\n");
            writer.flush();

            byte[] data = byteArrayOutputStream.toByteArray();

            HttpRequest request = addAuthHeader(HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/documents"))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(data)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 401) {
                authToken = null;
            }

            updateActivityTime();
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    // Add this method to ApiClient.java for file downloads
    public static byte[] downloadDocument(int docId) {
        try {
            System.out.println("Downloading document ID: " + docId);

            HttpRequest request = addAuthHeader(HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/documents/" + docId + "/download"))
                    .GET())
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            System.out.println("Download response status: " + response.statusCode());

            if (response.statusCode() == 200) {
                updateActivityTime();
                byte[] fileData = response.body();
                System.out.println("Download successful, received " + fileData.length + " bytes");
                return fileData;
            } else if (response.statusCode() == 401) {
                authToken = null;
                showAuthError();
                System.err.println("Authentication failed for download");
            } else if (response.statusCode() == 404) {
                System.err.println("Document not found on server: " + docId);
            } else {
                System.err.println("Download failed with status: " + response.statusCode());
                // Try to read error message
                String errorBody = new String(response.body());
                System.err.println("Error response: " + errorBody);
            }
        } catch (Exception e) {
            System.err.println("ERROR downloading document: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static boolean deleteDocument(int docId) {
        try {
            HttpRequest request = addAuthHeader(HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/documents/" + docId))
                    .DELETE())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 401) {
                authToken = null;
            }

            updateActivityTime();
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // User methods - UPDATED with auth
    public static List<User> getUsers() {
        try {
            HttpRequest request = addAuthHeader(HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/users"))
                    .GET())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Users response: " + response.statusCode());

            if (response.statusCode() == 200) {
                updateActivityTime();
                // FIXED: Use the properly configured objectMapper
                return objectMapper.readValue(response.body(), new TypeReference<List<User>>() {});
            } else if (response.statusCode() == 401) {
                authToken = null;
            }
        } catch (Exception e) {
            System.err.println("ERROR in getUsers: " + e.getMessage());
            e.printStackTrace();
        }
        return List.of();
    }

    public static boolean createUser(User user) {
        try {
            String jsonBody = objectMapper.writeValueAsString(user);

            HttpRequest request = addAuthHeader(HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/users"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 401) {
                authToken = null;
            }

            updateActivityTime();
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateUser(User user) {
        try {
            String jsonBody = objectMapper.writeValueAsString(user);

            HttpRequest request = addAuthHeader(HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/users/" + user.getId()))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 401) {
                authToken = null;
            }

            updateActivityTime();
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteUser(int userId) {
        try {
            HttpRequest request = addAuthHeader(HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/users/" + userId))
                    .DELETE())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 401) {
                authToken = null;
            }

            updateActivityTime();
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Registration method - NO auth needed
    public static boolean register(String username, String email, String password) {
        try {
            Map<String, String> formData = new HashMap<>();
            formData.put("username", username);
            formData.put("email", email);
            formData.put("password", password);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/register"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(buildFormDataFromMap(formData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Check if user is authenticated
    public static boolean isAuthenticated() {
        return authToken != null;
    }
    public static String getAuthToken() {
        return authToken;
    }



    // Check server availability
    public static boolean isServerAvailable() {
        return testConnection();
    }

    private static HttpRequest.BodyPublisher buildFormDataFromMap(Map<String, String> data) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(entry.getKey());
            builder.append("=");
            builder.append(entry.getValue());
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }



public static Map<String, Object> triggerBackup() {
    return triggerBackup("manual"); // Default to manual
}
public static Map<String, Object> triggerBackup(String source) {
    try {
        String url = baseUrl + "/backup/trigger";
        if ("auto".equals(source)) {
            url += "?source=auto";
        }

        HttpRequest request = addAuthHeader(HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 202) {
            updateActivityTime();
            Map<String, Object> result = objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
            System.out.println("✅ Backup started with ID: " + result.get("backupId"));
            return result;
        } else {
            System.err.println("Backup trigger failed with status: " + response.statusCode());
            return createErrorResult("Backup failed with status: " + response.statusCode());
        }
    } catch (Exception e) {
        System.err.println("ERROR triggering backup: " + e.getMessage());
        e.printStackTrace();
        return createErrorResult("Backup failed: " + e.getMessage());
    }
}

    public static Map<String, Object> getBackupProgress(String backupId) {
        try {
            String url = baseUrl + "/backup/progress";
            if (backupId != null && !backupId.isEmpty()) {
                url += "?backupId=" + backupId;
            }

            HttpRequest request = addAuthHeader(HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ApiResponse<Map<String, Object>> apiResponse = objectMapper.readValue(
                        response.body(),
                        new TypeReference<ApiResponse<Map<String, Object>>>() {}
                );
                return apiResponse.getData();
            } else {
                return createErrorResult("Failed to get backup progress");
            }
        } catch (Exception e) {
            System.err.println("ERROR getting backup progress: " + e.getMessage());
            return createErrorResult("Failed to get backup progress: " + e.getMessage());
        }
    }
    public static Map<String, Object> getBackupProgress() {
        try {
            HttpRequest request = addAuthHeader(HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/backup/progress"))
                    .GET())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ApiResponse<Map<String, Object>> apiResponse = objectMapper.readValue(
                        response.body(),
                        new TypeReference<ApiResponse<Map<String, Object>>>() {}
                );
                return apiResponse.getData();
            } else {
                return createErrorResult("Failed to get backup progress");
            }
        } catch (Exception e) {
            System.err.println("ERROR getting backup progress: " + e.getMessage());
            return createErrorResult("Failed to get backup progress: " + e.getMessage());
        }
    }

    public static List<Map<String, String>> listBackups() {
        try {
            HttpRequest request = addAuthHeader(HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/backup/list"))
                    .GET())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                updateActivityTime();
                ApiResponse<List<Map<String, String>>> apiResponse = objectMapper.readValue(
                        response.body(),
                        new TypeReference<ApiResponse<List<Map<String, String>>>>() {}
                );
                return apiResponse.getData();
            } else {
                return List.of();
            }
        } catch (Exception e) {
            System.err.println("ERROR listing backups: " + e.getMessage());
            return List.of();
        }
    }

    private static Map<String, Object> createErrorResult(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        result.put("progress", 0);
        return result;
    }

    // Message methods
// Message methods - UPDATED with proper error handling
    public static List<Message> getConversations() {
        try {
            HttpRequest request = addAuthHeader(HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/messages/conversations"))
                    .GET())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Conversations response: " + response.statusCode());
            System.out.println("Conversations body: " + response.body());

            if (response.statusCode() == 200) {
                updateActivityTime();
                // Check if response is an error object or actual data
                String responseBody = response.body();
                if (responseBody.startsWith("{")) {
                    // It's a JSON object, check if it's an error
                    Map<String, Object> result = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
                    if (result.containsKey("success") && !(Boolean) result.get("success")) {
                        System.err.println("Server error: " + result.get("message"));
                        return List.of();
                    }
                }
                // It's an array, parse as list of messages
                return objectMapper.readValue(responseBody, new TypeReference<List<Message>>() {});
            } else if (response.statusCode() == 401) {
                authToken = null;
                showAuthError();
            }
        } catch (Exception e) {
            System.err.println("ERROR getting conversations: " + e.getMessage());
            e.printStackTrace();
        }
        return List.of();
    }

    public static List<Message> getMessages(int otherUserId) {
        try {
            HttpRequest request = addAuthHeader(HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/messages/" + otherUserId))
                    .GET())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("=== MESSAGES DEBUG ===");
            System.out.println("URL: " + baseUrl + "/messages/" + otherUserId);
            System.out.println("Status: " + response.statusCode());
            System.out.println("Response: " + response.body());
            System.out.println("=== END DEBUG ===");

            if (response.statusCode() == 200) {
//                updateActivityTime();
                String responseBody = response.body().trim();

                // Handle empty response
                if (responseBody.isEmpty()) {
                    return List.of();
                }

                // Check if it's an error response
                if (responseBody.startsWith("{")) {
                    try {
                        Map<String, Object> result = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
                        if (result.containsKey("success") && !(Boolean) result.get("success")) {
                            System.err.println("Server returned error: " + result.get("message"));
                            return List.of();
                        }
                        // If it has success=true but is an object, try to extract data array
                        if (result.containsKey("data") && result.get("data") instanceof List) {
                            return objectMapper.convertValue(result.get("data"), new TypeReference<List<Message>>() {});
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to parse error response: " + e.getMessage());
                        return List.of();
                    }
                }

                // Try to parse as array
                return objectMapper.readValue(responseBody, new TypeReference<List<Message>>() {});
            } else if (response.statusCode() == 401) {
                authToken = null;
                showAuthError();
            }
        } catch (Exception e) {
            System.err.println("ERROR getting messages: " + e.getMessage());
            e.printStackTrace();
        }
        return List.of();
    }

    public static boolean sendMessage(int receiverId, String messageText) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("receiverId", receiverId);
            messageData.put("messageText", messageText);

            String jsonBody = objectMapper.writeValueAsString(messageData);

            HttpRequest request = addAuthHeader(HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/messages"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 401) {
                authToken = null;
                showAuthError();
            }

            updateActivityTime();
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("ERROR sending message: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static void markMessagesAsRead(int otherUserId) {
        try {
            HttpRequest request = addAuthHeader(HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/messages/" + otherUserId + "/read"))
                    .POST(HttpRequest.BodyPublishers.noBody()))
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            System.err.println("ERROR marking messages as read: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static Map<String, Object> getSystemStatus() {
        try {
            HttpRequest request = addAuthHeader(HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/system/status"))
                    .GET())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                updateActivityTime();
                ApiResponse<Map<String, Object>> apiResponse = objectMapper.readValue(
                        response.body(),
                        new TypeReference<ApiResponse<Map<String, Object>>>() {}
                );
                return apiResponse.getData();
            } else {
                return createErrorResult("Failed to get system status");
            }
        } catch (Exception e) {
            System.err.println("ERROR getting system status: " + e.getMessage());
            return createErrorResult("Failed to get system status: " + e.getMessage());
        }
    }

    public static Map<String, Object> getActiveSessions() {
        try {
            HttpRequest request = addAuthHeader(HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/system/sessions"))
                    .GET())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                updateActivityTime();
                ApiResponse<Map<String, Object>> apiResponse = objectMapper.readValue(
                        response.body(),
                        new TypeReference<ApiResponse<Map<String, Object>>>() {}
                );
                return apiResponse.getData();
            } else {
                return createErrorResult("Failed to get active sessions");
            }
        } catch (Exception e) {
            System.err.println("ERROR getting active sessions: " + e.getMessage());
            return createErrorResult("Failed to get active sessions: " + e.getMessage());
        }
    }
    public static List<Map<String, Object>> getBackupHistory() {
        try {
            HttpRequest request = addAuthHeader(HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/backup/history"))
                    .GET())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                updateActivityTime();
                ApiResponse<List<Map<String, Object>>> apiResponse = objectMapper.readValue(
                        response.body(),
                        new TypeReference<ApiResponse<List<Map<String, Object>>>>() {}
                );
                return apiResponse.getData();
            } else {
                return List.of();
            }
        } catch (Exception e) {
            System.err.println("ERROR getting backup history: " + e.getMessage());
            return List.of();
        }
    }


    public static List<Map<String, Object>> getNotifications() {
        try {
            HttpRequest request = addAuthHeader(HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/notifications"))
                    .GET())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                updateActivityTime();
                ApiResponse<List<Map<String, Object>>> apiResponse = objectMapper.readValue(
                        response.body(),
                        new TypeReference<ApiResponse<List<Map<String, Object>>>>() {}
                );
                return apiResponse.getData();
            } else {
                return List.of();
            }
        } catch (Exception e) {
            System.err.println("ERROR getting notifications: " + e.getMessage());
            return List.of();
        }
    }

    public static boolean markNotificationAsRead(int notificationId) {
        try {
            HttpRequest request = addAuthHeader(HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/notifications/" + notificationId + "/read"))
                    .POST(HttpRequest.BodyPublishers.noBody()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            updateActivityTime();
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("ERROR marking notification as read: " + e.getMessage());
            return false;
        }
    }

}