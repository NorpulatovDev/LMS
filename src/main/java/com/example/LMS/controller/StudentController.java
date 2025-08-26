package com.example.LMS.controller;

import com.example.LMS.dto.StudentRequest;
import com.example.LMS.exception.ResourceNotFoundException;
import com.example.LMS.model.Course;
import com.example.LMS.model.Student;
import com.example.LMS.repository.CourseRepository;
import com.example.LMS.service.StudentService;
import com.example.LMS.serviceImpl.StudentServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

@RestController
@CrossOrigin
@RequestMapping("/students")
public class StudentController {
    private final StudentService studentService;
    private final StudentServiceImpl studentServiceImpl;

    @Autowired
    private CourseRepository courseRepository;

    public StudentController(StudentService studentService, StudentServiceImpl studentServiceImpl){
        this.studentService = studentService;
        this.studentServiceImpl = studentServiceImpl;
    }

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

    @PostMapping
    public ResponseEntity<?> addStudent(@Valid @RequestBody StudentRequest studentRequest){
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
                    if (!courseRepository.existsById(courseId)) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("error", "Course with ID " + courseId + " not found"));
                    }
                }
            }

            Student createdStudent = studentService.createStudent(studentRequest);
            System.out.println("Created student: " + createdStudent);
            System.out.println("Student courses: " + createdStudent.getCourses().size());

            return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);

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

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentRequest studentRequest){
        try {
            System.out.println("Updating student with ID: " + id);
            System.out.println("Update request: " + studentRequest);

            // Convert StudentRequest to Student
            Student studentDetails = new Student();
            studentDetails.setName(studentRequest.getName());
            studentDetails.setEmail(studentRequest.getEmail());
            studentDetails.setPhone(studentRequest.getPhone());
            studentDetails.setEnrollmentDate(studentRequest.getEnrollmentDate());

            // Handle courses
            Set<Course> courses = new HashSet<>();
            if (studentRequest.getCourseIds() != null && !studentRequest.getCourseIds().isEmpty()) {
                for (Long courseId : studentRequest.getCourseIds()) {
                    Course course = courseRepository.findById(courseId)
                            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
                    courses.add(course);
                }
            }
            studentDetails.setCourses(courses);

            Student updatedStudent = studentService.updateStudent(id, studentDetails);
            System.out.println("Updated student with courses: " + updatedStudent.getCourses().size());

            return ResponseEntity.ok(updatedStudent);
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

    @DeleteMapping("/{id}")
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

    // Additional endpoints for course management
    @PostMapping("/{studentId}/courses/{courseId}")
    public ResponseEntity<?> addStudentToCourse(@PathVariable Long studentId, @PathVariable Long courseId) {
        try {
            Student updatedStudent = studentServiceImpl.addStudentToCourse(studentId, courseId);
            return ResponseEntity.ok(updatedStudent);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{studentId}/courses/{courseId}")
    public ResponseEntity<?> removeStudentFromCourse(@PathVariable Long studentId, @PathVariable Long courseId) {
        try {
            Student updatedStudent = studentServiceImpl.removeStudentFromCourse(studentId, courseId);
            return ResponseEntity.ok(updatedStudent);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
}