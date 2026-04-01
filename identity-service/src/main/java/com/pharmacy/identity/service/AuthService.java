package com.pharmacy.identity.service;

import com.pharmacy.identity.dto.AuthResponse;
import com.pharmacy.identity.dto.LoginRequest;
import com.pharmacy.identity.dto.SignupRequest;
import com.pharmacy.identity.entity.User;
import com.pharmacy.identity.enums.Role;
import com.pharmacy.identity.enums.UserStatus;
import com.pharmacy.identity.repository.UserRepository;
import com.pharmacy.identity.security.CustomUserDetailsService;
import com.pharmacy.identity.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    public AuthService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            AuthenticationManager authenticationManager,
            CustomUserDetailsService userDetailsService,
            org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.rabbitTemplate = rabbitTemplate;
    }

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new IllegalArgumentException("Email already registered");
        if (userRepository.existsByMobile(request.getMobile()))
            throw new IllegalArgumentException("Mobile number already registered");

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setMobile(request.getMobile());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        // Send welcome email via RabbitMQ
        com.pharmacy.identity.dto.NotificationEvent notification = new com.pharmacy.identity.dto.NotificationEvent();
        notification.setType(com.pharmacy.identity.dto.NotificationType.WELCOME);
        notification.setRecipientEmail(user.getEmail());
        notification.setPayload(java.util.Map.of("name", user.getName()));
        rabbitTemplate.convertAndSend(com.pharmacy.identity.config.RabbitMQConfig.EXCHANGE,
                com.pharmacy.identity.config.RabbitMQConfig.ROUTING_KEY_NOTIFICATION, notification);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails, user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getName(), user.getRole().name(), "Signup successful");
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails, user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getName(), user.getRole().name(), "Login successful");
    }
}
