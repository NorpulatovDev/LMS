package com.example.LMS.repository;

import com.example.LMS.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("SELECT s FROM Student s JOIN s.courses c WHERE c.id = :courseId")
    List<Student> findStudentsByCourseId(@Param("courseId") Long courseId);

    // Find student with courses loaded
    @Query("SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.courses WHERE s.id = :id")
    Optional<Student> findByIdWithCourses(@Param("id") Long id);

    // Find all students with courses loaded
    @Query("SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.courses")
    List<Student> findAllWithCourses();

    // Check if student exists by email
    boolean existsByEmail(String email);

    // Find student by email
    Optional<Student> findByEmail(String email);
}