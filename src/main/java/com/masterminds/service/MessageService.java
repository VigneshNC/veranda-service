package com.masterminds.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.masterminds.dto.ChatMessage;
import com.masterminds.dto.ChatMessage.MessageType;
import com.masterminds.entity.Message;
import com.masterminds.entity.MessageStatus;
import com.masterminds.entity.User;
import com.masterminds.repository.MessageRepository;
import com.masterminds.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class MessageService {

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private UserRepository userRepository;

	/**
	 * Main entry point for WebSocket messages. Processes, Saves, and Routes the
	 * message.
	 */
	@Transactional
	public void processMessage(ChatMessage chatMessage) {
		// 1. Fetch Sender and Receiver entities to establish relationships
		User sender = userRepository.findById(chatMessage.getSenderId())
				.orElseThrow(() -> new RuntimeException("Sender not found"));

		User receiver = userRepository.findById(chatMessage.getRecipientId())
				.orElseThrow(() -> new RuntimeException("Receiver not found"));

		// 2. Map DTO to Entity and Save
		Message messageEntity = Message.builder()
				.id(chatMessage.getMessageId())
				.sender(sender)
				.receiver(receiver)
				.status(MessageStatus.SENT)
				.content(chatMessage.getContent()).createdDate(LocalDateTime.now()).build();

		messageRepository.save(messageEntity);

		// 3. Route the DTO to the specific recipient's WebSocket queue
		// Frontend subscribes to: /user/{userId}/queue/messages
		messagingTemplate.convertAndSendToUser(chatMessage.getRecipientId().toString(), "/queue/messages", chatMessage);
	}

	/**
	 * Retrieves the conversation history between two users. Optimized to return the
	 * list directly.
	 */
	public List<Message> getConversation(UUID user1, UUID user2) {
		return messageRepository.findChatHistory(user1, user2);
	}

	/**
	 * Optional: Helper to get all messages for a user (History feed)
	 */
	public List<Message> getMessageHistoryForUser(UUID userId) {
		// This would typically be used for the main chat list view
		return messageRepository.findBySenderIdOrReceiverIdOrderByCreatedDateAsc(userId, userId);
	}

	public List<Message> getRecentConversations(UUID userId) {
		return messageRepository.findRecentConversations(userId);
	}

	public void markConversationAsRead(ChatMessage payload) {
		UUID senderId = payload.getSenderId(); // The person who sent the msg
		UUID recipientId = payload.getRecipientId(); // Me (the reader)

		// 1. Update DB: Set all messages from sender to reader as READ
		messageRepository.markAsRead(recipientId, senderId);

		// Create the echo response
		ChatMessage readNotification = ChatMessage.builder().type(MessageType.READ_RECEIPT).senderId(senderId)
				.recipientId(recipientId).status("READ").build();

		// CRITICAL: Ensure this matches the recipient's subscription path pattern
		messagingTemplate.convertAndSendToUser(senderId.toString(), "/queue/messages", readNotification);
	}

	public void markConversationAsDelivered(ChatMessage payload) {
		UUID senderId = payload.getSenderId(); // The person who sent the msg
		UUID recipientId = payload.getRecipientId(); // Me (the reader)

		messageRepository.markAsDelivered(payload.getMessageId());

		ChatMessage deliveryNotification = ChatMessage.builder()
				.type(MessageType.DELIVERED_RECEIPT) // Add this to your
				.messageId(payload.getMessageId())																						// Enum
				.senderId(senderId)
				.recipientId(recipientId)
				.status("DELIVERED").build();

		messagingTemplate.convertAndSendToUser(senderId.toString(), "/queue/messages", deliveryNotification);
	}
	
	public void handleTyping(ChatMessage payload) {
		ChatMessage typingNotification = ChatMessage.builder()
	            .type(MessageType.TYPING)
	            .senderId(payload.getSenderId()) // Who is typing
	            .recipientId(payload.getRecipientId()) // Who should see it
	            .content(payload.getContent()) // We'll use this for "true" or "false"
	            .build();

	    messagingTemplate.convertAndSendToUser(
	        payload.getRecipientId().toString(), 
	        "/queue/messages", 
	        typingNotification
	    );
	}
}