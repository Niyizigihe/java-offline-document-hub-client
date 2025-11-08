package com.example.offlinedocumenthub;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.util.Duration;
import java.util.function.Consumer;

public class SessionMonitorService extends Service<Void> {
    private static final int CHECK_INTERVAL_SECONDS = 5;
    private Runnable onSessionExpired;
    private Consumer<Integer> onSessionAboutToExpire; // Changed to Consumer<Integer>
    private Timeline warningTimeline;
    private Timeline countdownTimeline;

    public void setOnSessionExpired(Runnable callback) {
        this.onSessionExpired = callback;
    }

    public void setOnSessionAboutToExpire(Consumer<Integer> callback) { // Updated parameter type
        this.onSessionAboutToExpire = callback;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                System.out.println("=== SESSION MONITOR: Started monitoring service ===");

                Platform.runLater(() -> startWarningTimer());

                while (!isCancelled() && SessionManager.isLoggedIn()) {
                    try {
                        Thread.sleep(CHECK_INTERVAL_SECONDS * 1000);

                        if (SessionManager.isSessionExpired()) {
                            System.out.println("=== SESSION MONITOR: Session expired, triggering callback ===");
                            Platform.runLater(() -> {
                                if (onSessionExpired != null) {
                                    onSessionExpired.run();
                                }
                            });
                            break;
                        }
                    } catch (InterruptedException e) {
                        System.out.println("=== SESSION MONITOR: Service interrupted ===");
                        break;
                    }
                }
                System.out.println("=== SESSION MONITOR: Monitoring service stopped ===");
                return null;
            }
        };
    }

    private void startWarningTimer() {
        if (warningTimeline != null) {
            warningTimeline.stop();
        }

        warningTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> checkSessionWarning()) // Check every second
        );
        warningTimeline.setCycleCount(Timeline.INDEFINITE);
        warningTimeline.play();
    }

    private void checkSessionWarning() {
        if (!SessionManager.isLoggedIn()) {
            if (warningTimeline != null) {
                warningTimeline.stop();
            }
            if (countdownTimeline != null) {
                countdownTimeline.stop();
            }
            return;
        }

        int secondsLeft = SessionManager.getSecondsUntilExpiration();

        // Show warning when 30 seconds or less remain
        if (secondsLeft <= 30 && secondsLeft > 0 && onSessionAboutToExpire != null) {
            System.out.println("=== SESSION MONITOR: Sending countdown update - " + secondsLeft + " seconds left ===");
            Platform.runLater(() -> onSessionAboutToExpire.accept(secondsLeft));
        }

        // Auto-logout when time reaches zero
        if (secondsLeft <= 0 && onSessionExpired != null) {
            System.out.println("=== SESSION MONITOR: Time reached zero, forcing logout ===");
            Platform.runLater(() -> {
                if (onSessionExpired != null) {
                    onSessionExpired.run();
                }
            });
        }
    }

    public void startCountdownUpdates() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        countdownTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    if (SessionManager.isLoggedIn()) {
                        int secondsLeft = SessionManager.getSecondsUntilExpiration();
                        if (secondsLeft <= 30 && secondsLeft >= 0 && onSessionAboutToExpire != null) {
                            Platform.runLater(() -> onSessionAboutToExpire.accept(secondsLeft));
                        }
                    }
                })
        );
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    public void stopCountdownUpdates() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
            countdownTimeline = null;
        }
    }

    @Override
    protected void cancelled() {
        System.out.println("=== SESSION MONITOR: Service cancelled ===");
        if (warningTimeline != null) {
            warningTimeline.stop();
        }
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        super.cancelled();
    }
}