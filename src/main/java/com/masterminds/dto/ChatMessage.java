package com.masterminds.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor // <--- This fixes the Jackson error
@AllArgsConstructor // <--- Required when NoArgsConstructor is used with Builder
public class ChatMessage {

	private UUID messageId;
	private String content; // The actual text (e.g., "Hello!")
	private UUID senderId; // Who sent it?
	private UUID recipientId; // Who is it for?
	private LocalDateTime timestamp; // When was it sent?
	private MessageType type; // CHAT, JOIN, or LEAVE
	private String status;      // String to hold "SENT", "DELIVERED", or "READ"
	
	public enum MessageType {
	    CHAT,
	    READ_RECEIPT,
	    TYPING,
	    DELIVERED_RECEIPT
	}
}
