package com.masterminds.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class UserDTO {
	private UUID id;
	private String displayName;
	private String phoneNumber;
	private String status;
	private String profileImageUrl;
	private boolean isOnline;
}
