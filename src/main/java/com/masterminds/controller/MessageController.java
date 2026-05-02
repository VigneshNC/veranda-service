package com.masterminds.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.masterminds.dto.ChatMessage;
import com.masterminds.entity.Message;
import com.masterminds.service.MessageService;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

	@Autowired
	private MessageService messageService;

	@MessageMapping("/chat") // Clients send to /app/chat
	public void processMessage(@Payload ChatMessage chatMessage) {
		System.out.println(chatMessage);
		messageService.processMessage(chatMessage);
	}

	@GetMapping("/inbox/{userId}")
	public ResponseEntity<List<Message>> getInbox(@PathVariable UUID userId) {
		return ResponseEntity.ok(messageService.getRecentConversations(userId));
	}

	@GetMapping("/history/{user1}/{user2}")
	public ResponseEntity<List<Message>> getChatHistory(@PathVariable UUID user1, @PathVariable UUID user2) {
		// This calls the Repository method we wrote earlier
		return ResponseEntity.ok(messageService.getConversation(user1, user2));
	}

	@MessageMapping("/chat.read")
	public void processReadReceipt(@Payload ChatMessage payload) {
		messageService.markConversationAsRead(payload);
	}

	@MessageMapping("/chat.delivered")
	public void processDeliveryReceipt(@Payload ChatMessage payload) {
		messageService.markConversationAsDelivered(payload);
	}

	@MessageMapping("/chat.typing")
	public void handleTyping(@Payload ChatMessage payload) {
		messageService.handleTyping(payload);
	}

}
