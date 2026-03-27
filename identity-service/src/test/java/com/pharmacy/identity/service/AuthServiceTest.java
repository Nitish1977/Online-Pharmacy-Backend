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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private User mockUser;
    private UserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Rahul Kumar");
        mockUser.setEmail("rahul@example.com");
        mockUser.setMobile("9876543210");
        mockUser.setPassword("encodedPassword");
        mockUser.setRole(Role.CUSTOMER);
        mockUser.setStatus(UserStatus.ACTIVE);

        mockUserDetails = new org.springframework.security.core.userdetails.User(
                "rahul@example.com", "encodedPassword",
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
    }

    // ── Signup Tests ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-01: Signup with valid data should return token")
    void signup_ValidData_ReturnsAuthResponse() {
        SignupRequest request = new SignupRequest();
        request.setName("Rahul Kumar");
        request.setEmail("rahul@example.com");
        request.setMobile("9876543210");
        request.setPassword("password123");

        when(userRepository.existsByEmail("rahul@example.com")).thenReturn(false);
        when(userRepository.existsByMobile("9876543210")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(userDetailsService.loadUserByUsername("rahul@example.com")).thenReturn(mockUserDetails);
        when(jwtUtil.generateToken(mockUserDetails, "CUSTOMER")).thenReturn("jwt-token-123");

        AuthResponse response = authService.signup(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token-123");
        assertThat(response.getEmail()).isEqualTo("rahul@example.com");
        assertThat(response.getRole()).isEqualTo("CUSTOMER");
        assertThat(response.getMessage()).isEqualTo("Signup successful");

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
        verify(emailService).sendWelcomeEmail("rahul@example.com", "Rahul Kumar");
    }

    @Test
    @DisplayName("TC-02: Signup with duplicate email should throw exception")
    void signup_DuplicateEmail_ThrowsException() {
        SignupRequest request = new SignupRequest();
        request.setEmail("rahul@example.com");
        request.setMobile("9876543210");

        when(userRepository.existsByEmail("rahul@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-03: Signup with duplicate mobile should throw exception")
    void signup_DuplicateMobile_ThrowsException() {
        SignupRequest request = new SignupRequest();
        request.setEmail("new@example.com");
        request.setMobile("9876543210");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByMobile("9876543210")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Mobile number already registered");
    }

    // ── Login Tests ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-04: Login with valid credentials should return token")
    void login_ValidCredentials_ReturnsAuthResponse() {
        LoginRequest request = new LoginRequest();
        request.setEmail("rahul@example.com");
        request.setPassword("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("rahul@example.com")).thenReturn(Optional.of(mockUser));
        when(userDetailsService.loadUserByUsername("rahul@example.com")).thenReturn(mockUserDetails);
        when(jwtUtil.generateToken(mockUserDetails, "CUSTOMER")).thenReturn("jwt-token-456");

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token-456");
        assertThat(response.getEmail()).isEqualTo("rahul@example.com");
        assertThat(response.getMessage()).isEqualTo("Login successful");
    }

    @Test
    @DisplayName("TC-05: Login with wrong password should throw BadCredentialsException")
    void login_WrongPassword_ThrowsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("rahul@example.com");
        request.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("TC-06: Login with non-existent email should throw exception")
    void login_NonExistentEmail_ThrowsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("notfound@example.com");
        request.setPassword("password123");

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }
}
