package com.masterminds.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.masterminds.dto.ChatMessage;
import com.masterminds.entity.Message;
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
	 * Sends a message from one user to another.
	 */
	@Transactional
	public Message sendMessage(UUID senderId, UUID receiverId, String content) {
		User sender = userRepository.findById(senderId).orElseThrow(() -> new RuntimeException("Sender not found"));

		User receiver = userRepository.findById(receiverId)
				.orElseThrow(() -> new RuntimeException("Receiver not found"));

		Message message = Message.builder().sender(sender).receiver(receiver).content(content).build();

		return messageRepository.save(message);
	}

	public void processMessage(ChatMessage chatMessage) {
		// 1. Convert DTO -> Entity
		Message messageEntity = new Message();
		messageEntity.setContent(chatMessage.getContent());
		messageEntity.setCreatedDate(LocalDateTime.now());

		// 2. Save Entity
		messageRepository.save(messageEntity);

		// 3. Send DTO back to the recipient
		messagingTemplate.convertAndSendToUser(chatMessage.getRecipientId().toString(), "/queue/messages", chatMessage);
	}

	/**
	 * Retrieves the conversation history between two users.
	 */
	public List<Message> getConversation(UUID user1, UUID user2) {
		return messageRepository.findChatHistory(user1, user2);
	}

}
