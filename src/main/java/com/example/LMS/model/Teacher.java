package com.example.LMS.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.util.HashSet; // Set uchun import
import java.util.Set;     // Set uchun import

@Data
@Entity
@Table(name = "teachers")
public class Teacher {

    @Id
    private Long id;

    @NotBlank(message = "Teacher name is required!")
    private String name;
    private String email;

    @NotBlank(message = "Teacher phone number is required!")
    private String phone;

    @NotNull(message = "Salary is required!")
    @Positive(message = "Salary must be positive!")
    private Double salary;

    // User entitiysi bilan one-to-one bog'lanish
    @OneToOne
    @MapsId // Teacher IDsi User IDsi bilan bir xil bo'ladi
    @JoinColumn(name = "id")
    private User user;

    // Course entitiysi bilan Many-to-Many bog'lanish
    // O'qituvchi dars beradigan kurslar
    @ManyToMany(mappedBy = "teachers")
    @JsonIgnore   // KEEP as JsonManagedReference
    private Set<Course> courses = new HashSet<>(); // NullPointerException ni oldini olish uchun boshlang'ich qiymat
}
