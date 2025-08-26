package com.example.LMS.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "students")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Student name is required!")
    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String email;

    @NotBlank(message = "Student phone number is required!")
    @Column(nullable = false)
    private String phone;

    @NotBlank(message = "Enrollment date is required!")
    @Column(nullable = false)
    private String enrollmentDate;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "student_course",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id"})
    )
    @JsonIgnoreProperties({"students", "teachers"})
    private List<Course> courses;

    // Constructors
    public Student() {
        this.courses = new ArrayList<>();
    }

    public Student(String name, String email, String phone, String enrollmentDate) {
        this();
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.enrollmentDate = enrollmentDate;
    }

    // Helper methods for managing courses
    public void addCourse(Course course) {
        if (this.courses == null) {
            this.courses = new ArrayList<>();
        }
        if (!this.courses.contains(course)) {
            this.courses.add(course);
            // Maintain bidirectional relationship
            if (course.getStudents() != null && !course.getStudents().contains(this)) {
                course.getStudents().add(this);
            }
        }
    }

    public void removeCourse(Course course) {
        if (this.courses != null) {
            this.courses.remove(course);
            // Maintain bidirectional relationship
            if (course.getStudents() != null) {
                course.getStudents().remove(this);
            }
        }
    }

    public void setCourses(List<Course> courses) {
        if (this.courses == null) {
            this.courses = new ArrayList<>();
        } else {
            this.courses.clear();
        }

        if (courses != null) {
            this.courses.addAll(courses);
        }
    }

    public List<Course> getCourses() {
        if (this.courses == null) {
            this.courses = new ArrayList<>();
        }
        return this.courses;
    }
}