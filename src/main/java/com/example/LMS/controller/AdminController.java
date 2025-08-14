package com.example.LMS.controller;


import com.example.LMS.dto.TeacherCreationDto;
import com.example.LMS.model.Role;
import com.example.LMS.model.User;
import com.example.LMS.repository.RoleRepository;
import com.example.LMS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create-teacher")
    public ResponseEntity<String> createTeacher(@RequestBody TeacherCreationDto teacherDto) {
        if (userRepository.findByUsername(teacherDto.getUsername()).isPresent()) {
            return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
        }

        User teacher = new User();
        teacher.setUsername(teacherDto.getUsername());
        teacher.setPassword(passwordEncoder.encode(teacherDto.getPassword()));

        Role teacherRole = roleRepository.findByName("TEACHER")
                .orElseThrow(() -> new RuntimeException("Role 'TEACHER' not found!"));
        teacher.setRoles(Collections.singleton(teacherRole));

        userRepository.save(teacher);
        return new ResponseEntity<>("Teacher created successfully!", HttpStatus.CREATED);
    }
}
