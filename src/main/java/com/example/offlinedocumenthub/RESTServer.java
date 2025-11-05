package com.example.offlinedocumenthub;

import com.example.offlinedocumenthub.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.List;
import java.util.stream.Collectors;

public class RESTServer {
    private static final int PORT = 8080;
    private static Javalin app;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void startRESTServer() {
        app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
            config.plugins.enableCors(cors -> {
                cors.add(it -> {
                    it.anyHost();
                });
            });
        }).start("0.0.0.0", PORT);

        setupRoutes();
        System.out.println("🚀 REST Server started on port " + PORT);
        System.out.println("📡 API available at: http://localhost:" + PORT + "/api");
    }

    private static void setupRoutes() {
        // Authentication endpoints
        app.post("/api/login", RESTServer::handleLogin);
        app.post("/api/logout", RESTServer::handleLogout);

        // Document management endpoints
        app.get("/api/documents", RESTServer::getAllDocuments);
        app.post("/api/documents", RESTServer::createDocument);
        app.put("/api/documents/{id}", RESTServer::updateDocument);
        app.delete("/api/documents/{id}", RESTServer::deleteDocument);
        app.get("/api/documents/download/{id}", RESTServer::downloadDocument);

        // System endpoints
        app.post("/api/backup", RESTServer::triggerBackup);
    }

    private static void handleLogin(Context ctx) {
        try {
            LoginRequest loginRequest = ctx.bodyAsClass(LoginRequest.class);
            ctx.json(new ApiResponse<>(true, "Login endpoint ready"));
        } catch (Exception e) {
            ctx.json(new ApiResponse<>(false, "Login failed: " + e.getMessage()));
        }
    }

    private static void handleLogout(Context ctx) {
        ctx.json(new ApiResponse<>(true, "Logout endpoint ready"));
    }

    private static void getAllDocuments(Context ctx) {
        try {
            ctx.json(new ApiResponse<>(true, "Documents endpoint ready"));
        } catch (Exception e) {
            ctx.json(new ApiResponse<>(false, "Failed to load documents: " + e.getMessage()));
        }
    }

    private static void createDocument(Context ctx) {
        ctx.json(new ApiResponse<>(true, "Create document endpoint ready"));
    }

    private static void updateDocument(Context ctx) {
        ctx.json(new ApiResponse<>(true, "Update document endpoint ready"));
    }

    private static void deleteDocument(Context ctx) {
        ctx.json(new ApiResponse<>(true, "Delete document endpoint ready"));
    }

    private static void downloadDocument(Context ctx) {
        ctx.json(new ApiResponse<>(true, "Download document endpoint ready"));
    }

    private static void triggerBackup(Context ctx) {
        ctx.json(new ApiResponse<>(true, "Backup endpoint ready"));
    }

    public static void stopServer() {
        if (app != null) {
            app.stop();
        }
    }
}