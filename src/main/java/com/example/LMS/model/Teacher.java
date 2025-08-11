package com.example.LMS.model;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private double salary;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole userRole;
}
