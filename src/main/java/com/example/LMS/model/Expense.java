package com.example.LMS.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Entity
@Table(name = "expenses")
@Data
public class Expense {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Expense name is required!")
    private String name;

    @NotNull(message = "Amount is required!")
    @Positive(message = "Amount must be positive!")
    private Double amount;

    private String expenseDate; // Simple string date like "2025-08-27"
    
    private String expenseMonth; // Format: "2025-08" for filtering
}