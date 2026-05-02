package com.masterminds.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountReportDTO {

	private String generatedDate;
	private UserProfileInfo profile;
	private List<String> contacts;
	private List<String> blockedUsers;
	private List<String> groups;
	private PrivacySummary privacy;

	@Data
	@Builder
	public static class UserProfileInfo {
		private String displayName;
		private String phoneNumber;
		private String status;
		private String profileImageUrl;
		private LocalDateTime createdDate;
	}

	@Data
	@Builder
	public static class PrivacySummary {
		private String readReceipts;
		private String lastSeen;
	}

}
