package com.example.LMS.repository;

import com.example.LMS.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Find expenses by month
    @Query("SELECT e FROM Expense e WHERE e.expenseMonth = :month")
    List<Expense> findExpensesByMonth(@Param("month") String month);

    // Calculate total expenses for a month
    @Query("SELECT COALESCE(SUM(e.amount), 0.0) FROM Expense e WHERE e.expenseMonth = :month")
    Double calculateTotalExpensesByMonth(@Param("month") String month);

    // NEW: Find expenses by category and month
    @Query("SELECT e FROM Expense e WHERE e.expenseMonth = :month AND e.category = :category")
    List<Expense> findExpensesByMonthAndCategory(@Param("month") String month,
                                                 @Param("category") Expense.ExpenseCategory category);

    // NEW: Calculate total expenses by category for a month
    @Query("SELECT COALESCE(SUM(e.amount), 0.0) FROM Expense e WHERE e.expenseMonth = :month AND e.category = :category")
    Double calculateTotalExpensesByMonthAndCategory(@Param("month") String month,
                                                    @Param("category") Expense.ExpenseCategory category);

    // NEW: Find salary payments for a specific teacher and month
    @Query("SELECT e FROM Expense e WHERE e.expenseMonth = :month AND e.category = 'SALARY' AND e.teacherId = :teacherId")
    List<Expense> findSalaryPaymentsByTeacherAndMonth(@Param("teacherId") Long teacherId, @Param("month") String month);

    // NEW: Check if teacher was paid in a specific month
    @Query("SELECT COUNT(e) > 0 FROM Expense e WHERE e.expenseMonth = :month AND e.category = 'SALARY' AND e.teacherId = :teacherId")
    boolean isTeacherPaidInMonth(@Param("teacherId") Long teacherId, @Param("month") String month);

    // NEW: Get expense breakdown by category for a month
    @Query("SELECT e.category, COALESCE(SUM(e.amount), 0.0) FROM Expense e WHERE e.expenseMonth = :month GROUP BY e.category")
    List<Object[]> getExpenseBreakdownByMonth(@Param("month") String month);

    // NEW: Find unpaid teachers for a month (teachers without salary expense records)
    @Query("SELECT t FROM Teacher t WHERE t.id NOT IN " +
            "(SELECT DISTINCT e.teacherId FROM Expense e WHERE e.expenseMonth = :month AND e.category = 'SALARY' AND e.teacherId IS NOT NULL)")
    List<com.example.LMS.model.Teacher> findUnpaidTeachersInMonth(@Param("month") String month);
}