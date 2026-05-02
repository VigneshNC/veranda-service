package com.masterminds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for the Change Number process. Used to securely transport the old and new
 * phone numbers from the React frontend to the Spring Boot backend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeNumberRequest {

	private String oldNumber;
	private String newNumber;

}
