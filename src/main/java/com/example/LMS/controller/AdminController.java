package com.example.LMS.controller;

import com.example.LMS.dto.TeacherCreationDto;
import com.example.LMS.model.*;
import com.example.LMS.repository.*;
import jakarta.validation.Valid; // Validatsiya uchun
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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


    /**
     * Get all payments for current month
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/payments/by-month")
    public ResponseEntity<List<Payment>> getPaymentsByMonth(
            @RequestParam(required = false) String month) {
        // If month not provided, use current month
        if (month == null || month.isEmpty()) {
            month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        List<Payment> payments = paymentRepository.findPaymentsByMonth(month);
        return ResponseEntity.ok(payments);
    }

    /**
     * Get students who haven't paid this month for a specific course
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/payments/unpaid/{courseId}")
    public ResponseEntity<List<Student>> getUnpaidStudents(
            @PathVariable Long courseId,
            @RequestParam(required = false) String month) {
        // If month not provided, use current month
        if (month == null || month.isEmpty()) {
            month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        List<Student> unpaidStudents = paymentRepository.findStudentsWithoutPayment(courseId, month);
        return ResponseEntity.ok(unpaidStudents);
    }

    /**
     * Get all students who haven't paid this month (any course)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/payments/all-unpaid")
    public ResponseEntity<List<Map<String, Object>>> getAllUnpaidStudents(
            @RequestParam(required = false) String month) {
        // If month not provided, use current month
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
        User user = teacher.getUser(); // Get the associated user

        // Delete teacher first (due to foreign key constraint)
        teacherRepository.delete(teacher);

        // Then delete the user
        if (user != null) {
            userRepository.delete(user);
        }

        return ResponseEntity.noContent().build();
    }
}
