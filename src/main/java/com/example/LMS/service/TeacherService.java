package com.example.LMS.service;

import com.example.LMS.model.Teacher;

import java.util.List;


public interface TeacherService {
    public List<Teacher> getAllTeachers();

    public Teacher addTeacher(Teacher teacher);
}
