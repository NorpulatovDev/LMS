package com.example.LMS.controller;

import com.example.LMS.dto.TeacherCreationDto;
import com.example.LMS.model.*;
import com.example.LMS.repository.*;
import com.example.LMS.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private ExpenseService expenseService;

    // EXPENSE MANAGEMENT
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/expenses")
    public ResponseEntity<Expense> addExpense(@Valid @RequestBody Expense expense) {
        return ResponseEntity.ok(expenseService.addExpense(expense));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/expenses")
    public ResponseEntity<List<Expense>> getAllExpenses() {
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/expenses/by-month")
    public ResponseEntity<List<Expense>> getExpensesByMonth(@RequestParam(required = false) String month) {
        if (month == null || month.isEmpty()) {
            month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        return ResponseEntity.ok(expenseService.getExpensesByMonth(month));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/financial-summary")
    public ResponseEntity<Map<String, Object>> getFinancialSummary(@RequestParam(required = false) String month) {
        if (month == null || month.isEmpty()) {
            month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        Map<String, Object> summary = new HashMap<>();

        // Calculate revenue from payments
        List<Payment> payments = paymentRepository.findPaymentsByMonth(month);
        double revenue = payments.stream().mapToDouble(Payment::getAmount).sum();

        // Calculate teacher salaries (fixed monthly cost)
        List<Teacher> teachers = teacherRepository.findAll();
        double salaries = teachers.stream().mapToDouble(Teacher::getSalary).sum();

        // Calculate utility expenses
        double utilities = expenseService.getTotalExpensesByMonth(month);

        // Calculate net profit
        double totalExpenses = salaries + utilities;
        double netProfit = revenue - totalExpenses;

        // Add detailed information for debugging
        summary.put("month", month);
        summary.put("revenue", revenue);
        summary.put("teacherSalaries", salaries);
        summary.put("utilityExpenses", utilities);
        summary.put("totalExpenses", totalExpenses);
        summary.put("netProfit", netProfit);

        // ADD DEBUGGING INFO
        summary.put("paymentsCount", payments.size());
        summary.put("teachersCount", teachers.size());
        summary.put("paymentDetails", payments.stream().map(p -> {
            Map<String, Object> paymentInfo = new HashMap<>();
            paymentInfo.put("studentName", p.getStudentName());
            paymentInfo.put("courseName", p.getCourseName());
            paymentInfo.put("amount", p.getAmount());
            paymentInfo.put("paymentMonth", p.getPaymentMonth());
            return paymentInfo;
        }).collect(Collectors.toList()));

        return ResponseEntity.ok(summary);
    }

//    // FINANCIAL SUMMARY (Revenue - All Expenses)
//    @PreAuthorize("hasRole('ADMIN')")
//    @GetMapping("/financial-summary")
//    public ResponseEntity<Map<String, Object>> getFinancialSummary(@RequestParam(required = false) String month) {
//        if (month == null || month.isEmpty()) {
//            month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
//        }
//
//        Map<String, Object> summary = new HashMap<>();
//
//        // Calculate revenue from payments
//        List<Payment> payments = paymentRepository.findPaymentsByMonth(month);
//        double revenue = payments.stream().mapToDouble(Payment::getAmount).sum();
//
//        // Calculate teacher salaries (fixed monthly cost)
//        List<Teacher> teachers = teacherRepository.findAll();
//        double salaries = teachers.stream().mapToDouble(Teacher::getSalary).sum();
//
//        // Calculate utility expenses
//        double utilities = expenseService.getTotalExpensesByMonth(month);
//
//        // Calculate net profit
//        double totalExpenses = salaries + utilities;
//        double netProfit = revenue - totalExpenses;
//
//        summary.put("month", month);
//        summary.put("revenue", revenue);
//        summary.put("teacherSalaries", salaries);
//        summary.put("utilityExpenses", utilities);
//        summary.put("totalExpenses", totalExpenses);
//        summary.put("netProfit", netProfit);
//
//        return ResponseEntity.ok(summary);
//    }

    // Payment Management (existing endpoints)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/payments/by-month")
    public ResponseEntity<List<Payment>> getPaymentsByMonth(@RequestParam(required = false) String month) {
        if (month == null || month.isEmpty()) {
            month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        List<Payment> payments = paymentRepository.findPaymentsByMonth(month);
        return ResponseEntity.ok(payments);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/payments/unpaid/{courseId}")
    public ResponseEntity<List<Student>> getUnpaidStudents(
            @PathVariable Long courseId,
            @RequestParam(required = false) String month) {
        if (month == null || month.isEmpty()) {
            month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        List<Student> unpaidStudents = paymentRepository.findStudentsWithoutPayment(courseId, month);
        return ResponseEntity.ok(unpaidStudents);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/payments/all-unpaid")
    public ResponseEntity<List<Map<String, Object>>> getAllUnpaidStudents(
            @RequestParam(required = false) String month) {
        if (month == null || month.isEmpty()) {
            month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        List<Course> allCourses = courseRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Course course : allCourses) {
            List<Student> unpaidStudents = paymentRepository.findStudentsWithoutPayment(course.getId(), month);
            for (Student student : unpaidStudents) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("studentId", student.getId());
                entry.put("studentName", student.getName());
                entry.put("courseId", course.getId());
                entry.put("courseName", course.getName());
                entry.put("courseFee", course.getFee());
                entry.put("month", month);
                result.add(entry);
            }
        }
        return ResponseEntity.ok(result);
    }

    // Teacher Management (existing endpoints)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/teachers")
    public ResponseEntity<String> createTeacher(@Valid @RequestBody TeacherCreationDto teacherDto) {
        if (userRepository.findByUsername(teacherDto.getUsername()).isPresent()) {
            return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
        }

        User newUser = new User();
        newUser.setUsername(teacherDto.getUsername());
        newUser.setPassword(passwordEncoder.encode(teacherDto.getPassword()));

        Role teacherRole = roleRepository.findByName("TEACHER")
                .orElseThrow(() -> new RuntimeException("Role 'TEACHER' not found! Please ensure 'TEACHER' role exists in the database."));
        newUser.setRoles(Collections.singleton(teacherRole));

        User savedUser = userRepository.save(newUser);

        Teacher newTeacher = new Teacher();
        newTeacher.setName(teacherDto.getName());
        newTeacher.setEmail(teacherDto.getEmail());
        newTeacher.setPhone(teacherDto.getPhone());
        newTeacher.setSalary(teacherDto.getSalary());
        newTeacher.setUser(savedUser);

        teacherRepository.save(newTeacher);

        return new ResponseEntity<>("Teacher '" + teacherDto.getUsername() + "' created successfully!", HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/teachers/{id}")
    public ResponseEntity<Teacher> updateTeacher(@PathVariable Long id, @Valid @RequestBody Teacher teacherDetails) {
        Optional<Teacher> optionalTeacher = teacherRepository.findById(id);

        if (optionalTeacher.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Teacher existingTeacher = optionalTeacher.get();
        existingTeacher.setName(teacherDetails.getName());
        existingTeacher.setEmail(teacherDetails.getEmail());
        existingTeacher.setPhone(teacherDetails.getPhone());
        existingTeacher.setSalary(teacherDetails.getSalary());

        Teacher updatedTeacher = teacherRepository.save(existingTeacher);
        return ResponseEntity.ok(updatedTeacher);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/teachers/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        Optional<Teacher> optionalTeacher = teacherRepository.findById(id);

        if (optionalTeacher.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Teacher teacher = optionalTeacher.get();
        User user = teacher.getUser();

        teacherRepository.delete(teacher);

        if (user != null) {
            userRepository.delete(user);
        }
        return ResponseEntity.noContent().build();
    }
}