package com.example.LMS.serviceImpl;

import com.example.LMS.model.Student;
import com.example.LMS.repository.StudentRepository;
import com.example.LMS.service.StudentService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;

    public StudentServiceImpl(StudentRepository studentRepository){
        this.studentRepository = studentRepository;
    }


    @Override
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    public Student addStudent(Student student) {
        return studentRepository.save(student);
    }
}
