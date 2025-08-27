package com.example.LMS.serviceImpl;

import com.example.LMS.model.Expense;
import com.example.LMS.repository.ExpenseRepository;
import com.example.LMS.service.ExpenseService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExpenseServiceImpl implements ExpenseService {
    
    private final ExpenseRepository expenseRepository;
    
    public ExpenseServiceImpl(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }
    
    @Override
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }
    
    @Override
    public Expense addExpense(Expense expense) {
        // Auto-fill dates if not provided
        if (expense.getExpenseDate() == null || expense.getExpenseDate().isEmpty()) {
            expense.setExpenseDate(LocalDate.now().toString());
        }
        
        if (expense.getExpenseMonth() == null || expense.getExpenseMonth().isEmpty()) {
            LocalDate date = LocalDate.parse(expense.getExpenseDate());
            expense.setExpenseMonth(date.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        }
        
        return expenseRepository.save(expense);
    }
    
    @Override
    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }
    
    @Override
    public List<Expense> getExpensesByMonth(String month) {
        return expenseRepository.findExpensesByMonth(month);
    }
    
    @Override
    public Double getTotalExpensesByMonth(String month) {
        return expenseRepository.calculateTotalExpensesByMonth(month);
    }
}