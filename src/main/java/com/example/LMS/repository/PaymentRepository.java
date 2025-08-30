package com.example.LMS.repository;

import com.example.LMS.model.Payment;
import com.example.LMS.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // FIXED: Get payments for this month with fallback logic for null paymentMonth
    @Query("SELECT p FROM Payment p WHERE " +
            "p.paymentMonth = :month OR " +
            "(p.paymentMonth IS NULL AND SUBSTRING(p.paymentDate, 1, 7) = :month) OR " +
            "(p.paymentMonth IS NULL AND p.paymentDate LIKE CONCAT(:month, '-%'))")
    List<Payment> findPaymentsByMonth(@Param("month") String month);

    // FIXED: Get payments for teacher's courses with better join
    @Query("SELECT p FROM Payment p WHERE p.courseId IN " +
            "(SELECT c.id FROM Course c JOIN c.teachers t WHERE t.id = :teacherId)")
    List<Payment> findPaymentsByTeacherId(@Param("teacherId") Long teacherId);

    // FIXED: Find students who haven't paid this month for a course with proper null handling
    @Query("SELECT s FROM Student s JOIN s.courses c WHERE c.id = :courseId " +
            "AND s.id NOT IN (" +
            "    SELECT COALESCE(p.studentId, -1) FROM Payment p WHERE p.courseId = :courseId " +
            "    AND (p.paymentMonth = :month OR " +
            "         (p.paymentMonth IS NULL AND SUBSTRING(p.paymentDate, 1, 7) = :month) OR " +
            "         (p.paymentMonth IS NULL AND p.paymentDate LIKE CONCAT(:month, '-%')))" +
            ")")
    List<Student> findStudentsWithoutPayment(@Param("courseId") Long courseId, @Param("month") String month);

    // NEW: Check if payment already exists for student/course/month
    @Query("SELECT COUNT(p) > 0 FROM Payment p WHERE p.studentId = :studentId AND p.courseId = :courseId " +
            "AND (p.paymentMonth = :month OR " +
            "     (p.paymentMonth IS NULL AND SUBSTRING(p.paymentDate, 1, 7) = :month) OR " +
            "     (p.paymentMonth IS NULL AND p.paymentDate LIKE CONCAT(:month, '-%')))")
    boolean existsByStudentAndCourseAndMonth(@Param("studentId") Long studentId,
                                             @Param("courseId") Long courseId,
                                             @Param("month") String month);

    // NEW: Get all payments with missing paymentMonth for data migration
    @Query("SELECT p FROM Payment p WHERE p.paymentMonth IS NULL OR p.paymentMonth = ''")
    List<Payment> findPaymentsWithMissingMonth();

    // NEW: Count total payments for debugging
    @Query("SELECT COUNT(p) FROM Payment p")
    Long countAllPayments();

    // NEW: Debug query to see payment dates and months
    @Query("SELECT p.id, p.paymentDate, p.paymentMonth, p.amount FROM Payment p")
    List<Object[]> findAllPaymentInfo();
}