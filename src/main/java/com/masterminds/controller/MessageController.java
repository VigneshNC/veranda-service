package com.masterminds.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.masterminds.dto.ChatMessage;
import com.masterminds.service.MessageService;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

	@Autowired
	private MessageService messageService;

	@MessageMapping("/chat") // Clients send to /app/chat
	public void processMessage(@Payload ChatMessage chatMessage) {
		messageService.processMessage(chatMessage);
	}

}
