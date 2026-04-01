package com.pharmacy.identity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.identity.dto.AuthResponse;
import com.pharmacy.identity.dto.LoginRequest;
import com.pharmacy.identity.dto.SignupRequest;
import com.pharmacy.identity.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // ✅ FIX: Disable Spring Security
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("TC-07: POST /api/auth/signup with valid body returns 200")
    void signup_ValidRequest_Returns200() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setName("Rahul Kumar");
        request.setEmail("rahul@example.com");
        request.setMobile("9876543210");
        request.setPassword("password123");

        AuthResponse mockResponse = new AuthResponse(
                "jwt-token-123", "rahul@example.com",
                "Rahul Kumar", "CUSTOMER", "Signup successful");

        when(authService.signup(any(SignupRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.email").value("rahul@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.message").value("Signup successful"));
    }

    @Test
    @DisplayName("TC-08: Signup with blank name returns 400")
    void signup_BlankName_Returns400() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setName(""); // invalid
        request.setEmail("rahul@example.com");
        request.setMobile("9876543210");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-10: Login with valid credentials returns 200")
    void login_ValidRequest_Returns200() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("rahul@example.com");
        request.setPassword("password123");

        AuthResponse mockResponse = new AuthResponse(
                "jwt-token-456", "rahul@example.com",
                "Rahul Kumar", "CUSTOMER", "Login successful");

        when(authService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-456"))
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    @DisplayName("TC-11: Login with empty password returns 400")
    void login_ShortPassword_Returns400() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("rahul@example.com");
        request.setPassword(""); // invalid

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}