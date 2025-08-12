package com.example.LMS.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Student name is required!")
    private String name;

    private String email;

    @NotBlank(message = "Student phone number is required!")
    private String phone;

    @NotBlank(message = "Enrollment date is required!")
    private String enrollmentDate;

    @ManyToMany
    @JoinTable(
            name = "student_course",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses;

    @NotNull(message = "User role is required!")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole;
}
