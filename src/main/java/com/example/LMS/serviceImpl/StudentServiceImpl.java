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

        // FIXED: Handle courses properly with bidirectional relationship
        if (request.getCourseIds() != null && !request.getCourseIds().isEmpty()) {
            System.out.println("Processing course IDs: " + request.getCourseIds());

            // First save the student without courses to get the ID
            Student savedStudent = studentRepository.save(student);

            // Then add courses one by one
            for (Long courseId : request.getCourseIds()) {
                Course course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

                // Use the helper method to maintain bidirectional relationship
                savedStudent.addCourse(course);
                System.out.println("Added course: " + course.getName());
            }

            // Save the student again with courses
            savedStudent = studentRepository.save(savedStudent);
            System.out.println("Final save - Student courses count: " + savedStudent.getCourses().size());

            return savedStudent;
        } else {
            // No courses, just save the student
            return studentRepository.save(student);
        }
    }

    @Override
    @Transactional
    public Student updateStudent(Long id, Student studentDetails) {
        Student existingStudent = getStudentById(id);

        existingStudent.setName(studentDetails.getName());
        existingStudent.setEmail(studentDetails.getEmail());
        existingStudent.setPhone(studentDetails.getPhone());
        existingStudent.setEnrollmentDate(studentDetails.getEnrollmentDate());

        // FIXED: Handle course updates properly
        if (studentDetails.getCourses() != null) {
            // Clear existing relationships first
            existingStudent.getCourses().forEach(course ->
                    course.getStudents().remove(existingStudent));
            existingStudent.getCourses().clear();

            // Add new relationships
            for (Course course : studentDetails.getCourses()) {
                existingStudent.addCourse(course);
            }
        }

        return studentRepository.save(existingStudent);
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        Student student = getStudentById(id);

        // FIXED: Clean up relationships before deletion
        student.getCourses().forEach(course -> course.getStudents().remove(student));
        student.getCourses().clear();

        studentRepository.delete(student);
    }

    // Additional method to add student to course
    @Transactional
    public Student addStudentToCourse(Long studentId, Long courseId) {
        Student student = getStudentById(studentId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        // Use helper method to maintain bidirectional relationship
        student.addCourse(course);

        return studentRepository.save(student);
    }

    // Additional method to remove student from course
    @Transactional
    public Student removeStudentFromCourse(Long studentId, Long courseId) {
        Student student = getStudentById(studentId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        // Use helper method to maintain bidirectional relationship
        student.removeCourse(course);

        return studentRepository.save(student);
    }
}