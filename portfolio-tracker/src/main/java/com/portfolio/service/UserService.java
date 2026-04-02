package com.portfolio.service;

import com.portfolio.config.SecurityConfig.JwtTokenUtil;
import com.portfolio.dto.mapper.UserMapper;
import com.portfolio.dto.request.CreateUserRequest;
import com.portfolio.dto.request.LoginRequest;
import com.portfolio.dto.response.AuthResponse;
import com.portfolio.entity.User;
import com.portfolio.entity.UserRole;
import com.portfolio.exception.AuthenticationException;
import com.portfolio.exception.BusinessException;
import com.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthResponse register(CreateUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(UserRole.USER)
                .build();

        User saved = userRepository.save(user);
        String token = jwtTokenUtil.generateToken(saved.getEmail());
        return AuthResponse.builder()
                .token(token)
                .userId(saved.getId())
                .email(saved.getEmail())
                .firstName(saved.getFirstName())
                .lastName(saved.getLastName())
                .build();
    }

    public AuthResponse authenticate(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid credentials");
        }

        String token = jwtTokenUtil.generateToken(user.getEmail());
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new com.portfolio.exception.ResourceNotFoundException("User not found"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new com.portfolio.exception.ResourceNotFoundException("User not found"));
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
