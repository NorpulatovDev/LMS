package com.example.LMS.model;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private String description;
    @Column(nullable = false)
    private double fee;
}
