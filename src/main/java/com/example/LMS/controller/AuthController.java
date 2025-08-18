package com.example.LMS.controller;


import com.example.LMS.dto.JwtAuthResponse;
import com.example.LMS.dto.LoginDto;
import com.example.LMS.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> authenticateUser(@RequestBody LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate both tokens
        String accessToken = tokenProvider.generateAccessToken(authentication);  // CHANGED
        String refreshToken = tokenProvider.generateRefreshToken(authentication); // NEW

        return ResponseEntity.ok(new JwtAuthResponse(accessToken, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthResponse> refreshToken(@RequestBody String refreshToken) {
        if (tokenProvider.validateToken(refreshToken)) {
            String username = tokenProvider.getUsernameFromJWT(refreshToken);

            // Create new access token
            String newAccessToken = tokenProvider.generateAccessTokenFromUsername(username);

            return ResponseEntity.ok(new JwtAuthResponse(newAccessToken, refreshToken));
        }
        return ResponseEntity.badRequest().build();
    }
}