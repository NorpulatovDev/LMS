package com.example.LMS.controller;


import com.example.LMS.model.Teacher;
import com.example.LMS.service.TeacherService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teachers")
public class TeacherController {
    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService){
        this.teacherService = teacherService;
    }

    @GetMapping
    public List<Teacher> getAllTeachers(){
        return teacherService.getAllTeachers();
    }

    @PostMapping
    public Teacher addTeacher(@RequestBody Teacher teacher){
        return teacherService.addTeacher(teacher);
    }
}
