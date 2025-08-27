package com.example.LMS.service;

import com.example.LMS.model.Expense;
import java.util.List;

public interface ExpenseService {
    
    List<Expense> getAllExpenses();
    
    Expense addExpense(Expense expense);
    
    void deleteExpense(Long id);
    
    List<Expense> getExpensesByMonth(String month);
    
    Double getTotalExpensesByMonth(String month);
}