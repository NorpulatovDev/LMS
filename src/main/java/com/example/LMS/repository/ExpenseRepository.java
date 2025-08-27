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
}