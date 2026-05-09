package com.masterminds.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.masterminds.dto.ChatMessage;
import com.masterminds.dto.ChatMessage.MessageType;
import com.masterminds.entity.Message;
import com.masterminds.entity.MessageStatus;
import com.masterminds.entity.User;
import com.masterminds.repository.MessageRepository;
import com.masterminds.repository.UserRepository;

import jakarta.transaction.Transactional;
import tools.jackson.databind.ObjectMapper;

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
		Message messageEntity = Message.builder().id(chatMessage.getMessageId()).sender(sender).receiver(receiver)
				.status(MessageStatus.SENT).content(chatMessage.getContent())
				.attachmentType(chatMessage.getAttachmentType()).attachmentUrl(chatMessage.getAttachmentUrl())
				.createdDate(LocalDateTime.now()).build();

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

		ChatMessage deliveryNotification = ChatMessage.builder().type(MessageType.DELIVERED_RECEIPT) // Add this to your
				.messageId(payload.getMessageId()) // Enum
				.senderId(senderId).recipientId(recipientId).status("DELIVERED").build();

		messagingTemplate.convertAndSendToUser(senderId.toString(), "/queue/messages", deliveryNotification);
	}

	public void handleTyping(ChatMessage payload) {
		ChatMessage typingNotification = ChatMessage.builder().type(MessageType.TYPING).senderId(payload.getSenderId()) // Who
																														// is
																														// typing
				.recipientId(payload.getRecipientId()) // Who should see it
				.content(payload.getContent()) // We'll use this for "true" or "false"
				.build();

		messagingTemplate.convertAndSendToUser(payload.getRecipientId().toString(), "/queue/messages",
				typingNotification);
	}

	public ResponseEntity<?> uploadFile(MultipartFile multipartFile) throws IOException {

		Map<String, Object> finalPayload = new HashMap<>();
		finalPayload.put("fileType", multipartFile.getContentType());
		finalPayload.put("fileName", multipartFile.getOriginalFilename());
		finalPayload.put("fileSize", multipartFile.getSize());
		finalPayload.put("acl", "public-read");

		// 3. Forward the complete request to UploadThing
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders utHeaders = new HttpHeaders();
		utHeaders.set("x-uploadthing-api-key",
				"sk_live_c8634138777b1d79aa7048f25c876b6ca0cdff2e1d731ec45fd0bc1857974a5f");
		utHeaders.setContentType(MediaType.APPLICATION_JSON);
		utHeaders.set("x-uploadthing-version", "7.7.4");
		utHeaders.set("x-uploadthing-fe-package", "custom-sdk");

		System.out.println("finalPayload: " + finalPayload);

		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(finalPayload, utHeaders);

		try {
			ResponseEntity<String> utResponse = restTemplate
					.postForEntity("https://api.uploadthing.com/v7/prepareUpload", entity, String.class);

			Map<String, String> utResponseStr = new ObjectMapper().readValue(utResponse.getBody(), Map.class);

			return ResponseEntity.ok(utResponseStr);
		} catch (HttpClientErrorException e) {
			e.printStackTrace();
			// Log the actual error body from UploadThing for easier debugging
			System.out.println("UploadThing Error: " + e.getResponseBodyAsString());
			return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
		}
	}
}