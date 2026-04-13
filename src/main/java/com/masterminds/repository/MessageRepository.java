package com.masterminds.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.masterminds.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

	// Fetch the chat history between two specific users
	@Query("SELECT m FROM Message m WHERE " + "(m.sender.id = :u1 AND m.receiver.id = :u2) OR "
			+ "(m.sender.id = :u2 AND m.receiver.id = :u1) " + "ORDER BY m.createdDate ASC")
	List<Message> findChatHistory(@Param("u1") UUID user1, @Param("u2") UUID user2);

}
