package com.example.LMS.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "courses")
@Schema(description = "Course entity representing a course in the Learning Management System")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the course", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Course name is required!")
    @Size(min = 3, max = 100, message = "Course name must be between 3 and 100 characters!")
    @Schema(description = "Name of the course", example = "Introduction to Java Programming", required = true)
    private String name;

    @Schema(description = "Detailed description of the course",
            example = "This course covers the fundamentals of Java programming including OOP concepts, data structures, and basic algorithms.")
    private String description;

    @NotNull(message = "Course fee is required!")
    @Positive(message = "Course fee must be positive!")
    @Schema(description = "Course fee in USD", example = "299.99", required = true)
    private Double fee;

    // Teacher entity relationship - Many-to-Many
    // Use @JsonIgnore to prevent infinite recursion during serialization
    @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
    @JsonIgnore  // This prevents the circular reference issue
    @ToString.Exclude  // Exclude from toString to avoid potential issues
    @EqualsAndHashCode.Exclude  // Exclude from equals/hashCode to avoid potential issues
    @Schema(description = "List of teachers assigned to this course (hidden in JSON responses)")
    private Set<Teacher> teachers = new HashSet<>();

    // If you need to expose teachers in some responses, create a separate DTO or use a custom serializer
    // For now, we'll keep it simple and hide the teachers relationship
}