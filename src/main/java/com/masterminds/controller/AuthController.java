package com.masterminds.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.masterminds.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

	@Autowired
	private AuthService authService;

	@PostMapping("/request-otp")
	public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
		String phone = request.get("phoneNumber"); // Clean format: 9876543210
		String otp = authService.requestOtp(phone);

		// FOR TESTING: We print to console.
		// LATER: Replace this with your Fast2SMS/Twilio call.
		System.out.println(">>> SENDING SMS TO " + phone + " : CODE IS " + otp);

		return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
	}

	@PostMapping("/verify-otp")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> request) {
        try {
            String phone = request.get("phoneNumber");
            String otp = request.get("otp");

            // Controller just calls the service
            Map<String, Object> loginData = authService.verifyOtpAndLogin(phone, otp);

            return ResponseEntity.ok(loginData);
        } catch (RuntimeException e) {
            // Controller handles the error response
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("error", e.getMessage()));
        }

//		boolean isValid = authService.verifyOtp(phone, otp);
//
//		if (isValid) {
//			return ResponseEntity.ok(Map.of("message", "Login successful", "status", "success"));
//		}
//		return ResponseEntity.status(401).body(Map.of("message", "Invalid or expired OTP"));
	}

}
