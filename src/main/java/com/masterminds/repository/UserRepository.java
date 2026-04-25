package com.masterminds.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.masterminds.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

	Optional<User> findByPhoneNumber(String phoneNumber);

	// Fetch all contacts for a user
	@Query("SELECT u.contacts FROM User u WHERE u.id = :userId")
	List<User> findContactsByUserId(@Param("userId") UUID userId);

	@Query("SELECT u.blockedUsers FROM User u WHERE u.id = :userId")
	List<User> findBlockedUsersByUserId(@Param("userId") UUID userId);

	@Query("SELECT u FROM User u WHERE u.id IN "
			+ "(SELECT c.id FROM User owner JOIN owner.contacts c WHERE owner.id = :userId) " + "AND u.id NOT IN "
			+ "(SELECT b.id FROM User owner JOIN owner.blockedUsers b WHERE owner.id = :userId)")
	List<User> findBlockableContacts(@Param("userId") UUID userId);

	@Query("SELECT u FROM User u WHERE "
			+ "(LOWER(u.displayName) LIKE LOWER(concat('%', :query, '%')) OR u.phoneNumber LIKE concat('%', :query, '%')) "
			+ "AND u.id != :myId "
			+ "AND u.id NOT IN (SELECT c.id FROM User owner JOIN owner.contacts c WHERE owner.id = :myId)")
	List<User> searchGlobalUsers(@Param("query") String query, @Param("myId") UUID myId);

}
