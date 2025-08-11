package com.example.LMS.serviceImpl;

import com.example.LMS.model.Payment;
import com.example.LMS.repository.PaymentRepository;
import com.example.LMS.service.PaymentService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;


    public PaymentServiceImpl(PaymentRepository paymentRepository){
        this.paymentRepository = paymentRepository;
    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public Payment addPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    public void deletePayment(Long id) {
        try{
            paymentRepository.deleteById(id);
        } catch (RuntimeException e) {
            throw new RuntimeException("Payment not found");
        }
    }
}
