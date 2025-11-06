//package com.example.offlinedocumenthub;
//
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Node;
//import javafx.scene.control.Button;
//import javafx.scene.layout.StackPane;
//import javafx.scene.layout.VBox;
//import javafx.scene.Scene;
//import javafx.stage.Stage;
//
//import java.io.IOException;
//
//public class AdminDashboardController {
//
//    @FXML private StackPane contentPane;
//    @FXML private VBox leftMenu;
//    @FXML private Button btnUsers;
//    @FXML private Button btnDocuments;
//    @FXML private Button btnActivityLogs;
//    @FXML private Button btnSystemControl;
//
//    @FXML
//    public void initialize() {
//        setupRoleBasedAccess();
//    }
//
//    private void setupRoleBasedAccess() {
//        // Check user role and show/hide buttons accordingly
//        if (SessionManager.isLoggedIn()) {
//            String role = SessionManager.getCurrentRole();
//            String username = SessionManager.getCurrentUsername();
//
//            System.out.println("User logged in: " + username + " with role: " + role);
//
//            if ("admin".equals(role)) {
//                // Admin can see all buttons
//                btnUsers.setVisible(true);
//                btnUsers.setManaged(true);
//                btnActivityLogs.setVisible(true);
//                btnActivityLogs.setManaged(true);
//                btnSystemControl.setVisible(true);
//                btnSystemControl.setManaged(true);
//                btnDocuments.setVisible(true);
//                btnDocuments.setManaged(true);
//            } else {
//                // Student can only see Documents button
//                btnUsers.setVisible(false);
//                btnUsers.setManaged(false);
//                btnActivityLogs.setVisible(false);
//                btnActivityLogs.setManaged(false);
//                btnSystemControl.setVisible(false);
//                btnSystemControl.setManaged(false);
//                btnDocuments.setVisible(true);
//                btnDocuments.setManaged(true);
//
//                // Also update the button text for students
//                btnDocuments.setText("Shared Documents");
//            }
//        } else {
//            // If not logged in, hide all buttons (shouldn't happen, but safety check)
//            btnUsers.setVisible(false);
//            btnUsers.setManaged(false);
//            btnActivityLogs.setVisible(false);
//            btnActivityLogs.setManaged(false);
//            btnSystemControl.setVisible(false);
//            btnSystemControl.setManaged(false);
//            btnDocuments.setVisible(false);
//            btnDocuments.setManaged(false);
//        }
//    }
//
//    @FXML
//    private void showUserManagement() {
//        if (!SessionManager.isAdmin()) {
//            showAccessDeniedAlert();
//            return;
//        }
//        loadPane("user-management-view.fxml");
//    }
//
//    @FXML
//    private void showDocumentManagement() {
//        loadPane("document-management-view.fxml");
//    }
//
//    @FXML
//    private void showActivityLogs() {
//        if (!SessionManager.isAdmin()) {
//            showAccessDeniedAlert();
//            return;
//        }
//        loadPane("activity-log-view.fxml");
//    }
//
//    @FXML
//    private void showSystemControl() {
//        if (!SessionManager.isAdmin()) {
//            showAccessDeniedAlert();
//            return;
//        }
//        loadPane("system-control-view.fxml");
//    }
//
//    @FXML
//    private void logout() {
//        try {
//            // Clear session
//            SessionManager.logout();
//            ApiClient.logout();
//
//            // Navigate back to login
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
//            Scene scene = new Scene(loader.load(), 800, 600);
//            Stage stage = (Stage) contentPane.getScene().getWindow();
//            stage.setScene(scene);
//            stage.setTitle("Offline Document Hub - Login");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void loadPane(String fxmlFile) {
//        try {
//            Node pane = FXMLLoader.load(getClass().getResource(fxmlFile));
//            contentPane.getChildren().clear();
//            contentPane.getChildren().add(pane);
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.err.println("Failed to load: " + fxmlFile);
//        }
//    }
//
//    private void showAccessDeniedAlert() {
//        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
//        alert.setTitle("Access Denied");
//        alert.setHeaderText("Insufficient Permissions");
//        alert.setContentText("You do not have permission to access this feature. Please contact an administrator.");
//        alert.showAndWait();
//    }
//}

