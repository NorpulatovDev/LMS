package com.example.LMS.dto;

import lombok.Data;

import java.util.Set;

@Data
public class StudentRequest {
    private String name;
    private String email;
    private String phone;
    private String enrollmentDate;
    private Long courseId;  // only course IDs, not full objects
}