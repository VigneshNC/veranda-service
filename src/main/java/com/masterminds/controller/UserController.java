package com.masterminds.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.masterminds.dto.UserDTO;
import com.masterminds.entity.User;
import com.masterminds.service.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

	@Autowired
	private UserService userService;

	@GetMapping("/contacts")
	public ResponseEntity<List<User>> getAllContacts(@RequestParam UUID currentUserId) {
		List<User> users = userService.getAllContacts(currentUserId);
		return ResponseEntity.ok(users);
	}

	@PostMapping("/add-contact")
	public ResponseEntity<?> addContact(@RequestBody User contactRequest) {
		// Check if the user with this phone number already exists
		Optional<User> existingUser = userService.findByPhoneNumber(contactRequest.getPhoneNumber());

		if (existingUser.isPresent()) {
			return ResponseEntity.ok(existingUser.get());
		}
		// In a real app, you might send an invite SMS here via Fast2SMS
		User newUser = userService.saveContact(contactRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserDTO> getUser(@PathVariable UUID id) {
		UserDTO dto = userService.getUser(id);

		return ResponseEntity.ok(dto);
	}

	@PutMapping("/{id}/update-profile")
	public ResponseEntity<String> updateProfile(@PathVariable UUID id, @RequestBody UserDTO profileData) {
		// Just call the service and return the statement
		userService.updateProfile(id, profileData);
		return ResponseEntity.ok("Profile updated successfully");
	}

	@PostMapping("/logout/{id}")
	public ResponseEntity<String> logout(@PathVariable UUID id) {
		userService.logout(id);
		return ResponseEntity.ok("User status updated to offline");
	}

	@PostMapping("/block/{contactId}")
	public ResponseEntity<?> blockUser(@PathVariable UUID contactId, @RequestHeader("Authorization") String token) {
		// Logic to add contactId to the logged-in user's 'blocked_users' list
		// userService.blockUser(currentUserId, contactId);
		return ResponseEntity.ok("User blocked successfully");
	}

	// Get list of blocked users
	@GetMapping("/{userId}/blocked")
	public ResponseEntity<List<User>> getBlocked(@PathVariable UUID userId) {
		return ResponseEntity.ok(userService.getBlockedUsers(userId));
	}

	// Block a user
	@PostMapping("/{userId}/block/{targetId}")
	public ResponseEntity<String> block(@PathVariable UUID userId, @PathVariable UUID targetId) {
		userService.blockUser(userId, targetId);
		return ResponseEntity.ok("User blocked successfully");
	}

	// Unblock a user
	@DeleteMapping("/{userId}/block/{targetId}")
	public ResponseEntity<String> unblock(@PathVariable UUID userId, @PathVariable UUID targetId) {
		userService.unblockUser(userId, targetId);
		return ResponseEntity.ok("User unblocked successfully");
	}

	@GetMapping("/{userId}/blockable-contacts")
	public ResponseEntity<List<User>> getBlockable(@PathVariable UUID userId) {
		return ResponseEntity.ok(userService.getBlockableContacts(userId));
	}

	@GetMapping("/search")
	public ResponseEntity<List<User>> searchGlobal(@RequestParam String query, @RequestParam UUID myId) {
		// Calling the service method we discussed
		List<User> results = userService.searchGlobal(query, myId);
		return ResponseEntity.ok(results);
	}

	@PostMapping("/{userId}/contacts/{contactId}")
	public ResponseEntity<String> addContact(@PathVariable UUID userId, @PathVariable UUID contactId) {
		userService.addContact(userId, contactId);
		return ResponseEntity.ok("Contact added successfully");
	}

}
