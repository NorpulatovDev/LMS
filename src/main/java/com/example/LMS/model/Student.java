package com.example.LMS.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "students")
@ToString(exclude = "courses") // Prevent circular toString
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    @JoinTable(
            name = "student_course",
            joinColumns = @JoinColumn(name = "student_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "course_id", referencedColumnName = "id")
    )
    @JsonIgnoreProperties({"students", "teachers"}) // Prevent circular reference
    private List<Course> courses = new ArrayList<>();

    // Default constructor
    public Student() {
        this.courses = new ArrayList<>();
    }

    // Constructor with basic fields
    public Student(String name, String email, String phone, String enrollmentDate) {
        this();
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.enrollmentDate = enrollmentDate;
    }

    // Helper methods
    public void addCourse(Course course) {
        if (this.courses == null) {
            this.courses = new ArrayList<>();
        }
        if (!this.courses.contains(course)) {
            this.courses.add(course);
        }
    }

    public void removeCourse(Course course) {
        if (this.courses != null) {
            this.courses.remove(course);
        }
    }

    public void clearCourses() {
        if (this.courses != null) {
            this.courses.clear();
        }
    }

    // Override equals and hashCode for proper entity comparison
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Student student = (Student) obj;
        return id != null && id.equals(student.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}