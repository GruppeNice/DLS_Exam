package com.dlsexam.catalogservice.messaging;

import com.dlsexam.catalogservice.service.CatalogService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReviewEventListener {

    private static final Logger logger = LoggerFactory.getLogger(ReviewEventListener.class);

    private final CatalogService catalogService;
    private final ObjectMapper objectMapper;

    public ReviewEventListener(CatalogService catalogService, ObjectMapper objectMapper) {
        this.catalogService = catalogService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "#{reviewEventsQueue.name}")
    public void handleReviewEvent(Message message) {
        try {
            String routingKey = message.getMessageProperties().getReceivedRoutingKey();
            Map<String, Object> payload = objectMapper.readValue(message.getBody(), new TypeReference<>() {
            });
            UUID contentId = parseUuid(payload.get("contentId"));
            if (contentId == null) {
                logger.debug("Ignoring review event without contentId");
                return;
            }

            if ("content.rated".equals(routingKey)) {
                catalogService.applyContentRated(contentId, parseStars(payload));
            } else if ("content.reviewed".equals(routingKey)) {
                catalogService.applyContentReviewed(contentId);
            }
        } catch (Exception exception) {
            logger.warn("Failed to process review event: {}", exception.getMessage());
        }
    }

    private UUID parseUuid(Object value) {
        if (value == null) {
            return null;
        }
        return UUID.fromString(value.toString());
    }

    private int parseStars(Map<String, Object> payload) {
        Object stars = payload.get("stars");
        if (stars == null) {
            stars = payload.get("rating");
        }
        if (stars == null) {
            return 3;
        }
        return Integer.parseInt(stars.toString());
    }
}
