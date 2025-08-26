package com.example.LMS.repository;

import com.example.LMS.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("SELECT DISTINCT s FROM Student s JOIN s.courses c WHERE c.id = :courseId")
    List<Student> findStudentsByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.courses WHERE s.id = :id")
    Optional<Student> findByIdWithCourses(@Param("id") Long id);

    @Query("SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.courses")
    List<Student> findAllWithCourses();

    boolean existsByEmail(String email);

    Optional<Student> findByEmail(String email);

    @Query("SELECT s FROM Student s WHERE :courseId MEMBER OF (SELECT c.id FROM s.courses c)")
    List<Student> findStudentsEnrolledInCourse(@Param("courseId") Long courseId);
}