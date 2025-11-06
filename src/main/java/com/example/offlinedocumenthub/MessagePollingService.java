package com.example.offlinedocumenthub;

import com.example.offlinedocumenthub.dto.Message;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MessagePollingService extends ScheduledService<List<Message>> {
    private int currentChatUserId = -1;
    private Consumer<List<Message>> onNewMessages;
    private Consumer<List<Message>> onNewConversations;

    public MessagePollingService() {
        setDelay(Duration.millis(100)); // Initial delay
        setPeriod(Duration.seconds(2)); // Poll every 2 seconds
        setRestartOnFailure(true);
        setMaximumFailureCount(3);
    }

    public void setCurrentChatUserId(int userId) {
        this.currentChatUserId = userId;
    }

    public void setOnNewMessages(Consumer<List<Message>> callback) {
        this.onNewMessages = callback;
    }

    public void setOnNewConversations(Consumer<List<Message>> callback) {
        this.onNewConversations = callback;
    }

    @Override
    protected Task<List<Message>> createTask() {
        return new Task<>() {
            @Override
            protected List<Message> call() throws Exception {
                try {
                    if (currentChatUserId > 0) {
                        // Poll for new messages in current chat
                        List<Message> messages = ApiClient.getMessages(currentChatUserId);
                        return messages != null ? messages : new ArrayList<>();
                    } else {
                        // Poll for new conversations
                        List<Message> conversations = ApiClient.getConversations();
                        return conversations != null ? conversations : new ArrayList<>();
                    }
                } catch (Exception e) {
                    System.err.println("Error in message polling: " + e.getMessage());
                    return new ArrayList<>(); // Return empty list on error
                }
            }
        };
    }

    @Override
    protected void succeeded() {
        List<Message> messages = getValue();
        if (messages != null) {
            if (currentChatUserId > 0 && onNewMessages != null) {
                Platform.runLater(() -> onNewMessages.accept(messages));
            } else if (onNewConversations != null) {
                Platform.runLater(() -> onNewConversations.accept(messages));
            }
        }
        super.succeeded();
    }

    @Override
    protected void failed() {
        System.err.println("Message polling service failed: " + getException().getMessage());
        super.failed();
    }
}