package com.example.LMS.controller;

import com.example.LMS.dto.TeacherCreationDto;
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
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/financial-summary")
    public ResponseEntity<Map<String, Object>> getFinancialSummary(
            @Parameter(description = "Month in format YYYY-MM", example = "2025-08")
            @RequestParam(required = false) String month) {
        if (month == null || month.isEmpty()) {
            month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        Map<String, Object> summary = new HashMap<>();

        List<Payment> payments = paymentRepository.findPaymentsByMonth(month);
        double revenue = payments.stream().mapToDouble(Payment::getAmount).sum();

        List<Teacher> teachers = teacherRepository.findAll();
        double salaries = teachers.stream().mapToDouble(Teacher::getSalary).sum();

        double utilities = expenseService.getTotalExpensesByMonth(month);

        double totalExpenses = salaries + utilities;
        double netProfit = revenue - totalExpenses;

        summary.put("month", month);
        summary.put("revenue", revenue);
        summary.put("teacherSalaries", salaries);
        summary.put("utilityExpenses", utilities);
        summary.put("totalExpenses", totalExpenses);
        summary.put("netProfit", netProfit);
        summary.put("paymentsCount", payments.size());
        summary.put("teachersCount", teachers.size());

        return ResponseEntity.ok(summary);
    }

    // Payment Management
    @Operation(summary = "Get payments by month", description = "Retrieve all payment records for a specific month")
    @ApiResponse(responseCode = "200", description = "Monthly payments retrieved successfully")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/payments/by-month")
    public ResponseEntity<List<Payment>> getPaymentsByMonth(
            @Parameter(description = "Month in format YYYY-MM", example = "2025-08")
            @RequestParam(required = false) String month) {
        if (month == null || month.isEmpty()) {
            month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        List<Payment> payments = paymentRepository.findPaymentsByMonth(month);
        return ResponseEntity.ok(payments);
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