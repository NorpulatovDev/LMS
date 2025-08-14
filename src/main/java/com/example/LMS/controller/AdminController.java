package com.example.LMS.controller;

import com.example.LMS.dto.TeacherCreationDto;
import com.example.LMS.model.Role;
import com.example.LMS.model.Teacher;
import com.example.LMS.model.User;
import com.example.LMS.repository.RoleRepository;
import com.example.LMS.repository.TeacherRepository;
import com.example.LMS.repository.UserRepository;
import jakarta.validation.Valid; // Validatsiya uchun
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TeacherRepository teacherRepository; // TeacherRepository ni qo'shamiz

    /**
     * Yangi o'qituvchi akkauntini yaratadi (User va Teacher profilini birga).
     * Faqat ADMIN roli ega bo'lgan foydalanuvchilar kira oladi.
     * @param teacherDto O'qituvchi yaratish uchun ma'lumotlar DTOsi
     * @return Yaratilgan o'qituvchi haqida xabar
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/teachers")
    public ResponseEntity<String> createTeacher(@Valid @RequestBody TeacherCreationDto teacherDto) {
        // Username allaqachon mavjudligini tekshiramiz
        if (userRepository.findByUsername(teacherDto.getUsername()).isPresent()) {
            return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
        }

        // 1-qadam: User entitiysini yaratish va saqlash (login ma'lumotlari)
        User newUser = new User();
        newUser.setUsername(teacherDto.getUsername());
        newUser.setPassword(passwordEncoder.encode(teacherDto.getPassword()));

        // 'TEACHER' rolini topamiz va yangi foydalanuvchiga tayinlaymiz
        Role teacherRole = roleRepository.findByName("TEACHER")
                .orElseThrow(() -> new RuntimeException("Role 'TEACHER' not found! Please ensure 'TEACHER' role exists in the database."));
        newUser.setRoles(Collections.singleton(teacherRole));

        // Foydalanuvchini bazaga saqlaymiz va saqlangan User ob'ektini olamiz (ID bilan)
        User savedUser = userRepository.save(newUser);

        // 2-qadam: Teacher entitiysini yaratish va saqlash (profil ma'lumotlari)
        Teacher newTeacher = new Teacher();
        newTeacher.setName(teacherDto.getName());
        newTeacher.setEmail(teacherDto.getEmail());
        newTeacher.setPhone(teacherDto.getPhone());
        newTeacher.setSalary(teacherDto.getSalary());

        // Teacher entitiysini User entitiysiga bog'laymiz (User IDsi Teacher IDsi bo'ladi)
        newTeacher.setUser(savedUser); // @MapsId tufayli Teacher.id = User.id bo'ladi

        teacherRepository.save(newTeacher); // Teacher profilini saqlaymiz

        return new ResponseEntity<>("Teacher '" + teacherDto.getUsername() + "' created successfully!", HttpStatus.CREATED);
    }

    /**
     * Mavjud o'qituvchi profilini yangilaydi.
     * Faqat ADMIN roli ega bo'lgan foydalanuvchilar kira oladi.
     * @param id Yangilanadigan o'qituvchining IDsi
     * @param teacherDetails Yangilangan o'qituvchi ma'lumotlari
     * @return Yangilangan o'qituvchi ob'ekti
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/teachers/{id}")
    public ResponseEntity<Teacher> updateTeacher(@PathVariable Long id, @Valid @RequestBody Teacher teacherDetails) {
        // O'qituvchini ID bo'yicha topamiz
        Optional<Teacher> optionalTeacher = teacherRepository.findById(id);

        if (optionalTeacher.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Teacher existingTeacher = optionalTeacher.get();

        // Profil ma'lumotlarini yangilaymiz
        existingTeacher.setName(teacherDetails.getName());
        existingTeacher.setEmail(teacherDetails.getEmail());
        existingTeacher.setPhone(teacherDetails.getPhone());
        existingTeacher.setSalary(teacherDetails.getSalary());

        // Agar username yoki password o'zgargan bo'lsa, User ob'ektini ham yangilash kerak bo'lishi mumkin.
        // Hozirgi DTO faqat profil ma'lumotlarini o'z ichiga oladi, shuning uchun Userni alohida DTOda yangilash kerak.
        // Yoki bu endpointni TeacherCreationDto ga o'xshash kengroq DTO bilan almashtirish mumkin.
        // Hozirda faqat Teacher profil ma'lumotlari yangilanadi.

        Teacher updatedTeacher = teacherRepository.save(existingTeacher);
        return ResponseEntity.ok(updatedTeacher);
    }

    /**
     * O'qituvchi profilini o'chiradi. Bu User akkauntini ham o'chiradi,
     * chunki bazadagi CASCADE Delete konfiguratsiyasi mavjud.
     * Faqat ADMIN roli ega bo'lgan foydalanuvchilar kira oladi.
     * @param id O'chiriladigan o'qituvchining IDsi
     * @return Muvaffaqiyatli o'chirilganini bildiruvchi javob
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/teachers/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        Optional<Teacher> optionalTeacher = teacherRepository.findById(id);

        if (optionalTeacher.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Teacher ob'ektini o'chirish User ob'ektini ham o'chiradi
        // chunki `ON DELETE CASCADE` FOREIGN KEY cheklovi mavjud.
        teacherRepository.delete(optionalTeacher.get());
        return ResponseEntity.noContent().build();
    }
}
