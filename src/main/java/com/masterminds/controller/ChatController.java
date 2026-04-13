package com.masterminds.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import com.masterminds.dto.MessageRequest;
import com.masterminds.service.MessageService;

@RestController
@CrossOrigin(origins = "*")
public class ChatController {

	@Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageService messageService;

    @MessageMapping("/chat")
    public void processMessage(MessageRequest request) {
        // 1. Save to Aiven PostgreSQL
        var savedMsg = messageService.sendMessage(
            request.getSenderId(), 
            request.getReceiverId(), 
            request.getContent()
        );

        // 2. Push to the Receiver instantly
        // The receiver must be subscribed to: /user/{receiverId}/queue/messages
        messagingTemplate.convertAndSendToUser(
            request.getReceiverId().toString(), 
            "/queue/messages", 
            savedMsg
        );
    }
	
}
