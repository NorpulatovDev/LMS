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
        // Use the method that loads courses to avoid lazy loading issues
        return studentRepository.findByIdWithCourses(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    @Override
    @Transactional
    public Student createStudent(StudentRequest request) {
        System.out.println("Creating student with request: " + request);

        try {
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

            // Initialize courses set
            student.setCourses(new HashSet<>());

            // Handle courses if provided
            if (request.getCourseIds() != null && !request.getCourseIds().isEmpty()) {
                System.out.println("Processing course IDs: " + request.getCourseIds());

                Set<Course> courses = new HashSet<>();
                for (Long courseId : request.getCourseIds()) {
                    Course course = courseRepository.findById(courseId)
                            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
                    courses.add(course);
                    System.out.println("Added course: " + course.getName());
                }
                student.setCourses(courses);
            }

            Student savedStudent = studentRepository.save(student);
            System.out.println("Student saved successfully with courses count: " + savedStudent.getCourses().size());

            return savedStudent;

        } catch (Exception e) {
            System.err.println("Error in createStudent: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to trigger rollback
        }
    }

    @Override
    @Transactional
    public Student updateStudent(Long id, Student studentDetails) {
        try {
            // Fetch the existing student with courses loaded
            Student existingStudent = studentRepository.findByIdWithCourses(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

            // Update basic fields
            existingStudent.setName(studentDetails.getName());
            existingStudent.setEmail(studentDetails.getEmail());
            existingStudent.setPhone(studentDetails.getPhone());
            existingStudent.setEnrollmentDate(studentDetails.getEnrollmentDate());

            // Handle course updates more carefully
            if (studentDetails.getCourses() != null) {
                // Clear existing courses
                existingStudent.getCourses().clear();

                // Add new courses
                Set<Course> newCourses = new HashSet<>();
                for (Course courseDetails : studentDetails.getCourses()) {
                    // Fetch the managed course entity
                    Course managedCourse = courseRepository.findById(courseDetails.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseDetails.getId()));
                    newCourses.add(managedCourse);
                }
                existingStudent.setCourses(newCourses);
            }

            // Save and return
            Student updatedStudent = studentRepository.save(existingStudent);
            System.out.println("Student updated successfully");

            return updatedStudent;

        } catch (Exception e) {
            System.err.println("Error in updateStudent: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to trigger rollback
        }
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        try {
            // Fetch the student with courses loaded
            Student student = studentRepository.findByIdWithCourses(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

            // Clear all course relationships
            student.getCourses().clear();

            // Save to update relationships, then delete
            studentRepository.save(student);
            studentRepository.delete(student);

            System.out.println("Student deleted successfully");

        } catch (Exception e) {
            System.err.println("Error in deleteStudent: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to trigger rollback
        }
    }

    // Additional helper methods
    @Transactional
    public Student addStudentToCourse(Long studentId, Long courseId) {
        try {
            Student student = getStudentById(studentId);
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

            student.getCourses().add(course);
            return studentRepository.save(student);

        } catch (Exception e) {
            System.err.println("Error in addStudentToCourse: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public Student removeStudentFromCourse(Long studentId, Long courseId) {
        try {
            Student student = getStudentById(studentId);
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

            student.getCourses().remove(course);
            return studentRepository.save(student);

        } catch (Exception e) {
            System.err.println("Error in removeStudentFromCourse: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}