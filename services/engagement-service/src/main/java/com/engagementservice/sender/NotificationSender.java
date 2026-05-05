package com.engagementservice.sender;


import com.engagementservice.exception.NotificationException;
import com.engagementservice.model.Notification;
import com.engagementservice.types.NotificationType;

public interface NotificationSender {

    // Each sender handles a specific type
    NotificationType getType();

    // Send the notification, throw exception on failure
    void send(Notification notification) throws NotificationException;
}
