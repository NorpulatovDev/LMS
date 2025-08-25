package com.example.LMS.dto;

import lombok.Data;

import java.util.List;

@Data
public class StudentRequest {
    private String name;
    private String email;
    private String phone;
    private String enrollmentDate;
    private List<Long> courseIds;  // only course IDs, not full objects
}