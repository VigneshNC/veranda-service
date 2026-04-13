package com.masterminds.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

}
