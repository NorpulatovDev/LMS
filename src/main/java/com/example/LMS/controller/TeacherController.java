package com.example.LMS.controller;

import com.example.LMS.exception.ResourceNotFoundException;
import com.example.LMS.model.Payment;
import com.example.LMS.model.Student;
import com.example.LMS.model.Teacher;
import com.example.LMS.repository.PaymentRepository;
import com.example.LMS.repository.TeacherRepository;
import com.example.LMS.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    // Teacher Profile Management
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping
    public ResponseEntity<List<Teacher>> getAllTeachers() {
        List<Teacher> teachers = teacherService.getAllTeachers();
        return ResponseEntity.ok(teachers);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/{id}")
    public ResponseEntity<Teacher> getTeacherById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        UserDetails currentUser = (UserDetails) authentication.getPrincipal();
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Teacher teacher = teacherService.getTeacherById(id);

        // Teachers can only view their own profile unless they are admin
        if (!isAdmin && !teacher.getUser().getUsername().equals(currentUser.getUsername())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(teacher);
    }

    // Payment Tracking for Teachers
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/my-payments")
    public ResponseEntity<List<Payment>> getMyPayments(Authentication auth) {
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        Teacher teacher = teacherRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        List<Payment> payments = paymentRepository.findPaymentsByTeacherId(teacher.getId());
        return ResponseEntity.ok(payments);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/my-payments/by-month")
    public ResponseEntity<List<Payment>> getMyPaymentsByMonth(
            @RequestParam(required = false) String month,
            Authentication auth) {

        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        Teacher teacher = teacherRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        List<Payment> allPayments = paymentRepository.findPaymentsByTeacherId(teacher.getId());

        // Filter by month if provided
        if (month != null && !month.isEmpty()) {
            allPayments = allPayments.stream()
                    .filter(p -> month.equals(p.getPaymentMonth()))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(allPayments);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/unpaid-students/{courseId}")
    public ResponseEntity<List<Student>> getUnpaidStudentsForCourse(
            @PathVariable Long courseId,
            @RequestParam(required = false) String month,
            Authentication auth) {

        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        Teacher teacher = teacherRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        // Check if teacher teaches this course
        if (!teacherRepository.teacherHasCourse(teacher.getId(), courseId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // If month not provided, use current month
        if (month == null || month.isEmpty()) {
            month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        List<Student> unpaidStudents = paymentRepository.findStudentsWithoutPayment(courseId, month);
        return ResponseEntity.ok(unpaidStudents);
    }
}