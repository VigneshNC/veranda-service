package com.masterminds.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageResponseDTO {

	private UUID id;
	private UUID senderId;
	private String content;
	private LocalDateTime timestamp;
	private String status;

}
