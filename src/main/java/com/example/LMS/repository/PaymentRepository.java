package com.example.LMS.repository;

import com.example.LMS.model.Payment;
import com.example.LMS.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// src/main/java/com/example/LMS/repository/PaymentRepository.java
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Get payments for this month
    @Query("SELECT p FROM Payment p WHERE p.paymentMonth = :month")
    List<Payment> findPaymentsByMonth(@Param("month") String month);

    // Get payments for teacher's courses
    @Query("SELECT p FROM Payment p WHERE p.courseId IN " +
            "(SELECT c.id FROM Course c JOIN c.teachers t WHERE t.id = :teacherId)")
    List<Payment> findPaymentsByTeacherId(@Param("teacherId") Long teacherId);

    // Find students who haven't paid this month for a course
    @Query("SELECT s FROM Student s JOIN s.courses c WHERE c.id = :courseId " +
            "AND s.id NOT IN (SELECT p.studentId FROM Payment p WHERE p.courseId = :courseId AND p.paymentMonth = :month)")
    List<Student> findStudentsWithoutPayment(@Param("courseId") Long courseId, @Param("month") String month);
}
