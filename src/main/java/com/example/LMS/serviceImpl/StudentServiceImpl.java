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
import java.util.List;


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
    @Transactional(readOnly = true)
    public List<Student> getAllStudents() {
        List<Student> students = studentRepository.findAll();
        // Force loading of courses for each student
        students.forEach(student -> {
            if (student.getCourses() != null) {
                student.getCourses().size(); // This forces lazy loading
            }
        });
        return students;
    }

    @Override
    @Transactional(readOnly = true)
    public Student getStudentById(Long id) {
        Student student = studentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Student not found with id: " + id)
        );
        // Force loading of courses
        if (student.getCourses() != null) {
            student.getCourses().size();
        }
        return student;
    }

    @Override
    @Transactional
    public Student createStudent(StudentRequest request) {
        // Validate required fields
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Student name is required");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Student phone is required");
        }

        Student student = new Student();
        student.setName(request.getName().trim());
        student.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
        student.setPhone(request.getPhone().trim());

        // Set enrollment date
        if (request.getEnrollmentDate() == null || request.getEnrollmentDate().trim().isEmpty()) {
            student.setEnrollmentDate(LocalDate.now().toString());
        } else {
            student.setEnrollmentDate(request.getEnrollmentDate().trim());
        }

        // Initialize courses list
        student.setCourses(new ArrayList<>());

        // Handle courses if provided
        if (request.getCourseIds() != null && !request.getCourseIds().isEmpty()) {
            System.out.println("Processing course IDs: " + request.getCourseIds()); // Debug log

            List<Course> courses = new ArrayList<>();
            for (Long courseId : request.getCourseIds()) {
                Course course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
                courses.add(course);
                System.out.println("Added course: " + course.getName()); // Debug log
            }
            student.setCourses(courses);
        }

        // Save student first
        Student savedStudent = studentRepository.save(student);
        System.out.println("Student saved with ID: " + savedStudent.getId()); // Debug log

        // Flush to ensure data is written to database
        studentRepository.flush();

        // Clear the persistence context and reload to ensure fresh data
        Student reloadedStudent = studentRepository.findById(savedStudent.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Failed to reload saved student"));

        // Force loading of courses
        if (reloadedStudent.getCourses() != null) {
            reloadedStudent.getCourses().size();
            System.out.println("Reloaded student courses count: " + reloadedStudent.getCourses().size()); // Debug log
        }

        return reloadedStudent;
    }

    @Override
    @Transactional
    public Student updateStudent(Long id, Student studentDetails) {
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        // Update basic fields
        existingStudent.setName(studentDetails.getName());
        existingStudent.setEmail(studentDetails.getEmail());
        existingStudent.setPhone(studentDetails.getPhone());
        existingStudent.setEnrollmentDate(studentDetails.getEnrollmentDate());

        // Clear existing courses and add new ones
        if (existingStudent.getCourses() != null) {
            existingStudent.getCourses().clear();
        } else {
            existingStudent.setCourses(new ArrayList<>());
        }

        if (studentDetails.getCourses() != null && !studentDetails.getCourses().isEmpty()) {
            existingStudent.getCourses().addAll(studentDetails.getCourses());
        }

        Student savedStudent = studentRepository.save(existingStudent);
        studentRepository.flush();

        // Reload to ensure fresh data
        Student reloadedStudent = studentRepository.findById(savedStudent.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Failed to reload updated student"));

        // Force loading of courses
        if (reloadedStudent.getCourses() != null) {
            reloadedStudent.getCourses().size();
        }

        return reloadedStudent;
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        studentRepository.delete(student);
    }
}