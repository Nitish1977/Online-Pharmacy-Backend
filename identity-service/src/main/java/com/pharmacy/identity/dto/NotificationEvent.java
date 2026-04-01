package com.pharmacy.identity.dto;

import java.util.Map;

public class NotificationEvent {

    private NotificationType type;
    private String recipientEmail;
    private Map<String, Object> payload;

    public NotificationEvent(NotificationType type, String recipientEmail, Map<String, Object> payload) {
        this.type = type;
        this.recipientEmail = recipientEmail;
        this.payload = payload;
    }

    public NotificationEvent() {
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
