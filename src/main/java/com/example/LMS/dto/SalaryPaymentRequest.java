package com.example.LMS.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SalaryPaymentRequest {
    
    @NotNull(message = "Teacher ID is required")
    private Long teacherId;
    
    private String month; // Format: YYYY-MM, optional - will use current month if not provided
    
    @Positive(message = "Amount must be positive if provided")
    private Double amount; // Optional - will use teacher's salary if not provided
    
    private String paymentDate; // Optional - will use current date if not provided
    
    private String description; // Optional description/notes for the salary payment
    
    // Optional - for partial payments or bonuses
    private String paymentType; // "FULL_SALARY", "PARTIAL_SALARY", "BONUS", "ADVANCE"
}