package com.masterminds.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.masterminds.dto.MessageResponseDTO;
import com.masterminds.entity.Message;
import com.masterminds.service.MessageService;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {
	
	@Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public ResponseEntity<MessageResponseDTO> send(@RequestBody Map<String, String> request) {
        UUID senderId = UUID.fromString(request.get("senderId"));
        UUID receiverId = UUID.fromString(request.get("receiverId"));
        String content = request.get("content");

        Message msg = messageService.sendMessage(senderId, receiverId, content);

        return ResponseEntity.ok(new MessageResponseDTO(
                msg.getId(),
                msg.getSender().getId(),
                msg.getContent(),
                msg.getCreatedDate(),
                msg.getStatus().toString()
        ));
    }

}
