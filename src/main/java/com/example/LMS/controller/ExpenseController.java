package com.example.LMS.controller;



import com.example.LMS.model.Course;
import com.example.LMS.model.Student;
import com.example.LMS.model.Teacher;
import com.example.LMS.repository.CourseRepository;
import com.example.LMS.repository.StudentRepository;
import com.example.LMS.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    /**
     * Calculate total expenses (teacher salaries + potential student fees)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/total")
    public ResponseEntity<Map<String, Object>> getTotalExpenses() {
        Map<String, Object> response = new HashMap<>();

        // Calculate total teacher salaries
        List<Teacher> teachers = teacherRepository.findAll();
        double totalSalaries = teachers.stream()
                .mapToDouble(Teacher::getSalary)
                .sum();

        // Calculate potential revenue from students (students * course fees)
        List<Student> students = studentRepository.findAll();
        List<Course> courses = courseRepository.findAll();

        double potentialRevenue = 0;
        for (Student student : students) {
            if (student.getCourses() != null) {
                potentialRevenue += student.getCourses().stream()
                        .mapToDouble(Course::getFee)
                        .sum();
            }
        }

        // Calculate net (revenue - expenses)
        double netAmount = potentialRevenue - totalSalaries;

        // Prepare response
        response.put("totalTeacherSalaries", totalSalaries);
        response.put("potentialStudentRevenue", potentialRevenue);
        response.put("netAmount", netAmount);
        response.put("teacherCount", teachers.size());
        response.put("studentCount", students.size());
        response.put("courseCount", courses.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Get breakdown by teachers
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/teachers")
    public ResponseEntity<Map<String, Object>> getTeacherExpenses() {
        List<Teacher> teachers = teacherRepository.findAll();

        Map<String, Object> response = new HashMap<>();
        response.put("teachers", teachers);
        response.put("totalSalaries", teachers.stream().mapToDouble(Teacher::getSalary).sum());

        return ResponseEntity.ok(response);
    }

    /**
     * Get potential revenue breakdown by courses
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getPotentialRevenue() {
        List<Course> courses = courseRepository.findAll();
        List<Student> students = studentRepository.findAll();

        Map<String, Object> response = new HashMap<>();

        double totalPotentialRevenue = 0;
        for (Student student : students) {
            if (student.getCourses() != null) {
                totalPotentialRevenue += student.getCourses().stream()
                        .mapToDouble(Course::getFee)
                        .sum();
            }
        }

        response.put("courses", courses);
        response.put("totalPotentialRevenue", totalPotentialRevenue);
        response.put("enrolledStudents", students.size());

        return ResponseEntity.ok(response);
    }
}