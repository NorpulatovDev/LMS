package com.example.LMS.serviceImpl;

import com.example.LMS.model.Teacher;
import com.example.LMS.repository.TeacherRepository;
import com.example.LMS.service.TeacherService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TeacherServiceImpl implements TeacherService {
    private final TeacherRepository teacherRepository;

    public TeacherServiceImpl(TeacherRepository teacherRepository){
        this.teacherRepository =teacherRepository;
    }

    @Override
    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    @Override
    public Teacher addTeacher(Teacher teacher) {
        return teacherRepository.save(teacher);
    }
}
