package com.example.LMS.controller;

import com.example.LMS.dto.StudentRequest;
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
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id){
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @PostMapping
    public ResponseEntity<Student> addStudent(@Valid @RequestBody StudentRequest studentRequest){
        try {
            Student createdStudent = studentService.createStudent(studentRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentRequest studentRequest){
        try {
            // Convert StudentRequest to Student for update
            Student studentDetails = new Student();
            studentDetails.setName(studentRequest.getName());
            studentDetails.setEmail(studentRequest.getEmail());
            studentDetails.setPhone(studentRequest.getPhone());
            studentDetails.setEnrollmentDate(studentRequest.getEnrollmentDate());

            // Handle courses
            if (studentRequest.getCourseId() != null) {
                Course course = courseRepository.findById(studentRequest.getCourseId()).orElseThrow();
                studentDetails.getCourses().add(course);
            } else {
                studentDetails.setCourses(new ArrayList<>());
            }

            Student updatedStudent = studentService.updateStudent(id, studentDetails);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id){
        try {
            studentService.deleteStudent(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}