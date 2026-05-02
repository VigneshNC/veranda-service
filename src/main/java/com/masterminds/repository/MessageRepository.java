package com.masterminds.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.masterminds.entity.Message;

import jakarta.transaction.Transactional;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

	// 1. Fetch the chat history between two specific users (Classic Chat View)
	@Query("SELECT m FROM Message m WHERE " + "(m.sender.id = :u1 AND m.receiver.id = :u2) OR "
			+ "(m.sender.id = :u2 AND m.receiver.id = :u1) " + "ORDER BY m.createdDate ASC")
	List<Message> findChatHistory(@Param("u1") UUID user1, @Param("u2") UUID user2);

	// 2. Fetch shared media/attachments (Gallery View)
	@Query("SELECT m.attachmentUrl FROM Message m WHERE " + "((m.sender.id = :u1 AND m.receiver.id = :u2) OR "
			+ "(m.sender.id = :u2 AND m.receiver.id = :u1)) " + "AND m.attachmentUrl IS NOT NULL "
			+ "ORDER BY m.createdDate DESC")
	List<String> findSharedMedia(@Param("u1") UUID user1, @Param("u2") UUID user2);

	// 3. Find all messages involving a user (Used for the global history feed)
	List<Message> findBySenderIdOrReceiverIdOrderByCreatedDateAsc(UUID senderId, UUID receiverId);

	// 4. Find the LATEST message between two users (Used for the Chat List/Inbox
	// preview)
	@Query(value = "SELECT * FROM messages WHERE " + "(sender_id = :u1 AND receiver_id = :u2) OR "
			+ "(sender_id = :u2 AND receiver_id = :u1) " + "ORDER BY created_date DESC LIMIT 1", nativeQuery = true)
	Message findLastMessage(@Param("u1") UUID u1, @Param("u2") UUID u2);

//	@Query(value = "SELECT * FROM messages m WHERE m.id IN (" + "  SELECT MAX(id) FROM messages "
//			+ "  WHERE sender_id = :userId OR receiver_id = :userId "
//			+ "  GROUP BY LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id)"
//			+ ") ORDER BY created_date DESC", nativeQuery = true)
//	List<Message> findRecentConversations(@Param("userId") UUID userId);

	@Query(value = "SELECT m.* FROM messages m " + "INNER JOIN ("
			+ "    SELECT LEAST(sender_id, receiver_id) as user_a, "
			+ "           GREATEST(sender_id, receiver_id) as user_b, "
			+ "           MAX(created_date) as last_msg_time " + "    FROM messages "
			+ "    WHERE sender_id = :userId OR receiver_id = :userId " + "    GROUP BY user_a, user_b" + ") sub ON ("
			+ "    (LEAST(m.sender_id, m.receiver_id) = sub.user_a AND "
			+ "     GREATEST(m.sender_id, m.receiver_id) = sub.user_b) " + "    AND m.created_date = sub.last_msg_time"
			+ ") ORDER BY m.created_date DESC", nativeQuery = true)
	List<Message> findRecentConversations(@Param("userId") UUID userId);

	// For Read: Update all messages in a conversation
	@Modifying
	@Transactional
	@Query("UPDATE Message m SET m.status = 'READ' WHERE m.sender.id = :senderId AND m.receiver.id = :readerId AND m.status != 'READ'")
	void markAsRead(@Param("readerId") UUID readerId, @Param("senderId") UUID senderId);
	
	// For Delivery: Update a specific message by ID
	@Modifying
	@Transactional
	@Query("UPDATE Message m SET m.status = 'DELIVERED' WHERE m.id = :messageId AND m.status = 'SENT'")
	void markAsDelivered(@Param("messageId") UUID messageId);

}