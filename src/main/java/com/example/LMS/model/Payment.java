package com.example.LMS.model;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long studentId;
    @Column(nullable = false)
    private double amount;
    @Column(nullable = false)
    private String paymentDate;
}
