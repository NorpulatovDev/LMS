package com.example.LMS.serviceImpl;

import com.example.LMS.dto.StudentRequest;
import com.example.LMS.exception.ResourceNotFoundException;
import com.example.LMS.model.Course;
import com.example.LMS.model.Student;
import com.example.LMS.repository.CourseRepository;
import com.example.LMS.repository.StudentRepository;
import com.example.LMS.service.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
@Transactional
public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public StudentServiceImpl(StudentRepository studentRepository, CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }


    @Override
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    public Student getStudentById(Long id) {
        return studentRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Student not found with id: " + id)
        );
    }

    @Override
    @Transactional
    public Student createStudent(StudentRequest request) {
        Student student = new Student();
        student.setName(request.getName());
        student.setEmail(request.getEmail());
        student.setPhone(request.getPhone());

        // Set enrollment date - use current date if not provided
        if (request.getEnrollmentDate() == null || request.getEnrollmentDate().isEmpty()) {
            student.setEnrollmentDate(LocalDate.now().toString());
        } else {
            student.setEnrollmentDate(request.getEnrollmentDate());
        }

        // Handle courses - Initialize as empty list if null
        List<Course> courses = new ArrayList<>();
        if (request.getCourseIds() != null && !request.getCourseIds().isEmpty()) {
            courses = courseRepository.findAllById(request.getCourseIds());
            if (courses.size() != request.getCourseIds().size()) {
                throw new ResourceNotFoundException("One or more courses not found");
            }
        }
        student.setCourses(courses);

        // Save student
        Student savedStudent = studentRepository.save(student);

        // Fetch the saved student with courses to ensure proper loading
        return studentRepository.findById(savedStudent.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Failed to save student"));
    }

    @Override
    @Transactional
    public Student updateStudent(Long id, Student studentDetails) {
        Student existingStudent = getStudentById(id);

        // Update basic fields
        existingStudent.setName(studentDetails.getName());
        existingStudent.setEmail(studentDetails.getEmail());
        existingStudent.setPhone(studentDetails.getPhone());
        existingStudent.setEnrollmentDate(studentDetails.getEnrollmentDate());

        // Update courses if provided
        if (studentDetails.getCourses() != null) {
            existingStudent.setCourses(studentDetails.getCourses());
        }

        Student savedStudent = studentRepository.save(existingStudent);

        // Return the updated student with properly loaded courses
        return studentRepository.findById(savedStudent.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Failed to update student"));
    }

    @Override
    public void deleteStudent(Long id) {
        Student student = getStudentById(id);
        studentRepository.delete(student);
    }
}