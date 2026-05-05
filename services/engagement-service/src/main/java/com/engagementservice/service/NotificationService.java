package com.engagementservice.service;

import com.engagementservice.config.RabbitConfig;
import com.engagementservice.dto.NotificationRequest;
import com.engagementservice.exception.NotificationException;
import com.engagementservice.model.Notification;
import com.engagementservice.repository.NotificationRepository;
import com.engagementservice.sender.NotificationSender;
import com.engagementservice.types.NotificationStatus;
import com.engagementservice.types.NotificationType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger log = Logger.getLogger(NotificationService.class.getName());

    private final NotificationRepository notificationRepository;
    private final TemplateEngine templateEngine;
    private final Map<NotificationType, NotificationSender> senders;
    private final RabbitTemplate rabbitTemplate;

    // Spring injects all NotificationSender implementations
    public NotificationService(NotificationRepository notificationRepository,
                               TemplateEngine templateEngine,
                               List<NotificationSender> senderList, RabbitTemplate rabbitTemplate) {
        this.notificationRepository = notificationRepository;
        this.templateEngine = templateEngine;

        // Build a map for quick lookup by type
        this.senders = senderList.stream()
                .collect(Collectors.toMap(
                        NotificationSender::getType,
                        Function.identity()
                ));
        this.rabbitTemplate = rabbitTemplate;
    }

    public Integer queueNotification(NotificationRequest request) {
        // Process template to generate content
        String content = processTemplate(request.getTemplateName(),
                request.getTemplateVariables());

        // Create notification record
        Notification notification = new Notification();
        notification.setType(request.getType());
        notification.setRecipient(request.getRecipient());
        notification.setSubject(request.getSubject());
        notification.setContent(content);
        notification.setStatus(NotificationStatus.QUEUED);
        notification.setCreatedAt(LocalDateTime.now());

       Notification savedNotification = notificationRepository.save(notification);

        rabbitTemplate.convertAndSend(RabbitConfig.NOTIFICATION_QUEUE, savedNotification.getId());

        return savedNotification.getId();
    }

    @Retryable(
            value = NotificationException.class,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public Integer sendWithRetry(Notification notification) {
        NotificationSender sender = senders.get(notification.getType());

        if (sender == null) throw new IllegalArgumentException("No sender for type: " + notification.getType());

        try {
            sender.send(notification);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            Notification savedNotification = notificationRepository.save(notification);
            log.info("Notification sent successfully: " + notification.getId());
            return savedNotification.getId();

        } catch (NotificationException e) {
            notification.setRetryCount(notification.getRetryCount() + 1);
            notification.setStatus(NotificationStatus.RETRY);
            Notification savedNotification = notificationRepository.save(notification);
            log.warning("Notification failed, will retry: " + savedNotification.getId() + ", exceptionMsg: " +  e.getMessage());
            throw e;
        }


    }

    @Recover
    public Integer recover(NotificationException e, Notification notification) {
        notification.setStatus(NotificationStatus.FAILED);
        Notification savedNotification = notificationRepository.save(notification);
        return savedNotification.getId();
    }

    private String processTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        if (variables != null) {
            variables.forEach(context::setVariable);
        }
        return templateEngine.process(templateName, context);
    }
}
