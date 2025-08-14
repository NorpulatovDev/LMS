package com.example.LMS.serviceImpl;

import com.example.LMS.exception.ResourceNotFoundException;
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
    public Teacher getTeacherById(Long id) {
        return teacherRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Teacher not found with id: " + id)
        );
    }

    @Override
    public Teacher addTeacher(Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    @Override
    public Teacher updateTeacher(Long id, Teacher teacherDetails) {
        Teacher teacher = getTeacherById(id);
        teacher.setEmail(teacher.getEmail());
        teacher.setName(teacher.getName());
        teacher.setSalary(teacher.getSalary());
        teacher.setPhone(teacher.getPhone());

        return teacher;
    }


    @Override
    public void deleteTeacher(Long id) {
        Teacher teacher = getTeacherById(id);
        teacherRepository.delete(teacher);
    }
}
