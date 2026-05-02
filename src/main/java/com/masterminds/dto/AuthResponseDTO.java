package com.masterminds.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDTO {

	private String token;
	private UUID userId;
	
	@JsonProperty("isNewUser")
	private boolean isNewUser; // Crucial for your onboarding flow
	private String phoneNumber;
	
}
