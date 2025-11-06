//package com.example.offlinedocumenthub;
//
//import com.example.offlinedocumenthub.dto.*;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.OutputStreamWriter;
//import java.io.*;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.nio.charset.*;
//import java.nio.file.Files;
//import java.time.Duration;
//import java.util.HashMap;
//import java.util.List;
//import java.util.*;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import com.fasterxml.jackson.databind.SerializationFeature;
//
//
//public class ApiClient {
//    private static String baseUrl = "http://localhost:8080/api";
//    private static final HttpClient httpClient = HttpClient.newBuilder()
//            .connectTimeout(Duration.ofSeconds(10))
//            .build();
////    private static final ObjectMapper objectMapper = new ObjectMapper();
//    private static final ObjectMapper objectMapper = new ObjectMapper()
//            .registerModule(new JavaTimeModule())
//            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//
//
//    public static void setServerAddress(String host) {
//        baseUrl = "http://" + host + ":8080/api";
//    }
//
//    // Login method
//    public static boolean login(String username, String password) {
//        try {
//            Map<String, String> formData = new HashMap<>();
//            formData.put("username", username);
//            formData.put("password", password);
//
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(baseUrl + "/login"))
//                    .header("Content-Type", "application/x-www-form-urlencoded")
//                    .POST(buildFormDataFromMap(formData))
//                    .build();
//
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//
//            if (response.statusCode() == 200) {
//                String responseBody = response.body();
//                ApiResponse<Map<String, Object>> apiResponse = objectMapper.readValue(
//                        responseBody,
//                        new TypeReference<ApiResponse<Map<String, Object>>>() {}
//                );
//
//                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
//                    Map<String, Object> userData = apiResponse.getData();
//                    int userId = ((Number) userData.get("userId")).intValue();
//                    String role = (String) userData.get("role");
//                    String fullName = (String) userData.get("fullName");
//
//                    // Set session
//                    SessionManager.login(userId, username, role, fullName);
//                    return true;
//                }
//            }
//            return false;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    // Activity Logs methods
//    // In ApiClient.java - update the getActivityLogs method:
//    public static List<ActivityLog> getActivityLogs() {
//        try {
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(baseUrl + "/activity-logs"))
//                    .GET()
//                    .build();
//
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//
//            if (response.statusCode() == 200) {
//                // Configure ObjectMapper to handle Java 8 dates
//                ObjectMapper mapper = new ObjectMapper();
//                mapper.registerModule(new JavaTimeModule());
//                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//
//                return mapper.readValue(response.body(), new TypeReference<List<ActivityLog>>() {});
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return List.of(); // Return empty list on error
//    }
//
//    // Test connection method
//    public static boolean testConnection() {
//        try {
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(baseUrl + "/system/health"))
//                    .GET()
//                    .timeout(Duration.ofSeconds(5))
//                    .build();
//
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//            return response.statusCode() == 200;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    private static HttpRequest.BodyPublisher buildFormDataFromMap(Map<String, String> data) {
//        StringBuilder builder = new StringBuilder();
//        for (Map.Entry<String, String> entry : data.entrySet()) {
//            if (builder.length() > 0) {
//                builder.append("&");
//            }
//            builder.append(entry.getKey());
//            builder.append("=");
//            builder.append(entry.getValue());
//        }
//        return HttpRequest.BodyPublishers.ofString(builder.toString());
//    }
//    // Document methods
//    public static List<Document> getDocuments() {
//        try {
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(baseUrl + "/documents"))
//                    .GET()
//                    .build();
//
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//
//            if (response.statusCode() == 200) {
//                return objectMapper.readValue(response.body(), new TypeReference<List<Document>>() {});
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return List.of();
//    }
//
//    public static boolean createDocument(String title, String filePath, File file) {
//        try {
//            // Create multipart form data for file upload
//            var boundary = "-------------" + System.currentTimeMillis();
//
//            // Build multipart request
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            PrintWriter writer = new PrintWriter(new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8), true);
//
//            // Add form fields
//            writer.append("--" + boundary).append("\r\n");
//            writer.append("Content-Disposition: form-data; name=\"title\"").append("\r\n");
//            writer.append("Content-Type: text/plain; charset=UTF-8").append("\r\n");
//            writer.append("\r\n");
//            writer.append(title).append("\r\n");
//            writer.flush();
//
//            // Add file
//            writer.append("--" + boundary).append("\r\n");
//            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"").append("\r\n");
//            writer.append("Content-Type: application/octet-stream").append("\r\n");
//            writer.append("Content-Transfer-Encoding: binary").append("\r\n");
//            writer.append("\r\n");
//            writer.flush();
//
//            Files.copy(file.toPath(), byteArrayOutputStream);
//            writer.append("\r\n");
//            writer.flush();
//
//            // End of multipart
//            writer.append("--" + boundary + "--").append("\r\n");
//            writer.flush();
//
//            byte[] data = byteArrayOutputStream.toByteArray();
//
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(baseUrl + "/documents"))
//                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
//                    .POST(HttpRequest.BodyPublishers.ofByteArray(data))
//                    .build();
//
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//            return response.statusCode() == 200;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public static boolean deleteDocument(int docId) {
//        try {
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(baseUrl + "/documents/" + docId))
//                    .DELETE()
//                    .build();
//
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//            return response.statusCode() == 200;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    // User methods
//    public static List<User> getUsers() {
//        try {
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(baseUrl + "/users"))
//                    .GET()
//                    .build();
//
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//
//            if (response.statusCode() == 200) {
//                return objectMapper.readValue(response.body(), new TypeReference<List<User>>() {});
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return List.of();
//    }
//
//    public static boolean createUser(User user) {
//        try {
//            String jsonBody = objectMapper.writeValueAsString(user);
//
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(baseUrl + "/users"))
//                    .header("Content-Type", "application/json")
//                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
//                    .build();
//
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//            return response.statusCode() == 200;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public static boolean updateUser(User user) {
//        try {
//            String jsonBody = objectMapper.writeValueAsString(user);
//
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(baseUrl + "/users/" + user.getId()))
//                    .header("Content-Type", "application/json")
//                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
//                    .build();
//
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//            return response.statusCode() == 200;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public static boolean deleteUser(int userId) {
//        try {
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(baseUrl + "/users/" + userId))
//                    .DELETE()
//                    .build();
//
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//            return response.statusCode() == 200;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    // Registration method
//    public static boolean register(String username, String email, String password) {
//        try {
//            Map<String, String> formData = new HashMap<>();
//            formData.put("username", username);
//            formData.put("email", email);
//            formData.put("password", password);
//
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(baseUrl + "/register"))
//                    .header("Content-Type", "application/x-www-form-urlencoded")
//                    .POST(buildFormDataFromMap(formData))
//                    .build();
//
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//            return response.statusCode() == 200;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    // Remove the old isServerMode method and add this:
//    public static boolean isServerAvailable() {
//        return testConnection();
//    }
//}


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
//    private static final ObjectMapper objectMapper = new ObjectMapper()
//            .registerModule(new JavaTimeModule())
//            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
// FIXED: Configure ObjectMapper for both serialization and deserialization
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // Store the authentication token after login
    private static String authToken = null;

    public static void setServerAddress(String host) {
        baseUrl = "http://" + host + ":8080/api";
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

//    public static String getAuthToken() {
//        return authToken;
//    }
//
//    public static boolean isAuthenticated() {
//        return authToken != null;
//    }

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

    // In ApiClient.java - add these backup methods
    public static Map<String, Object> triggerBackup() {
        try {
            HttpRequest request = addAuthHeader(HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/backup/trigger"))
                    .POST(HttpRequest.BodyPublishers.noBody()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
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
}