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
        return studentRepository.findAllWithCourses();
    }

    @Override
    @Transactional(readOnly = true)
    public Student getStudentById(Long id) {
        return studentRepository.findByIdWithCourses(id).orElseThrow(
                () -> new ResourceNotFoundException("Student not found with id: " + id)
        );
    }

    @Override
    @Transactional
    public Student createStudent(StudentRequest request) {
        System.out.println("=== Creating Student ===");
        System.out.println("Name: " + request.getName());
        System.out.println("Course IDs: " + request.getCourseIds());

        // Create new student
        Student student = new Student();
        student.setName(request.getName());
        student.setEmail(request.getEmail());
        student.setPhone(request.getPhone());

        // Set enrollment date
        if (request.getEnrollmentDate() == null || request.getEnrollmentDate().trim().isEmpty()) {
            student.setEnrollmentDate(LocalDate.now().toString());
        } else {
            student.setEnrollmentDate(request.getEnrollmentDate());
        }

        // FIRST: Save the student without courses
        Student savedStudent = studentRepository.save(student);
        System.out.println("Student saved with ID: " + savedStudent.getId());

        // SECOND: Handle courses if provided
        if (request.getCourseIds() != null && !request.getCourseIds().isEmpty()) {
            List<Course> coursesToAdd = new ArrayList<>();

            for (Long courseId : request.getCourseIds()) {
                Course course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
                coursesToAdd.add(course);
                System.out.println("Found course: " + course.getName() + " (ID: " + course.getId() + ")");
            }

            // Set courses and save again
            savedStudent.setCourses(coursesToAdd);
            savedStudent = studentRepository.save(savedStudent);

            System.out.println("Student updated with " + coursesToAdd.size() + " courses");
        }

        // Force flush to database
        studentRepository.flush();

        // Reload student with courses
        Student finalStudent = studentRepository.findByIdWithCourses(savedStudent.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Could not reload student"));

        System.out.println("Final student courses count: " +
                (finalStudent.getCourses() != null ? finalStudent.getCourses().size() : 0));

        return finalStudent;
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

        // Update courses
        if (studentDetails.getCourses() != null) {
            existingStudent.setCourses(new ArrayList<>(studentDetails.getCourses()));
        } else {
            existingStudent.setCourses(new ArrayList<>());
        }

        Student savedStudent = studentRepository.save(existingStudent);
        studentRepository.flush();

        return studentRepository.findByIdWithCourses(savedStudent.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Could not reload updated student"));
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        studentRepository.delete(student);
    }
}