package com.engagementservice.dto;

import com.engagementservice.types.NotificationType;
import lombok.Data;

import java.util.Map;

@Data
public class NotificationRequest {

    private NotificationType type;

    private String recipient;

    private String subject;

    private String templateName;

    private Map<String, Object> templateVariables;
}
