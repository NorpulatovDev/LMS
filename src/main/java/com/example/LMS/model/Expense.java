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

    // NEW: Expense category to track different types of expenses
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory category = ExpenseCategory.UTILITY;

    // NEW: Reference to teacher if this is a salary payment
    private Long teacherId;

    private String teacherName; // Cache teacher name for quick display

    private String description; // Additional details about the expense

    public enum ExpenseCategory {
        UTILITY("Utility Bills"),
        SALARY("Teacher Salary"),
        RENT("Office Rent"),
        SUPPLIES("Office Supplies"),
        MARKETING("Marketing & Advertising"),
        OTHER("Other Expenses");

        private final String displayName;

        ExpenseCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}