package com.example.offlinedocumenthub;

import com.example.offlinedocumenthub.dto.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
//import javafx.scene.control.Text;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminDashboardController {

    @FXML private StackPane contentPane;
    @FXML private VBox leftMenu;
    @FXML private VBox welcomePane;
    @FXML private Button btnUsers;
    @FXML private Button btnDocuments;
    @FXML private Button btnActivityLogs;
    @FXML private Button btnSystemControl;

    // Welcome pane components
    @FXML private Label lblWelcome;
    @FXML private Label lblUserWelcome;
    @FXML private Label lblDocumentCount;
    @FXML private Label lblUserCount;
    @FXML private Label lblActivityCount;
    @FXML private VBox documentsCard;
    @FXML private VBox usersCard;
    @FXML private VBox activityCard;
    @FXML private Button btnQuickUpload;
    @FXML private Button btnQuickViewDocs;
    @FXML private Button btnQuickUsers;
    @FXML private Text txtProjectInfo;
    @FXML private Label lblServerStatus;
    @FXML private Label lblDatabaseStatus;
    @FXML private Label lblLastLogin;

    private MessagePollingService messagePollingService;
    private VBox messagesPane;
    private VBox conversationsPane;
    private VBox chatPane;
    private TextField messageInput;
    private ScrollPane chatScrollPane;
    private VBox chatMessagesContainer;
    private int currentChatUserId = -1;

    @FXML
    public void initialize() {
        setupRoleBasedAccess();
        loadDashboardData();
        showWelcomePane();
        lblWelcome.setText("Welcome, " + SessionManager.getCurrentFullName() + " (" + SessionManager.getCurrentRole() + ")");
        lblUserWelcome.setText("Hello " + SessionManager.getCurrentFullName() + "! Welcome to your dashboard.");
        initializeMessaging();
        // Stop polling when controller is destroyed
        contentPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && messagePollingService != null) {
                messagePollingService.cancel();
            }
        });
        refreshDashboard();
    }


    private void setupRoleBasedAccess() {
        if (SessionManager.isLoggedIn()) {
            String role = SessionManager.getCurrentRole();
            String username = SessionManager.getCurrentUsername();
            String fullName = SessionManager.getCurrentFullName();

            // Update welcome labels
            lblWelcome.setText("Welcome, " + fullName + " (" + role + ")");
            lblUserWelcome.setText("Hello " + fullName + "! Welcome back to Offline Document Hub");

            if ("admin".equals(role)) {
                // Admin can see all buttons
                setButtonVisibility(true, true, true, true);
                setQuickActionVisibility(true, true, true);
                setCardVisibility(true, true, true);
            } else {
                // Student can only see Documents button
                setButtonVisibility(false, true, false, false);
                setQuickActionVisibility(true, true, false);
                setCardVisibility(true, false, false);

                // Update text for students
                btnDocuments.setText("My Documents");
                btnQuickViewDocs.setText("📋 View My Documents");
            }
        }
    }

    private void setButtonVisibility(boolean users, boolean documents, boolean activityLogs, boolean system) {
        btnUsers.setVisible(users);
        btnUsers.setManaged(users);
        btnActivityLogs.setVisible(activityLogs);
        btnActivityLogs.setManaged(activityLogs);
        btnSystemControl.setVisible(system);
        btnSystemControl.setManaged(system);
        btnDocuments.setVisible(documents);
        btnDocuments.setManaged(documents);
    }

    private void setQuickActionVisibility(boolean upload, boolean viewDocs, boolean users) {
        btnQuickUpload.setVisible(upload);
        btnQuickUpload.setManaged(upload);
        btnQuickViewDocs.setVisible(viewDocs);
        btnQuickViewDocs.setManaged(viewDocs);
        btnQuickUsers.setVisible(users);
        btnQuickUsers.setManaged(users);
    }

    private void setCardVisibility(boolean documents, boolean users, boolean activity) {
        documentsCard.setVisible(documents);
        documentsCard.setManaged(documents);
        usersCard.setVisible(users);
        usersCard.setManaged(users);
        activityCard.setVisible(activity);
        activityCard.setManaged(activity);
    }

    // Add this method to initialize messaging
    private void initializeMessaging() {
        try {
            messagePollingService = new MessagePollingService();
            messagePollingService.setPeriod(Duration.seconds(2));
            messagePollingService.setOnNewConversations(this::updateConversations);
            messagePollingService.setOnNewMessages(this::updateChatMessages);
        } catch (Exception e) {
            System.err.println("Error initializing messaging: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // Add the showMessages method
    @FXML
    private void showMessages() {
        try {
            // Ensure messaging is initialized
            if (messagePollingService == null) {
                initializeMessaging();
            }

            if (messagesPane == null) {
                createMessagesUI();
            }
            contentPane.getChildren().clear();
            contentPane.getChildren().add(messagesPane);

            // Start polling for conversations
            messagePollingService.setCurrentChatUserId(-1);
            messagePollingService.restart();
            loadConversations();
        } catch (Exception e) {
            System.err.println("Error showing messages: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to load messages: " + e.getMessage());
        }
    }

    private void createMessagesUI() {
        messagesPane = new VBox(10);
        messagesPane.setStyle("-fx-padding: 15; -fx-background-color: white;");
        messagesPane.setPrefSize(800, 600);

        // Header
        Label headerLabel = new Label("💬 Messages");
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Start New Conversation Button
        Button newConversationBtn = new Button("➕ Start New Conversation");
        newConversationBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        newConversationBtn.setOnAction(e -> showUserSelectionDialog());

        // Main content split pane
        SplitPane splitPane = new SplitPane();
        splitPane.setPrefSize(780, 520);

        // Conversations panel (left)
        conversationsPane = new VBox(10);
        conversationsPane.setPrefWidth(300);
        conversationsPane.setStyle("-fx-padding: 10;");

        Label conversationsLabel = new Label("Conversations");
        conversationsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        ScrollPane conversationsScroll = new ScrollPane();
        conversationsScroll.setFitToWidth(true);
        conversationsScroll.setStyle("-fx-background: transparent; -fx-border-color: #ddd;");

        VBox conversationsList = new VBox(5);
        conversationsScroll.setContent(conversationsList);

        conversationsPane.getChildren().addAll(conversationsLabel, conversationsScroll);

        // Chat panel (right)
        chatPane = new VBox(10);
        chatPane.setStyle("-fx-padding: 10;");

        // Chat header (will be set when a conversation is selected)
        HBox chatHeader = new HBox();
        chatHeader.setStyle("-fx-border-color: #ddd; -fx-border-width: 0 0 1 0; -fx-padding: 0 0 10 0;");

        // Chat messages area
        chatScrollPane = new ScrollPane();
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setStyle("-fx-background: transparent;");
        chatScrollPane.setVvalue(1.0);

        chatMessagesContainer = new VBox(10);
        chatMessagesContainer.setStyle("-fx-padding: 10;");
        chatScrollPane.setContent(chatMessagesContainer);

        // Message input area
        HBox inputArea = new HBox(10);
        messageInput = new TextField();
        messageInput.setPromptText("Type a message...");
        messageInput.setPrefWidth(400);

        Button sendButton = new Button("Send");
        sendButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        sendButton.setOnAction(e -> sendMessage());

        messageInput.setOnAction(e -> sendMessage());

        inputArea.getChildren().addAll(messageInput, sendButton);

        chatPane.getChildren().addAll(chatHeader, chatScrollPane, inputArea);

        splitPane.getItems().addAll(conversationsPane, chatPane);
        messagesPane.getChildren().addAll(headerLabel, newConversationBtn, splitPane);
    }

    private void showUserSelectionDialog() {
        try {
            // Get all users from the server
            List<User> allUsers = ApiClient.getUsers();

            // Filter out current user
            List<User> otherUsers = allUsers.stream()
                    .filter(user -> user.getId() != SessionManager.getCurrentUserId())
                    .collect(Collectors.toList());

            if (otherUsers.isEmpty()) {
                showAlert("No Users", "No other users found to start a conversation with.");
                return;
            }

            // Create dialog
            Dialog<User> dialog = new Dialog<>();
            dialog.setTitle("Start New Conversation");
            dialog.setHeaderText("Select a user to start chatting with:");

            // Set the button types
            ButtonType startChatButtonType = new ButtonType("Start Chat", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(startChatButtonType, ButtonType.CANCEL);

            // Create the user list
            ListView<User> userListView = new ListView<>();
            userListView.setCellFactory(lv -> new ListCell<User>() {
                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    if (empty || user == null) {
                        setText(null);
                    } else {
                        String displayText = user.getFullName() != null && !user.getFullName().isEmpty()
                                ? user.getFullName() + " (" + user.getUsername() + ")"
                                : user.getUsername();
                        setText(displayText);
                    }
                }
            });

            userListView.getItems().addAll(otherUsers);
            userListView.setPrefSize(300, 200);

            VBox content = new VBox(10);
            content.setStyle("-fx-padding: 10;");
            content.getChildren().addAll(new Label("Available Users:"), userListView);
            dialog.getDialogPane().setContent(content);

            // Enable/disable start button based on selection
            Node startButton = dialog.getDialogPane().lookupButton(startChatButtonType);
            startButton.setDisable(true);

            userListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                startButton.setDisable(newVal == null);
            });

            // Convert result to User when start button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == startChatButtonType) {
                    return userListView.getSelectionModel().getSelectedItem();
                }
                return null;
            });

            Optional<User> result = dialog.showAndWait();
            result.ifPresent(this::startNewConversation);

        } catch (Exception e) {
            System.err.println("Error showing user selection: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to load users: " + e.getMessage());
        }
    }

    private void startNewConversation(User selectedUser) {
        if (selectedUser != null) {
            // Open chat with the selected user
            openChat(selectedUser.getId(),
                    selectedUser.getFullName() != null && !selectedUser.getFullName().isEmpty()
                            ? selectedUser.getFullName()
                            : selectedUser.getUsername());

            // Show welcome message in the chat
            showWelcomeMessage(selectedUser);
        }
    }

    private void showWelcomeMessage(User selectedUser) {
        String welcomeMessage = "You started a conversation with " +
                (selectedUser.getFullName() != null && !selectedUser.getFullName().isEmpty()
                        ? selectedUser.getFullName()
                        : selectedUser.getUsername());

        HBox welcomeBubble = createSystemMessageBubble(welcomeMessage);
        chatMessagesContainer.getChildren().add(welcomeBubble);

        // Scroll to bottom
        Platform.runLater(() -> {
            chatScrollPane.setVvalue(1.0);
        });
    }
    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    private HBox createSystemMessageBubble(String message) {
        HBox messageContainer = new HBox();
        messageContainer.setAlignment(Pos.CENTER);
        messageContainer.setMaxWidth(400);

        Label systemLabel = new Label(message);
        systemLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px; -fx-font-style: italic;");
        systemLabel.setPadding(new Insets(5, 10, 5, 10));

        messageContainer.getChildren().add(systemLabel);
        return messageContainer;
    }
    private void loadConversations() {
        List<Message> conversations = ApiClient.getConversations();
        updateConversations(conversations);
    }

    private void updateConversations(List<Message> conversations) {
        VBox conversationsList = (VBox) ((ScrollPane) conversationsPane.getChildren().get(1)).getContent();
        conversationsList.getChildren().clear();

        for (Message conversation : conversations) {
            int otherUserId = (conversation.getSenderId() == SessionManager.getCurrentUserId())
                    ? conversation.getReceiverId()
                    : conversation.getSenderId();

            String otherUserName = (conversation.getSenderId() == SessionManager.getCurrentUserId())
                    ? conversation.getReceiverName()
                    : conversation.getSenderName();

            HBox conversationItem = createConversationItem(otherUserId, otherUserName,
                    conversation.getMessageText(), conversation.getSentDate(), !conversation.isRead());

            conversationsList.getChildren().add(conversationItem);
        }
    }

    private HBox createConversationItem(int userId, String userName, String lastMessage,
                                        LocalDateTime timestamp, boolean hasUnread) {
        HBox item = new HBox(10);
        item.setStyle("-fx-padding: 10; -fx-background-color: " + (hasUnread ? "#ecf0f1" : "white") +
                "; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");
        item.setPrefWidth(280);

        item.setOnMouseClicked(e -> openChat(userId, userName));

        Circle avatar = new Circle(20);
        avatar.setFill(Color.LIGHTBLUE);

        VBox textContainer = new VBox(2);
        Label nameLabel = new Label(userName);
        nameLabel.setStyle("-fx-font-weight: " + (hasUnread ? "bold" : "normal") + ";");

        Label messageLabel = new Label(lastMessage.length() > 30 ? lastMessage.substring(0, 30) + "..." : lastMessage);
        messageLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");

        textContainer.getChildren().addAll(nameLabel, messageLabel);

        VBox timeContainer = new VBox();
        Label timeLabel = new Label(formatTime(timestamp));
        timeLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 10px;");
        timeContainer.getChildren().add(timeLabel);

        HBox.setHgrow(textContainer, Priority.ALWAYS);
        item.getChildren().addAll(avatar, textContainer, timeContainer);

        return item;
    }

    private void openChat(int userId, String userName) {
        currentChatUserId = userId;

        // Update chat header
        HBox chatHeader = (HBox) chatPane.getChildren().get(0);
        chatHeader.getChildren().clear();
        Label chatTitle = new Label("Chat with " + userName);
        chatTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        chatHeader.getChildren().add(chatTitle);

        // Clear previous messages
        chatMessagesContainer.getChildren().clear();

        // Load messages
        loadChatMessages();

        // Mark messages as read
        ApiClient.markMessagesAsRead(userId);

        // Switch polling to current chat
        messagePollingService.setCurrentChatUserId(userId);
    }

    private void loadChatMessages() {
        List<Message> messages = ApiClient.getMessages(currentChatUserId);
        updateChatMessages(messages);
    }

    private void updateChatMessages(List<Message> messages) {
        chatMessagesContainer.getChildren().clear();

        for (Message message : messages) {
            HBox messageBubble = createMessageBubble(message);
            chatMessagesContainer.getChildren().add(messageBubble);
        }

        // Scroll to bottom
        Platform.runLater(() -> {
            chatScrollPane.setVvalue(1.0);
        });
    }

    private HBox createMessageBubble(Message message) {
        HBox messageContainer = new HBox();
        messageContainer.setMaxWidth(400);

        VBox bubble = new VBox(5);
        bubble.setStyle("-fx-background-color: " +
                (message.getSenderId() == SessionManager.getCurrentUserId() ? "#3498db" : "#ecf0f1") +
                "; -fx-background-radius: 15; -fx-padding: 10;");
        bubble.setMaxWidth(350);

        Label messageText = new Label(message.getMessageText());
        messageText.setStyle("-fx-text-fill: " +
                (message.getSenderId() == SessionManager.getCurrentUserId() ? "white" : "black") +
                "; -fx-wrap-text: true;");
        messageText.setMaxWidth(330);

        Label timeLabel = new Label(formatTime(message.getSentDate()));
        timeLabel.setStyle("-fx-text-fill: " +
                (message.getSenderId() == SessionManager.getCurrentUserId() ? "rgba(255,255,255,0.7)" : "#7f8c8d") +
                "; -fx-font-size: 10px;");

        bubble.getChildren().addAll(messageText, timeLabel);

        if (message.getSenderId() == SessionManager.getCurrentUserId()) {
            // My message - align right
            messageContainer.setAlignment(Pos.CENTER_RIGHT);
            HBox.setHgrow(messageContainer, Priority.ALWAYS);
        } else {
            // Their message - align left
            messageContainer.setAlignment(Pos.CENTER_LEFT);
        }

        messageContainer.getChildren().add(bubble);
        return messageContainer;
    }

    private void sendMessage() {
        String text = messageInput.getText().trim();
        if (!text.isEmpty() && currentChatUserId > 0) {
            boolean success = ApiClient.sendMessage(currentChatUserId, text);
            if (success) {
                messageInput.clear();
                // Message will appear in next poll
            }
        }
    }

    private String formatTime(LocalDateTime timestamp) {
        if (timestamp == null) return "";

        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("HH:mm");
        return timestamp.format(formatter);
    }



    private void loadDashboardData() {
        try {
            // Load document count
            List<Document> documents = ApiClient.getDocuments();
            lblDocumentCount.setText(String.valueOf(documents != null ? documents.size() : 0));

            // Load user count (admin only)
            if (SessionManager.isAdmin()) {
                List<User> users = ApiClient.getUsers();
                lblUserCount.setText(String.valueOf(users != null ? users.size() : 0));
            }

            // Load activity count (admin only)
            if (SessionManager.isAdmin()) {
                List<ActivityLog> activities = ApiClient.getActivityLogs();
                lblActivityCount.setText(String.valueOf(activities != null ? activities.size() : 0));
            }

            // Check system status
            boolean serverOnline = ApiClient.testConnection();
            lblServerStatus.setText("● Server: " + (serverOnline ? "Online" : "Offline"));
            lblServerStatus.setStyle(serverOnline ? "-fx-text-fill: #27ae60; -fx-font-weight: bold;" : "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

            // Database status (simplified - we assume if server is online, DB is connected)
            lblDatabaseStatus.setText("● Database: " + (serverOnline ? "Connected" : "Disconnected"));
            lblDatabaseStatus.setStyle(serverOnline ? "-fx-text-fill: #27ae60; -fx-font-weight: bold;" : "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

            // Last login
            lblLastLogin.setText("Last login: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));

        } catch (Exception e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
            lblServerStatus.setText("● Server: Error");
            lblServerStatus.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }
    }

    private void showWelcomePane() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(welcomePane);
    }

    @FXML
    private void showUserManagement() {
        if (!SessionManager.isAdmin()) {
            showAccessDeniedAlert();
            return;
        }
        loadPane("user-management-view.fxml");
    }

    @FXML
    private void showDocumentManagement() {
        loadPane("document-management-view.fxml");
    }

    @FXML
    private void showActivityLogs() {
        if (!SessionManager.isAdmin()) {
            showAccessDeniedAlert();
            return;
        }
        loadPane("activity-log-view.fxml");
    }

    @FXML
        private void showSystemControl() {
        if (!SessionManager.isAdmin()) {
            showAccessDeniedAlert();
            return;
        }
        loadPane("system-control-view.fxml");
    }

    @FXML
    private void onBackToDashboard() {
        try {
            Stage stage = (Stage) contentPane.getScene().getWindow(); // Get current stage
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("admin-dashboard-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 1200, 700); // Set dimensions
            stage.setScene(scene);
            stage.setTitle("Dashboard - Offline Document Hub");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            // Show error message to user
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Failed to load dashboard");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void quickUpload() {
        // Simulate quick upload - you can enhance this with actual file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Document to Upload");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Word Documents", "*.doc", "*.docx"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        File file = fileChooser.showOpenDialog(contentPane.getScene().getWindow());
        if (file != null) {
            // Show quick upload dialog
            showInfo("Quick Upload", "Selected file: " + file.getName() + "\n\nPlease use the Documents section to complete the upload with title and description.");
            showDocumentManagement(); // Navigate to documents for completion
        }
    }

    @FXML
    private void refreshDashboard() {
        loadDashboardData();
        showInfo("Dashboard Refreshed", "Dashboard statistics updated successfully.");
    }

    private void loadPane(String fxmlFile) {
        try {
            Node pane = FXMLLoader.load(getClass().getResource(fxmlFile));
            contentPane.getChildren().clear();
            contentPane.getChildren().add(pane);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load: " + fxmlFile);
        }
    }

    @FXML
    private void logout() {
        try {
            SessionManager.logout();
            ApiClient.logout();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Offline Document Hub - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAccessDeniedAlert() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle("Access Denied");
        alert.setHeaderText("Insufficient Permissions");
        alert.setContentText("You do not have permission to access this feature. Please contact an administrator.");
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}