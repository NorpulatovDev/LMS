package com.example.LMS.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TeacherCreationDto {

    // User details for authentication
    @NotBlank(message = "Username is required!")
    private String username;

    @NotBlank(message = "Password is required!")
    private String password;

    // Teacher profile details
    @NotBlank(message = "Teacher name is required!")
    private String name;

    private String email;

    @NotBlank(message = "Teacher phone number is required!")
    private String phone;

    @NotNull(message = "Salary is required!")
    @Positive(message = "Salary must be positive!")
    private Double salary;
}