package com.masterminds.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.masterminds.dto.UserDTO;
import com.masterminds.entity.User;
import com.masterminds.repository.UserRepository;

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

		return dto;
	}

}
