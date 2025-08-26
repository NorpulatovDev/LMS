package com.example.LMS.serviceImpl;

import com.example.LMS.exception.ResourceNotFoundException;
import com.example.LMS.model.Course;
import com.example.LMS.model.Student;
import com.example.LMS.repository.CourseRepository;
import com.example.LMS.repository.StudentRepository;
import com.example.LMS.service.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    public CourseServiceImpl(CourseRepository courseRepository, StudentRepository studentRepository) {
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Course getCourseById(Long id) {
        return courseRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Course not found with id: " + id)
        );
    }

    @Override
    @Transactional
    public Course addCourse(Course course) {
        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course existingCourse = getCourseById(id);
        courseRepository.delete(existingCourse);
    }

    @Override
    @Transactional
    public Course updateCourse(Long id, Course courseDetails) {
        Course existingCourse = getCourseById(id);
        existingCourse.setName(courseDetails.getName());
        existingCourse.setDescription(courseDetails.getDescription());
        existingCourse.setFee(courseDetails.getFee());

        if (courseDetails.getTeachers() != null) {
            existingCourse.setTeachers(courseDetails.getTeachers());
        }

        return courseRepository.save(existingCourse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> getStudentsByCourseId(Long courseId) {
        Course course = getCourseById(courseId);

        // Get all students who are enrolled in this course
        List<Student> allStudents = studentRepository.findAll();

        return allStudents.stream()
                .filter(student -> student.getCourses().stream()
                        .anyMatch(c -> c.getId().equals(courseId)))
                .collect(Collectors.toList());
    }
}