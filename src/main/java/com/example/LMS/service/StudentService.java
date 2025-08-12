package com.example.LMS.service;


import com.example.LMS.model.Student;

import java.util.List;


public interface StudentService {
    public List<Student> getAllStudents();

    public Student getStudentById(Long id);

    public Student addStudent(Student student);

    public Student updateStudent(Long id, Student studentDetails);

    public void deleteStudent(Long id);
}
