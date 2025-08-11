package com.example.LMS.controller;


import com.example.LMS.model.Student;
import com.example.LMS.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService){
        this.studentService = studentService;
    }

    @GetMapping
    public List<Student> getAllStudents(){
        return studentService.getAllStudents();
    }

    @PostMapping
    public ResponseEntity<Student> addStudent(@RequestBody Student student){
        return ResponseEntity.ok(studentService.addStudent(student));
    }
}
