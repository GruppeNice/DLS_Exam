package com.engagementservice.exception;

public class NotificationException extends RuntimeException {
    public NotificationException(String message, Exception cause) {
        super(message, cause);
    }
}
