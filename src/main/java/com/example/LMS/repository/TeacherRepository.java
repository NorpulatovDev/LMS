package com.example.LMS.repository;

import com.example.LMS.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

// src/main/java/com/example/LMS/repository/TeacherRepository.java
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    // Find teacher by username for getting their profile
    @Query("SELECT t FROM Teacher t JOIN t.user u WHERE u.username = :username")
    Optional<Teacher> findByUsername(@Param("username") String username);


    // FIXED: Check if teacher teaches a specific course
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
            "FROM Teacher t JOIN t.courses c " +
            "WHERE t.id = :teacherId AND c.id = :courseId")
    boolean teacherHasCourse(@Param("teacherId") Long teacherId, @Param("courseId") Long courseId);
}
