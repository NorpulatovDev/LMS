package com.example.LMS.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "teachers")
@Schema(description = "Teacher entity representing a teacher in the Learning Management System")
public class Teacher {

    @Id
    @Schema(description = "Unique identifier of the teacher", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Teacher name is required!")
    @Schema(description = "Full name of the teacher", example = "Dr. John Smith", required = true)
    private String name;

    @Schema(description = "Email address of the teacher", example = "john.smith@university.edu")
    private String email;

    @NotBlank(message = "Teacher phone number is required!")
    @Schema(description = "Phone number of the teacher", example = "+1-555-123-4567", required = true)
    private String phone;

    @NotNull(message = "Salary is required!")
    @Positive(message = "Salary must be positive!")
    @Schema(description = "Monthly salary of the teacher", example = "5000.00", required = true)
    private Double salary;

    // User entity relationship - One-to-One
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    @JsonIgnore  // Hide user details in teacher responses for security
    @Schema(description = "Associated user account (hidden for security)")
    private User user;

    // Course entity relationship - Many-to-Many
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "teacher_courses",
            joinColumns = @JoinColumn(name = "teacher_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    @JsonManagedReference("teacher-courses")  // KEEP as JsonManagedReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Schema(description = "List of courses taught by this teacher (hidden in JSON responses)")
    private Set<Course> courses = new HashSet<>();
}