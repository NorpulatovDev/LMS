package com.example.LMS.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "course_teacher",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "teacher_id")
    )
    @JsonIgnoreProperties({"courses", "user"})
    private Set<Teacher> teachers = new HashSet<>();

    // FIXED: Added cascade and proper initialization
    @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JsonIgnoreProperties({"courses"})
    private Set<Student> students = new HashSet<>();

    public Course(String name, String description, Double fee) {
        this.name = name;
        this.description = description;
        this.fee = fee;
        this.teachers = new HashSet<>();
        this.students = new HashSet<>();
    }

    // FIXED: Helper methods for bidirectional relationship
    public void addStudent(Student student) {
        if (student != null) {
            this.students.add(student);
            student.getCourses().add(this);
        }
    }

    public void removeStudent(Student student) {
        if (student != null) {
            this.students.remove(student);
            student.getCourses().remove(this);
        }
    }

    // FIXED: Ensure students set is properly initialized
    public Set<Student> getStudents() {
        if (this.students == null) {
            this.students = new HashSet<>();
        }
        return this.students;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course)) return false;
        Course course = (Course) o;
        return id != null && id.equals(course.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", fee=" + fee +
                ", studentsCount=" + (students != null ? students.size() : 0) +
                '}';
    }
}