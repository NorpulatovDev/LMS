package com.example.LMS.serviceImpl;

import com.example.LMS.exception.ResourceNotFoundException;
import com.example.LMS.model.Course;
import com.example.LMS.model.Payment;
import com.example.LMS.model.Student;
import com.example.LMS.repository.CourseRepository;
import com.example.LMS.repository.PaymentRepository;
import com.example.LMS.repository.StudentRepository;
import com.example.LMS.service.PaymentService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;


    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              StudentRepository studentRepository,
                              CourseRepository courseRepository)
    {
        this.paymentRepository = paymentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public Payment addPayment(Payment payment) {
        // Only auto-fill paymentDate if not provided
        if (payment.getPaymentDate() == null || payment.getPaymentDate().isEmpty()) {
            payment.setPaymentDate(LocalDate.now().toString());
        }

        // Only auto-fill paymentMonth if not provided
        if (payment.getPaymentMonth() == null || payment.getPaymentMonth().isEmpty()) {
            // Extract month from paymentDate
            LocalDate date = LocalDate.parse(payment.getPaymentDate());
            payment.setPaymentMonth(date.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        }

        // Auto-fill student and course names for quick display (only if not provided)
        if (payment.getStudentName() == null || payment.getStudentName().isEmpty()) {
            Student student = studentRepository.findById(payment.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
            payment.setStudentName(student.getName());
        }

        if (payment.getCourseName() == null || payment.getCourseName().isEmpty()) {
            Course course = courseRepository.findById(payment.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            payment.setCourseName(course.getName());
        }

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
