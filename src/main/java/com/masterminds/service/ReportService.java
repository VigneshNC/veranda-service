package com.masterminds.service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.masterminds.dto.AccountReportDTO;
import com.masterminds.entity.User;
import com.masterminds.repository.UserRepository;

import tools.jackson.databind.ObjectMapper;

@Service
public class ReportService {

	@Autowired
	private UserRepository userRepository;
	
    private ObjectMapper objectMapper = new ObjectMapper();

    public String generateAccountReportJson(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AccountReportDTO report = AccountReportDTO.builder()
                .generatedDate(LocalDateTime.now().toString())
                .profile(AccountReportDTO.UserProfileInfo.builder()
                        .displayName(user.getDisplayName())
                        .phoneNumber(user.getPhoneNumber())
                        .status(user.getStatus())
                        .createdDate(user.getCreatedDate())
                        .build())
                .contacts(user.getContacts().stream()
                        .map(u -> u.getDisplayName() + " (" + u.getPhoneNumber() + ")")
                        .collect(Collectors.toList()))
                .blockedUsers(user.getBlockedUsers().stream()
                        .map(User::getPhoneNumber)
                        .collect(Collectors.toList()))
                .privacy(AccountReportDTO.PrivacySummary.builder()
                        .readReceipts("Enabled") 
                        .lastSeen(user.getLastSeen() != null ? user.getLastSeen().toString() : "Not available")
                        .build())
                .build();

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(report);
    }
	
}
