package com.example.LMS.serviceImpl;

import com.example.LMS.model.Course;
import com.example.LMS.repository.CourseRepository;
import com.example.LMS.service.CourseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository){
        this.courseRepository = courseRepository;
    }


    @Override
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    public Course addCourse(Course course) {
        return courseRepository.save(course);
    }

    @Override
    public void deleteCourse(Long id){
        try{
            courseRepository.deleteById(id);
        } catch (RuntimeException e) {
            throw new RuntimeException("Course not found!");
        }
    }
}
