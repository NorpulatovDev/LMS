package com.example.LMS.service;

import com.example.LMS.model.Course;
import com.example.LMS.model.Student;
import com.example.LMS.repository.CourseRepository;

import java.util.List;



public interface CourseService {
    public List<Course> getAllCourses();

    public Course addCourse(Course course);

    public void deleteCourse(Long id);

    public Course getCourseById(Long id);

    public Course updateCourse(Long id, Course courseDetails);

    public List<Student> getStudentsByCourseId(Long courseId);
}
