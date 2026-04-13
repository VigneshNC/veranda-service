package com.masterminds.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

	@Id
	@UuidGenerator
	@Column(updatable = false, nullable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sender_id", nullable = false)
	private User sender;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "receiver_id", nullable = false)
	private User receiver;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String content;

	@Enumerated(EnumType.STRING)
	private MessageStatus status = MessageStatus.SENT;

	@Column(name = "CREATED_BY", length = 20)
	private String createdBy;

	@CreatedDate
	@Column(name = "CREATED_DATE", nullable = false, updatable = false)
//	@JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "Asia/India", pattern = "dd/MM/yyyy HH:mm:ss")
	private LocalDateTime createdDate;

	@Column(name = "MODIFIED_BY", length = 20)
	private String modifiedBy;

	@LastModifiedDate
	@Column(name = "MODIFIED_DATE", insertable = false)
//	@JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "Asia/India", pattern = "dd/MM/yyyy HH:mm:ss")
	private LocalDateTime modifiedDate;

}
