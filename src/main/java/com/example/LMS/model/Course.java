package com.example.LMS.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.HashSet; // Set uchun import
import java.util.Set;     // Set uchun import

@Entity
@Data
@Table(name = "courses") // Jadval nomini aniqlaymiz
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Course name is required!")
    @Size(min = 3, max = 100, message = "Course name must be between 3 and 100 characters!")
    private String name;
    private String description;

    @NotNull(message = "Course fee is required!")
    @Positive(message = "Course fee must be positive!")
    private Double fee;

    // Teacher entitiysi bilan Many-to-Many bog'lanish
    // Bu kursga dars beradigan o'qituvchilar
    @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY) // Teacher dagi "courses" maydoniga bog'lanadi
    private Set<Teacher> teachers = new HashSet<>(); // NullPointerException ni oldini olish uchun boshlang'ich qiymat
}
