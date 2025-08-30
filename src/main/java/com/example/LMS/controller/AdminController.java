package com.example.LMS.controller;

import com.example.LMS.dto.TeacherCreationDto;
import com.example.LMS.exception.ResourceNotFoundException;
import com.example.LMS.model.*;
import com.example.LMS.repository.*;
import com.example.LMS.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin Management", description = "Administrative operations for managing LMS system including expenses, financial summaries, teachers, and payments")
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
    @Autowired
    private ExpenseRepository expenseRepository;

    // EXPENSE MANAGEMENT
    @Operation(summary = "Add new expense", description = "Create a new expense record for the institution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Expense.class))),
            @ApiResponse(responseCode = "400", description = "Invalid expense data"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/expenses")
    public ResponseEntity<Expense> addExpense(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Expense details to create",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Expense.class),
                            examples = @ExampleObject(
                                    name = "expense_example",
                                    summary = "Sample expense",
                                    value = """
                        {
                            "name": "Electricity Bill",
                            "amount": 250.50,
                            "expenseDate": "2025-08-29",
                            "expenseMonth": "2025-08"
                        }
                        """
                            )
                    )
            )
            @Valid @RequestBody Expense expense) {
        return ResponseEntity.ok(expenseService.addExpense(expense));
    }

    @Operation(summary = "Get all expenses", description = "Retrieve all expense records")
    @ApiResponse(responseCode = "200", description = "List of all expenses retrieved successfully")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/expenses")
    public ResponseEntity<List<Expense>> getAllExpenses() {
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @Operation(summary = "Get expenses by month", description = "Retrieve expenses filtered by specific month")
    @ApiResponse(responseCode = "200", description = "Monthly expenses retrieved successfully")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/expenses/by-month")
    public ResponseEntity<List<Expense>> getExpensesByMonth(
            @Parameter(description = "Month in format YYYY-MM (e.g., 2025-08). If not provided, current month is used",
                    example = "2025-08")
            @RequestParam(required = false) String month) {
        if (month == null || month.isEmpty()) {
            month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        return ResponseEntity.ok(expenseService.getExpensesByMonth(month));
    }

    @Operation(summary = "Delete expense", description = "Remove an expense record by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Expense deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Expense not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<Void> deleteExpense(
            @Parameter(description = "Expense ID to delete", example = "1")
            @PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get financial summary", description = "Get comprehensive financial overview including revenue, expenses, and profit")
    @ApiResponse(responseCode = "200", description = "Financial summary retrieved successfully",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "financial_summary",
                            summary = "Sample financial summary",
                            value = """
                {
                    "month": "2025-08",
                    "revenue": 5000.00,
                    "teacherSalaries": 3000.00,
                    "utilityExpenses": 500.00,
                    "totalExpenses": 3500.00,
                    "netProfit": 1500.00,
                    "paymentsCount": 20,
                    "teachersCount": 3
                }
                """
                    )
            ))
    // SIMPLE FIX: Updated financial summary without new Expense fields
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/financial-summary")
    public ResponseEntity<Map<String, Object>> getFinancialSummary(
            @Parameter(description = "Month in format YYYY-MM", example = "2025-08")
            @RequestParam(required = false) String month) {

        try {
            if (month == null || month.isEmpty()) {
                month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            }

            if (!month.matches("\\d{4}-\\d{2}")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Month must be in format YYYY-MM (e.g., 2025-08)"));
            }

            Map<String, Object> summary = new HashMap<>();

            // STEP 1: Calculate Revenue
            List<Payment> payments = paymentRepository.findPaymentsByMonth(month);
            double revenue = payments.stream()
                    .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0)
                    .sum();

            // STEP 2: Calculate ONLY recorded expenses (correct logic!)
            double recordedExpenses = expenseService.getTotalExpensesByMonth(month);
//            if (recordedExpenses == null) recordedExpenses = 0.0;

            // STEP 3: Calculate profit based on actual expenses
            double netProfit = revenue - recordedExpenses;

            // STEP 4: Get all expenses for breakdown
            List<Expense> expensesList = expenseService.getExpensesByMonth(month);

            // STEP 5: Teacher information (without requiring new expense fields)
            List<Teacher> allTeachers = teacherRepository.findAll();
            double potentialSalaryCost = allTeachers.stream()
                    .mapToDouble(t -> t.getSalary() != null ? t.getSalary() : 0.0)
                    .sum();

            // STEP 6: Build response
            summary.put("month", month);
            summary.put("revenue", Math.round(revenue * 100.0) / 100.0);
            summary.put("recordedExpenses", Math.round(recordedExpenses * 100.0) / 100.0);
            summary.put("netProfit", Math.round(netProfit * 100.0) / 100.0);

            // Payment metrics
            summary.put("payments", Map.of(
                    "count", payments.size(),
                    "averageAmount", payments.isEmpty() ? 0.0 : Math.round((revenue / payments.size()) * 100.0) / 100.0
            ));

            // Expense breakdown
            summary.put("expenses", Map.of(
                    "totalRecorded", recordedExpenses,
                    "expensesList", expensesList.stream()
                            .map(e -> Map.of(
                                    "id", e.getId(),
                                    "name", e.getName(),
                                    "amount", e.getAmount(),
                                    "date", e.getExpenseDate()
                            ))
                            .collect(Collectors.toList())
            ));

            // Teacher info (informational only - not included in profit calculation)
            summary.put("teacherInfo", Map.of(
                    "totalTeachers", allTeachers.size(),
                    "potentialMonthlySalaryCost", Math.round(potentialSalaryCost * 100.0) / 100.0,
                    "note", "Salary costs are only included when admin records them as expenses"
            ));

            // Business metrics
            summary.put("metrics", Map.of(
                    "profitMargin", revenue == 0 ? 0.0 : Math.round((netProfit / revenue * 100) * 100.0) / 100.0,
                    "expenseRatio", revenue == 0 ? 0.0 : Math.round((recordedExpenses / revenue * 100) * 100.0) / 100.0,
                    "isProfitable", netProfit > 0
            ));

            // Simple recommendations
            List<String> recommendations = new ArrayList<>();
            if (netProfit < 0) {
                recommendations.add("Business is operating at a loss based on recorded expenses.");
            }
            if (revenue == 0 && recordedExpenses > 0) {
                recommendations.add("No revenue but expenses recorded. Focus on student enrollment.");
            }
            if (recordedExpenses == 0 && allTeachers.size() > 0) {
                recommendations.add("No expenses recorded yet. Consider recording teacher salaries and utility costs.");
            }

            summary.put("recommendations", recommendations);

            return ResponseEntity.ok(summary);

        } catch (Exception e) {
            System.err.println("Error in financial summary: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error calculating financial summary: " + e.getMessage()));
        }
    }

    // NEW: Helper endpoint to pay teacher salaries
    @Operation(summary = "Pay teacher salary", description = "Record a salary payment as an expense")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/pay-teacher-salary")
    public ResponseEntity<Map<String, Object>> payTeacherSalary(
            @RequestBody Map<String, Object> paymentRequest) {

        try {
            Long teacherId = Long.valueOf(paymentRequest.get("teacherId").toString());
            String month = (String) paymentRequest.get("month");
            Double customAmount = paymentRequest.containsKey("amount") ?
                    Double.valueOf(paymentRequest.get("amount").toString()) : null;

            // Get teacher details
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

            // Check if already paid this month
            if (expenseRepository.isTeacherPaidInMonth(teacherId, month)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Teacher " + teacher.getName() + " already paid for " + month));
            }

            // Create salary expense
            Expense salaryExpense = new Expense();
            salaryExpense.setName("Salary payment - " + teacher.getName());
            salaryExpense.setAmount(customAmount != null ? customAmount : teacher.getSalary());
            salaryExpense.setCategory(Expense.ExpenseCategory.SALARY);
            salaryExpense.setTeacherId(teacherId);
            salaryExpense.setTeacherName(teacher.getName());
            salaryExpense.setDescription("Monthly salary payment");

            // Set dates
            if (month == null || month.isEmpty()) {
                month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            }
            salaryExpense.setExpenseMonth(month);
            salaryExpense.setExpenseDate(LocalDate.now().toString());

            Expense savedExpense = expenseRepository.save(salaryExpense);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Salary paid successfully");
            response.put("teacher", Map.of(
                    "id", teacher.getId(),
                    "name", teacher.getName(),
                    "salaryAmount", teacher.getSalary()
            ));
            response.put("payment", Map.of(
                    "expenseId", savedExpense.getId(),
                    "amount", savedExpense.getAmount(),
                    "month", month,
                    "date", savedExpense.getExpenseDate()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process salary payment: " + e.getMessage()));
        }
    }

    // NEW: Helper method to fix existing payments with missing paymentMonth
    private void fixPaymentsWithMissingMonth() {
        try {
            List<Payment> paymentsWithMissingMonth = paymentRepository.findPaymentsWithMissingMonth();

            if (!paymentsWithMissingMonth.isEmpty()) {
                System.out.println("Found " + paymentsWithMissingMonth.size() + " payments with missing paymentMonth. Fixing...");

                for (Payment payment : paymentsWithMissingMonth) {
                    if (payment.getPaymentDate() != null && !payment.getPaymentDate().isEmpty()) {
                        try {
                            LocalDate date = LocalDate.parse(payment.getPaymentDate());
                            String month = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                            payment.setPaymentMonth(month);
                            paymentRepository.save(payment);
                            System.out.println("Fixed payment ID " + payment.getId() + " with month " + month);
                        } catch (Exception e) {
                            System.err.println("Could not parse date for payment " + payment.getId() + ": " + payment.getPaymentDate());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fixing payments with missing month: " + e.getMessage());
        }
    }

    // NEW: Debug endpoint to check all payments
    @Operation(summary = "Debug all payments", description = "Get all payment information for debugging")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/debug/payments")
    public ResponseEntity<Map<String, Object>> debugAllPayments() {
        Map<String, Object> debug = new HashMap<>();

        try {
            List<Payment> allPayments = paymentRepository.findAll();
            List<Payment> paymentsWithMissingMonth = paymentRepository.findPaymentsWithMissingMonth();

            debug.put("totalPayments", allPayments.size());
            debug.put("paymentsWithMissingMonth", paymentsWithMissingMonth.size());
            debug.put("allPayments", allPayments);

            // Group by month for analysis
            Map<String, Long> paymentsByMonth = allPayments.stream()
                    .filter(p -> p.getPaymentMonth() != null && !p.getPaymentMonth().isEmpty())
                    .collect(Collectors.groupingBy(Payment::getPaymentMonth, Collectors.counting()));

            debug.put("paymentsByMonth", paymentsByMonth);

            return ResponseEntity.ok(debug);

        } catch (Exception e) {
            debug.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(debug);
        }
    }

    // ENHANCED: Payment by month with debugging
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/payments/by-month")
    public ResponseEntity<Map<String, Object>> getPaymentsByMonth(
            @Parameter(description = "Month in format YYYY-MM", example = "2025-08")
            @RequestParam(required = false) String month) {

        if (month == null || month.isEmpty()) {
            month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        // Fix missing paymentMonth before querying
        fixPaymentsWithMissingMonth();

        List<Payment> payments = paymentRepository.findPaymentsByMonth(month);

        Map<String, Object> result = new HashMap<>();
        result.put("month", month);
        result.put("payments", payments);
        result.put("count", payments.size());
        result.put("totalAmount", payments.stream().mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0).sum());

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get unpaid students for course", description = "Get list of students who haven't paid for a specific course in given month")
    @ApiResponse(responseCode = "200", description = "Unpaid students list retrieved successfully")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/payments/unpaid/{courseId}")
    public ResponseEntity<List<Student>> getUnpaidStudents(
            @Parameter(description = "Course ID", example = "1")
            @PathVariable Long courseId,
            @Parameter(description = "Month in format YYYY-MM", example = "2025-08")
            @RequestParam(required = false) String month) {
        if (month == null || month.isEmpty()) {
            month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        List<Student> unpaidStudents = paymentRepository.findStudentsWithoutPayment(courseId, month);
        return ResponseEntity.ok(unpaidStudents);
    }

    @Operation(summary = "Get all unpaid students", description = "Get comprehensive list of all students who haven't paid across all courses")
    @ApiResponse(responseCode = "200", description = "All unpaid students retrieved successfully")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/payments/all-unpaid")
    public ResponseEntity<List<Map<String, Object>>> getAllUnpaidStudents(
            @Parameter(description = "Month in format YYYY-MM", example = "2025-08")
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

    // Teacher Management
    @Operation(summary = "Create new teacher", description = "Create a new teacher account with authentication credentials")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Teacher created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid teacher data or username already exists"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/teachers")
    public ResponseEntity<String> createTeacher(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Teacher creation details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TeacherCreationDto.class),
                            examples = @ExampleObject(
                                    name = "teacher_example",
                                    summary = "Sample teacher creation",
                                    value = """
                        {
                            "username": "john.doe",
                            "password": "securePassword123",
                            "name": "John Doe",
                            "email": "john.doe@example.com",
                            "phone": "+1-555-0123",
                            "salary": 3000.00
                        }
                        """
                            )
                    )
            )
            @Valid @RequestBody TeacherCreationDto teacherDto) {
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

    @Operation(summary = "Update teacher", description = "Update existing teacher information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teacher updated successfully"),
            @ApiResponse(responseCode = "404", description = "Teacher not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/teachers/{id}")
    public ResponseEntity<Teacher> updateTeacher(
            @Parameter(description = "Teacher ID to update", example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated teacher details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Teacher.class),
                            examples = @ExampleObject(
                                    name = "teacher_update_example",
                                    value = """
                        {
                            "name": "John Doe Updated",
                            "email": "john.updated@example.com",
                            "phone": "+1-555-9999",
                            "salary": 3500.00
                        }
                        """
                            )
                    )
            )
            @Valid @RequestBody Teacher teacherDetails) {
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

    @Operation(summary = "Delete teacher", description = "Delete a teacher and their associated user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Teacher deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Teacher not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/teachers/{id}")
    public ResponseEntity<Void> deleteTeacher(
            @Parameter(description = "Teacher ID to delete", example = "1")
            @PathVariable Long id) {
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