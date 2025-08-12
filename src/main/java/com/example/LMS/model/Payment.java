package com.example.LMS.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Entity
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull(message = "Student ID is required!")
    private Long studentId;
    @NotNull(message = "Amount is required!")
    @Positive(message = "Amount must be positive!")
    private Double amount;
    @NotBlank(message = "Payment date is required!")
    private String paymentDate;
}
