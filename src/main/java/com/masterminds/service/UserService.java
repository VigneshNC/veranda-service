package com.masterminds.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.masterminds.dto.UserDTO;
import com.masterminds.entity.User;
import com.masterminds.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	public List<User> getAllContacts(UUID currentUserId) {
		// Return all users from the database so we can start a chat with them
		return userRepository.findAll().stream().filter(u -> !u.getId().equals(currentUserId))
				.collect(Collectors.toList());
	}

	public Optional<User> findByPhoneNumber(String phoneNumber) {
		return userRepository.findByPhoneNumber(phoneNumber);
	}

	public User saveContact(User contactRequest) {
		return userRepository.save(contactRequest);
	}

	public UserDTO getUser(UUID id) {
		User user = userRepository.findById(id).orElseThrow();

		UserDTO dto = new UserDTO();
		dto.setId(user.getId());
		dto.setDisplayName(user.getDisplayName());
		dto.setPhoneNumber(user.getPhoneNumber());
		dto.setStatus(user.getStatus());
		dto.setProfileImageUrl(user.getProfileImageUrl());
		dto.setOnline(user.isOnline());
		dto.setSecurityNotifications(user.isSecurityNotifications());

		return dto;
	}

	public void updateProfile(UUID id, UserDTO profileData) {
		// 1. Find the user or throw an error
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		// 2. Apply business logic (e.g., validation)
		if (profileData.getDisplayName() == null || profileData.getDisplayName().isEmpty()) {
			throw new IllegalArgumentException("Display name cannot be empty");
		}

		// 3. Update the fields
		user.setDisplayName(profileData.getDisplayName());
		user.setProfileImageUrl(profileData.getProfileImageUrl());
		user.setStatus(profileData.getStatus());

		// 4. Save back to Aiven DB
		userRepository.save(user);
	}

	@Transactional
	public void logout(UUID userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		user.setOnline(false);
		user.setLastSeen(LocalDateTime.now());
		userRepository.save(user);
	}

	public List<User> getBlockedUsers(UUID userId) {
		return userRepository.findBlockedUsersByUserId(userId);
	}

	@Transactional
	public void blockUser(UUID userId, UUID targetId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
		User target = userRepository.findById(targetId)
				.orElseThrow(() -> new RuntimeException("Target user not found"));

		user.getBlockedUsers().add(target);
		userRepository.save(user);
	}

	@Transactional
	public void unblockUser(UUID userId, UUID targetId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

		user.getBlockedUsers().removeIf(u -> u.getId().equals(targetId));
		userRepository.save(user);
	}

	public List<User> getBlockableContacts(UUID userId) {
		// This calls the complex @Query we defined in the Repository
		return userRepository.findBlockableContacts(userId);
	}

	@Transactional
	public void addContact(UUID userId, UUID contactId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
		User contact = userRepository.findById(contactId).orElseThrow(() -> new RuntimeException("Contact not found"));

		user.getContacts().add(contact);
		userRepository.save(user);
	}

	public List<User> getMyContacts(UUID userId) {
		return userRepository.findContactsByUserId(userId);
	}

	public List<User> searchGlobal(String query, UUID myId) {
		if (query.length() < 3)
			return new ArrayList<>(); // Don't search for tiny strings
		return userRepository.searchGlobalUsers(query, myId);
	}
	
	@Transactional
	public void changePhoneNumber(UUID userId, String newPhoneNumber) {
	    User user = userRepository.findById(userId)
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    // 1. Check if the new number is already taken
	    if (userRepository.findByPhoneNumber(newPhoneNumber) != null) {
	        throw new RuntimeException("New phone number is already registered with another account");
	    }

	    // 2. Update number
	    user.setPhoneNumber(newPhoneNumber);
	    userRepository.save(user);

	    // Note: Since all relationships use the UUID 'id', they remain intact!
	}
	
	public void changePhoneNumber(UUID userId, String oldNumber, String newNumber) {
	    User user = userRepository.findById(userId)
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    // Verify the "Old" number actually belongs to this user session
	    if (!user.getPhoneNumber().equals(oldNumber)) {
	        throw new RuntimeException("The provided old number does not match our records.");
	    }

	    // Check if new number is already in use
	    if (userRepository.findByPhoneNumber(newNumber).isPresent()) {
	        throw new RuntimeException("The new phone number is already registered.");
	    }

	    user.setPhoneNumber(newNumber);
	    userRepository.save(user);
	}
	
	@Transactional
	public void deleteUserAccount(UUID userId) {
	    User user = userRepository.findById(userId)
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    // 1. Remove this user from other people's contact lists
	    // This cleans up the 'user_contacts' join table entries where this user was the 'contact'
	    userRepository.removeFromAllContactLists(userId);

	    // 2. Remove from groups (If you have a Group entity)
	    // groupRepository.removeUserFromAllGroups(userId);

	    // 3. Delete the user (JPA will handle user_contacts/user_blocks 
	    // where THIS user was the owner because of the @ManyToMany mappings)
	    userRepository.delete(user);
	}
	
	@Transactional
	public void updateSecurityNotifications(UUID userId, boolean enabled) {
	    User user = userRepository.findById(userId)
	            .orElseThrow(() -> new RuntimeException("User not found"));
	    user.setSecurityNotifications(enabled);
	    userRepository.save(user);
	}

}
