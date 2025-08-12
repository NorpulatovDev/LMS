package com.example.LMS.service;

import com.example.LMS.model.Teacher;

import java.util.List;


public interface TeacherService {
    public List<Teacher> getAllTeachers();

    public Teacher getTeacherById(Long id);

    public Teacher addTeacher(Teacher teacher);

    public Teacher updateTeacher(Long id, Teacher teacherDetails);

    public void deleteTeacher(Long id);
}
