package com.masterminds.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.masterminds.dto.AuthResponseDTO;
import com.masterminds.entity.User;
import com.masterminds.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class AuthService {

	@Autowired
    private UserRepository userRepository;
	
	@Autowired
    private JwtService jwtService;

    // --- STEP 1: Generate & Save OTP ---
    @Transactional
    public String requestOtp(String phoneNumber) {
        // 1. Generate a 6-digit random number
//        String otp = String.format("%06d", new Random().nextInt(999999));
    	String otp = "123456";
        
        // 2. Find existing user or create a new one (Registration + Login)
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElse(User.builder()
                        .phoneNumber(phoneNumber)
                        .isOnline(false)
                        .build());

        // 3. Set OTP and Expiry (5 minutes from now)
        user.setCurrentOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        userRepository.save(user);

        // 4. Return OTP so the Controller can send it via SMS/Console
        return otp;
    }

    public Map<String, Object> verifyOtpAndLogin(String phoneNumber, String userTypedOtp) {
        // 1. Business Logic: Hardcoded check (or DB check later)
        if (!"123456".equals(userTypedOtp)) {
            throw new RuntimeException("Invalid OTP provided");
        }

        // 2. Business Logic: Fetch user
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Business Logic: Generate the digital passport (JWT)
        String token = jwtService.generateToken(user.getPhoneNumber(), user.getId());

        // 4. Prepare data for the controller
        return Map.of(
            "token", token,
            "userId", user.getId(),
            "displayName", user.getDisplayName() != null ? user.getDisplayName() : ""
        );
    }
    
    @Transactional
    public AuthResponseDTO verifyOtp(String phoneNumber, String otp) {
        // 1. Fetch the user
        User user = userRepository.findByPhoneNumber(phoneNumber)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // 2. Business Logic: Check OTP and Expiry
        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP has expired");
        }

        if (!user.getCurrentOtp().equals(otp)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid OTP code");
        }

        // 3. Mark user as verified and online
        user.setVerified(true);
        user.setOnline(true);
        user.setCurrentOtp(null); // Clear OTP so it can't be reused
        userRepository.save(user);

        // 4. Generate the JWT
        String token = jwtService.generateToken(user.getPhoneNumber(), user.getId());

        // 5. Return the response data
        return AuthResponseDTO.builder()
            .token(token)
            .userId(user.getId())
            .phoneNumber(user.getPhoneNumber())
            .isNewUser(user.getDisplayName() == null) // Helps Frontend decide to show ProfileSetup
            .build();
    }
	
}
