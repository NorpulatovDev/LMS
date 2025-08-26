package com.example.LMS.dto;

import lombok.Data;
import lombok.ToString;

import java.util.Set;

@Data
@ToString
public class StudentRequest {
    private String name;
    private String email;
    private String phone;
    private String enrollmentDate;
    private Set<Long> courseIds;  // Using Set instead of List for better uniqueness
}