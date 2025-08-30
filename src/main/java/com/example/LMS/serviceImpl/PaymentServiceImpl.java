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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              StudentRepository studentRepository,
                              CourseRepository courseRepository) {
        this.paymentRepository = paymentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    @Transactional
    public Payment addPayment(Payment payment) {
        System.out.println("=== ADDING PAYMENT DEBUG ===");
        System.out.println("Incoming payment: " + payment);

        // STEP 1: Validation
        if (payment.getStudentId() == null) {
            throw new IllegalArgumentException("Student ID is required");
        }
        if (payment.getCourseId() == null) {
            throw new IllegalArgumentException("Course ID is required");
        }
        if (payment.getAmount() == null || payment.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        // STEP 2: Verify entities exist
        Student student = studentRepository.findById(payment.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + payment.getStudentId()));

        Course course = courseRepository.findById(payment.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + payment.getCourseId()));

        System.out.println("Found student: " + student.getName());
        System.out.println("Found course: " + course.getName());

        // STEP 3: Check enrollment (optional validation)
        if (!student.getCourses().contains(course)) {
            System.out.println("WARNING: Student is not enrolled in this course");
            // You can choose to throw an exception or just log a warning
            // throw new IllegalArgumentException("Student is not enrolled in this course");
        }

        // STEP 4: Handle payment date
        if (payment.getPaymentDate() == null || payment.getPaymentDate().trim().isEmpty()) {
            payment.setPaymentDate(LocalDate.now().toString());
            System.out.println("Set payment date to today: " + payment.getPaymentDate());
        }

        // STEP 5: CRITICAL - Always set paymentMonth properly
        String calculatedMonth = payment.getPaymentMonth();
//        try {
//            LocalDate paymentDateParsed = LocalDate.parse(payment.getPaymentMonth());
//            calculatedMonth = paymentDateParsed.format(DateTimeFormatter.ofPattern("yyyy-MM"));
//        } catch (DateTimeParseException e) {
//            System.err.println("Invalid payment date format: " + payment.getPaymentDate());
//            throw new IllegalArgumentException("Invalid payment date format. Use YYYY-MM-DD");
//        }

        // Always use calculated month, even if paymentMonth was provided
        payment.setPaymentMonth(calculatedMonth);
        System.out.println("Set payment month to: " + calculatedMonth);

        // STEP 6: Check for duplicate payments
        boolean duplicateExists = paymentRepository.existsByStudentAndCourseAndMonth(
                payment.getStudentId(), payment.getCourseId(), calculatedMonth);

        if (duplicateExists) {
            throw new IllegalArgumentException("Payment already exists for " + student.getName() +
                    " in " + course.getName() + " for " + calculatedMonth);
        }

        // STEP 7: Validate amount against course fee (warning only)
        if (!payment.getAmount().equals(course.getFee())) {
            System.out.println("WARNING: Payment amount (" + payment.getAmount() +
                    ") differs from course fee (" + course.getFee() + ")");
        }

        // STEP 8: Set cached names for quick display
        payment.setStudentName(student.getName());
        payment.setCourseName(course.getName());

        // STEP 9: Save payment
        Payment savedPayment = paymentRepository.save(payment);
        System.out.println("Payment saved successfully: " + savedPayment);
        System.out.println("=== END PAYMENT DEBUG ===");

        return savedPayment;
    }

    @Override
    @Transactional
    public void deletePayment(Long id) {
        try {
            Payment payment = paymentRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

            System.out.println("Deleting payment: " + payment);
            paymentRepository.delete(payment);

        } catch (Exception e) {
            System.err.println("Error deleting payment: " + e.getMessage());
            throw new RuntimeException("Failed to delete payment: " + e.getMessage());
        }
    }

    // NEW: Helper method to fix existing payments
    @Transactional
    public void fixAllPaymentsWithMissingMonth() {
        List<Payment> paymentsToFix = paymentRepository.findPaymentsWithMissingMonth();

        System.out.println("Found " + paymentsToFix.size() + " payments to fix");

        for (Payment payment : paymentsToFix) {
            if (payment.getPaymentDate() != null && !payment.getPaymentDate().trim().isEmpty()) {
                try {
                    LocalDate date = LocalDate.parse(payment.getPaymentDate());
                    String month = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                    payment.setPaymentMonth(month);
                    paymentRepository.save(payment);
                    System.out.println("Fixed payment ID " + payment.getId() + " with month " + month);
                } catch (DateTimeParseException e) {
                    System.err.println("Could not parse date for payment " + payment.getId() +
                            ": " + payment.getPaymentDate());
                }
            }
        }
    }
}