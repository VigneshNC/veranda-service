package com.masterminds.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {

	@Id
    @GeneratedValue(generator = "UUID")
	@UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
	
    @Column(unique = true, nullable = false, length = 15)
    private String phoneNumber;

    @Column(length = 100)
    private String displayName;

    private String profileImageUrl;

    // OTP Logic
    private String currentOtp;
    
//  @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "Asia/India", pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime otpExpiry;

    private boolean isOnline = false;
    
//    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "Asia/India", pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime lastSeen;

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
