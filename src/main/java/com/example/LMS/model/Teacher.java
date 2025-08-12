package com.example.LMS.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Entity
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Teacher name is required!")
    private String name;
    private String email;

    @NotBlank(message = "Teacher phone number is required!")
    private String phone;

    @NotNull(message = "Salary is required!")
    @Positive(message = "Salary must be positive!")
    private Double salary;

    @NotNull(message = "User role is required!")
    @Enumerated(EnumType.STRING)
    private UserRole userRole;
}
