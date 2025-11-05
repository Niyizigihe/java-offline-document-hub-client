package com.example.offlinedocumenthub;

import com.example.offlinedocumenthub.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.nio.charset.*;
import java.nio.file.Files;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.*;

public class ApiClient {
    private static String baseUrl = "http://localhost:8080/api";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void setServerAddress(String host) {
        baseUrl = "http://" + host + ":8080/api";
    }

    // Login method
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

                    // Set session
                    SessionManager.login(userId, username, role, fullName);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Activity Logs methods
    // In ApiClient.java - update the getActivityLogs method:
    public static List<ActivityLog> getActivityLogs() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/activity-logs"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Configure ObjectMapper to handle Java 8 dates
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                return mapper.readValue(response.body(), new TypeReference<List<ActivityLog>>() {});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of(); // Return empty list on error
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
    // Document methods
    public static List<Document> getDocuments() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/documents"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), new TypeReference<List<Document>>() {});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
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

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/documents"))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(data))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteDocument(int docId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/documents/" + docId))
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // User methods
    public static List<User> getUsers() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/users"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), new TypeReference<List<User>>() {});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }

    public static boolean createUser(User user) {
        try {
            String jsonBody = objectMapper.writeValueAsString(user);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/users"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateUser(User user) {
        try {
            String jsonBody = objectMapper.writeValueAsString(user);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/users/" + user.getId()))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteUser(int userId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/users/" + userId))
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Registration method
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

    // Remove the old isServerMode method and add this:
    public static boolean isServerAvailable() {
        return testConnection();
    }
}