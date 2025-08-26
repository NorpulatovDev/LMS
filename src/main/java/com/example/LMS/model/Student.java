package com.example.LMS.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Student name is required!")
    @Column(nullable = false)
    private String name;

    private String email;

    @NotBlank(message = "Student phone number is required!")
    @Column(nullable = false)
    private String phone;

    @NotBlank(message = "Enrollment date is required!")
    @Column(nullable = false)
    private String enrollmentDate;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "student_course",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    @JsonIgnoreProperties("students")
    private List<Course> courses = new ArrayList<>();

    // Constructors
    public Student() {}

    public Student(String name, String email, String phone, String enrollmentDate) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.enrollmentDate = enrollmentDate;
        this.courses = new ArrayList<>();
    }
}