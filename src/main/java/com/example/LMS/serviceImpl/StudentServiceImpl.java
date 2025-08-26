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
    @Transactional(readOnly = true)
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Student getStudentById(Long id) {
        return studentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Student not found with id: " + id)
        );
    }

    @Override
    @Transactional
    public Student createStudent(StudentRequest request) {
        System.out.println("Creating student with request: " + request);

        // Create student entity
        Student student = new Student();
        student.setName(request.getName());
        student.setEmail(request.getEmail());
        student.setPhone(request.getPhone());

        if (request.getEnrollmentDate() == null || request.getEnrollmentDate().trim().isEmpty()) {
            student.setEnrollmentDate(LocalDate.now().toString());
        } else {
            student.setEnrollmentDate(request.getEnrollmentDate());
        }

        // Handle courses
        Set<Course> courses = new HashSet<>();
        if (request.getCourseIds() != null && !request.getCourseIds().isEmpty()) {
            System.out.println("Processing course IDs: " + request.getCourseIds());

            for (Long courseId : request.getCourseIds()) {
                Course course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
                courses.add(course);
                System.out.println("Added course: " + course.getName());
            }
        }

        student.setCourses(courses);

        // Save student with courses
        Student savedStudent = studentRepository.save(student);
        System.out.println("Saved student: " + savedStudent);
        System.out.println("Student courses count: " + savedStudent.getCourses().size());

        return savedStudent;
    }

    @Override
    @Transactional
    public Student updateStudent(Long id, Student studentDetails) {
        Student existingStudent = getStudentById(id);

        existingStudent.setName(studentDetails.getName());
        existingStudent.setEmail(studentDetails.getEmail());
        existingStudent.setPhone(studentDetails.getPhone());
        existingStudent.setEnrollmentDate(studentDetails.getEnrollmentDate());

        if (studentDetails.getCourses() != null) {
            existingStudent.setCourses(new HashSet<>(studentDetails.getCourses()));
        }

        return studentRepository.save(existingStudent);
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        Student student = getStudentById(id);
        studentRepository.delete(student);
    }

    // Additional method to add student to course
    @Transactional
    public Student addStudentToCourse(Long studentId, Long courseId) {
        Student student = getStudentById(studentId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        student.getCourses().add(course);
        course.getStudents().add(student);

        return studentRepository.save(student);
    }

    // Additional method to remove student from course
    @Transactional
    public Student removeStudentFromCourse(Long studentId, Long courseId) {
        Student student = getStudentById(studentId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        student.getCourses().remove(course);
        course.getStudents().remove(student);

        return studentRepository.save(student);
    }
}