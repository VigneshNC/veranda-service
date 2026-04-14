package com.masterminds.dto;

import java.awt.TrayIcon.MessageType;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class ChatMessage {

	private String content;      // The actual text (e.g., "Hello!")
    private UUID senderId;       // Who sent it?
    private UUID recipientId;    // Who is it for?
    private LocalDateTime timestamp;      // When was it sent?
    private MessageType type;    // CHAT, JOIN, or LEAVE

}
