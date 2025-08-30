package com.example.LMS.controller;

import com.example.LMS.dto.StudentRequest;
import com.example.LMS.exception.ResourceNotFoundException;
import com.example.LMS.model.Course;
import com.example.LMS.model.Student;
import com.example.LMS.repository.CourseRepository;
import com.example.LMS.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/students")
public class StudentController {
    private final StudentService studentService;

    @Autowired
    private CourseRepository courseRepository;

    public StudentController(StudentService studentService){
        this.studentService = studentService;
    }

    // Essential CRUD Operations
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents(){
        try {
            List<Student> students = studentService.getAllStudents();
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            System.err.println("Error getting all students: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id){
        try {
            Student student = studentService.getStudentById(id);
            return ResponseEntity.ok(student);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            System.err.println("Error getting student by id: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Transactional
    public ResponseEntity<?> createStudent(@Valid @RequestBody StudentRequest studentRequest){
        try {
            System.out.println("Received student request: " + studentRequest);

            // Validate the request
            if (studentRequest.getName() == null || studentRequest.getName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Student name is required"));
            }

            if (studentRequest.getPhone() == null || studentRequest.getPhone().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Student phone is required"));
            }

            // Validate course IDs if provided
            if (studentRequest.getCourseIds() != null && !studentRequest.getCourseIds().isEmpty()) {
                for (Long courseId : studentRequest.getCourseIds()) {
                    if (courseId == null) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("error", "Course ID cannot be null"));
                    }
                    if (!courseRepository.existsById(courseId)) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("error", "Course with ID " + courseId + " not found"));
                    }
                }
            }

            Student createdStudent = studentService.createStudent(studentRequest);
            Student reloadedStudent = studentService.getStudentById(createdStudent.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(reloadedStudent);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error creating student: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentRequest studentRequest){
        try {
            // Validate the request
            if (studentRequest.getName() == null || studentRequest.getName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Student name is required"));
            }

            if (studentRequest.getPhone() == null || studentRequest.getPhone().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Student phone is required"));
            }

            // Convert StudentRequest to Student
            Student studentDetails = new Student();
            studentDetails.setName(studentRequest.getName());
            studentDetails.setEmail(studentRequest.getEmail());
            studentDetails.setPhone(studentRequest.getPhone());
            studentDetails.setEnrollmentDate(studentRequest.getEnrollmentDate());

            // Handle courses if provided
            if (studentRequest.getCourseIds() != null && !studentRequest.getCourseIds().isEmpty()) {
                Set<Course> courses = new HashSet<>();
                for (Long courseId : studentRequest.getCourseIds()) {
                    if (courseId == null) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("error", "Course ID cannot be null"));
                    }

                    Course course = courseRepository.findById(courseId)
                            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
                    courses.add(course);
                }
                studentDetails.setCourses(courses);
            } else {
                // If no course IDs provided, set empty set
                studentDetails.setCourses(new HashSet<>());
            }

            Student updatedStudent = studentService.updateStudent(id, studentDetails);

            // Reload the student to ensure all data is fresh
            Student reloadedStudent = studentService.getStudentById(updatedStudent.getId());

            return ResponseEntity.ok(reloadedStudent);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error updating student: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteStudent(@PathVariable Long id){
        try {
            studentService.deleteStudent(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error deleting student: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
}