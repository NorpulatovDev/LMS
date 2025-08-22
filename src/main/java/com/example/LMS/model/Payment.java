package com.example.LMS.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

// src/main/java/com/example/LMS/model/Payment.java
@Entity
@Data
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Student ID is required!")
    private Long studentId;

    @NotNull(message = "Course ID is required!") // ✅ ADD THIS
    private Long courseId;

    @NotNull(message = "Amount is required!")
    @Positive(message = "Amount must be positive!")
    private Double amount;

    @NotBlank(message = "Payment date is required!")
    private String paymentDate;

    // ✅ ADD THESE OPTIONAL FIELDS
    private String paymentMonth; // Format: "2025-08" for easy filtering
    private String studentName;  // Cache student name for quick display
    private String courseName;   // Cache course name for quick display
}