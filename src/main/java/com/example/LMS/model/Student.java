package com.example.LMS.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    // FIXED: Simplified relationship management
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "student_course",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    @JsonIgnoreProperties({"students", "teachers"})
    private Set<Course> courses = new HashSet<>();

    public Student(String name, String email, String phone, String enrollmentDate) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.enrollmentDate = enrollmentDate;
        this.courses = new HashSet<>();
    }

    // FIXED: Simplified setter that ensures courses is never null
    public void setCourses(Set<Course> courses) {
        if (this.courses == null) {
            this.courses = new HashSet<>();
        } else {
            this.courses.clear();
        }

        if (courses != null) {
            this.courses.addAll(courses);
        }
    }

    // FIXED: Ensure courses is never null
    public Set<Course> getCourses() {
        if (this.courses == null) {
            this.courses = new HashSet<>();
        }
        return this.courses;
    }

    // Helper methods for easier course management
    public void addCourse(Course course) {
        if (course != null) {
            getCourses().add(course);
        }
    }

    public void removeCourse(Course course) {
        if (course != null) {
            getCourses().remove(course);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student)) return false;
        Student student = (Student) o;
        return id != null && id.equals(student.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", enrollmentDate='" + enrollmentDate + '\'' +
                ", coursesCount=" + (courses != null ? courses.size() : 0) +
                '}';
    }
}