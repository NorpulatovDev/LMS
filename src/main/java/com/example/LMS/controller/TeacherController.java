package com.example.LMS.controller;

import com.example.LMS.model.Teacher;
import com.example.LMS.service.TeacherService; // TeacherService ni o'zgartirishlar uchun
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // Autentifikatsiya ma'lumotlarini olish uchun
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails; // Foydalanuvchi ma'lumotlari uchun
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/api/teachers") // API manzilini /api/teachers ga o'zgartiramiz
public class TeacherController {

    @Autowired
    private TeacherService teacherService; // Autowired orqali bog'laymiz

    /**
     * Barcha o'qituvchilar ro'yxatini qaytaradi.
     * Faqat ADMIN yoki TEACHER roli ega bo'lgan foydalanuvchilar kira oladi.
     * @return Barcha o'qituvchilar ro'yxati
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping
    public ResponseEntity<List<Teacher>> getAllTeachers() {
        List<Teacher> teachers = teacherService.getAllTeachers();
        return ResponseEntity.ok(teachers);
    }

    /**
     * Berilgan ID bo'yicha o'qituvchi ma'lumotlarini qaytaradi.
     * TEACHER o'zining profilini, ADMIN esa har qanday o'qituvchining profilini ko'ra oladi.
     * @param id O'qituvchining IDsi
     * @return O'qituvchi ob'ekti
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/{id}")
    public ResponseEntity<Teacher> getTeacherById(@PathVariable Long id) {
        // Hozirgi autentifikatsiya qilingan foydalanuvchi ma'lumotlarini olamiz
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // Autentifikatsiya qilinmagan
        }

        UserDetails currentUser = (UserDetails) authentication.getPrincipal();
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Teacher teacher = teacherService.getTeacherById(id);


        // Agar foydalanuvchi ADMIN bo'lmasa, faqat o'z profilini ko'rishiga ruxsat beramiz
        if (!isAdmin && !teacher.getUser().getUsername().equals(currentUser.getUsername())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // Ruxsat yo'q
        }

        return ResponseEntity.ok(teacher);
    }

    // Add, Update, Delete metodlari AdminControllerga ko'chirildi
    // @PostMapping
    // @PutMapping
    // @DeleteMapping
}
