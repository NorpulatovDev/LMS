package com.example.LMS.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Table(name = "courses")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Schema(description = "Course entity representing a course in the Learning Management System")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "course_teacher",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "teacher_id")
    )
    @JsonIgnoreProperties({"courses", "user"})
    private Set<Teacher> teachers;

    @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"courses"})
    private List<Student> students;

    // Constructors
    public Course() {
        this.teachers = new HashSet<>();
        this.students = new ArrayList<>();
    }

    public Course(String name, String description, Double fee) {
        this();
        this.name = name;
        this.description = description;
        this.fee = fee;
    }

    // Helper methods
    public void addStudent(Student student) {
        if (this.students == null) {
            this.students = new ArrayList<>();
        }
        if (!this.students.contains(student)) {
            this.students.add(student);
            if (student.getCourses() != null && !student.getCourses().contains(this)) {
                student.getCourses().add(this);
            }
        }
    }

    public void removeStudent(Student student) {
        if (this.students != null) {
            this.students.remove(student);
            if (student.getCourses() != null) {
                student.getCourses().remove(this);
            }
        }
    }

    public List<Student> getStudents() {
        if (this.students == null) {
            this.students = new ArrayList<>();
        }
        return this.students;
    }

    public Set<Teacher> getTeachers() {
        if (this.teachers == null) {
            this.teachers = new HashSet<>();
        }
        return this.teachers;
    }
}