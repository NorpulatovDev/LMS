package com.example.LMS.service;


import com.example.LMS.model.Student;

import java.util.List;


public interface StudentService {
    public List<Student> getAllStudents();

    public Student addStudent(Student student);


}
