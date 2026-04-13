package com.masterminds.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    // --- STEP 2: Verify OTP ---
    public boolean verifyOtp(String phoneNumber, String userTypedOtp) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .map(user -> {
                    boolean isNotExpired = user.getOtpExpiry().isAfter(LocalDateTime.now());
                    boolean matches = user.getCurrentOtp().equals(userTypedOtp);
                    
                    if (isNotExpired && matches) {
                        // Clear OTP after successful login for security
                        user.setCurrentOtp(null);
                        user.setOtpExpiry(null);
                        userRepository.save(user);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
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
	
}
