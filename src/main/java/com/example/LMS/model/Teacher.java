package com.example.LMS.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "Salary is required!")
    private double salary;

    @NotBlank(message = "User role is required!")
    @Enumerated(EnumType.STRING)
    private UserRole userRole;
}
