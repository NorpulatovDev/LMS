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
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@CrossOrigin
@RequestMapping("/students")
public class StudentController {
    private final StudentService studentService;

    @Autowired
    private CourseRepository courseRepository;

    public StudentController(StudentService studentService){
        this.studentService = studentService;
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
            System.out.println("Course IDs: " + studentRequest.getCourseIds());

            // Validate the request
            if (studentRequest.getName() == null || studentRequest.getName().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Student name is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            if (studentRequest.getPhone() == null || studentRequest.getPhone().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Student phone is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Validate course IDs if provided
            if (studentRequest.getCourseIds() != null && !studentRequest.getCourseIds().isEmpty()) {
                for (Long courseId : studentRequest.getCourseIds()) {
                    if (!courseRepository.existsById(courseId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Course with ID " + courseId + " not found");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                    }
                }
            }

            Student createdStudent = studentService.createStudent(studentRequest);
            System.out.println("Created student with courses: " +
                    (createdStudent.getCourses() != null ? createdStudent.getCourses().size() : 0));

            return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
        } catch (ResourceNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            System.err.println("Error creating student: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentRequest studentRequest){
        try {
            System.out.println("Updating student with ID: " + id);
            System.out.println("Update request: " + studentRequest);

            // Convert StudentRequest to Student for update
            Student studentDetails = new Student();
            studentDetails.setName(studentRequest.getName());
            studentDetails.setEmail(studentRequest.getEmail());
            studentDetails.setPhone(studentRequest.getPhone());
            studentDetails.setEnrollmentDate(studentRequest.getEnrollmentDate());

            // Handle courses
            List<Course> courses = new ArrayList<>();
            if (studentRequest.getCourseIds() != null && !studentRequest.getCourseIds().isEmpty()) {
                for (Long courseId : studentRequest.getCourseIds()) {
                    Course course = courseRepository.findById(courseId)
                            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
                    courses.add(course);
                }
            }
            studentDetails.setCourses(courses);

            Student updatedStudent = studentService.updateStudent(id, studentDetails);
            System.out.println("Updated student with courses: " +
                    (updatedStudent.getCourses() != null ? updatedStudent.getCourses().size() : 0));

            return ResponseEntity.ok(updatedStudent);
        } catch (ResourceNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            System.err.println("Error updating student: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id){
        try {
            studentService.deleteStudent(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            System.err.println("Error deleting student: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);

        }
    }
}