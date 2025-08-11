package com.example.LMS.service;

import com.example.LMS.model.Payment;

import java.util.List;


public interface PaymentService {
    public List<Payment> getAllPayments();

    public Payment addPayment(Payment payment);

    public void deletePayment(Long id);
}
