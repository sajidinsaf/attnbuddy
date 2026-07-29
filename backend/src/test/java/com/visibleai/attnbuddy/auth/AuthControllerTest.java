package com.visibleai.attnbuddy.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visibleai.attnbuddy.auth.dto.LoginRequest;
import com.visibleai.attnbuddy.auth.dto.RegisterRequest;
import com.visibleai.attnbuddy.model.User;
import com.visibleai.attnbuddy.repository.LifeDomainRepository;
import com.visibleai.attnbuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private LifeDomainRepository domainRepository;

    @BeforeEach
    void setUp() {
        domainRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void register_createsUserAndReturnsTokens() throws Exception {
        var request = new RegisterRequest("test@example.com", "password123", "Test User", User.Profile.EXECUTIVE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    void register_executive_seedsFiveDomains() throws Exception {
        var request = new RegisterRequest("exec@example.com", "password123", "Boss", User.Profile.EXECUTIVE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        var user = userRepository.findByEmail("exec@example.com").orElseThrow();
        var domains = domainRepository.findByUserIdOrderByPositionAsc(user.getId());
        assertEquals(5, domains.size());
        assertEquals("Work", domains.get(0).getName());
        assertEquals("Family", domains.get(1).getName());
        assertEquals("Personal", domains.get(2).getName());
        assertEquals("Financial", domains.get(3).getName());
        assertEquals("Health", domains.get(4).getName());
    }

    @Test
    void register_student_seedsThreeDomains() throws Exception {
        var request = new RegisterRequest("student@example.com", "password123", "Student", User.Profile.STUDENT);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        var user = userRepository.findByEmail("student@example.com").orElseThrow();
        var domains = domainRepository.findByUserIdOrderByPositionAsc(user.getId());
        assertEquals(3, domains.size());
        assertEquals("Study", domains.get(0).getName());
    }

    @Test
    void register_duplicateEmail_returns400() throws Exception {
        var request = new RegisterRequest("dup@example.com", "password123", "User", User.Profile.PROFESSIONAL);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withValidCredentials_returnsTokens() throws Exception {
        var registerReq = new RegisterRequest("login@example.com", "password123", "User", User.Profile.PROFESSIONAL);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        var loginReq = new LoginRequest("login@example.com", "password123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void login_withBadPassword_returns401() throws Exception {
        var registerReq = new RegisterRequest("bad@example.com", "password123", "User", User.Profile.PROFESSIONAL);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        var loginReq = new LoginRequest("bad@example.com", "wrongpassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_withValidToken_returnsNewAccessToken() throws Exception {
        var registerReq = new RegisterRequest("refresh@example.com", "password123", "User", User.Profile.PROFESSIONAL);
        var result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        var authResponse = objectMapper.readTree(result.getResponse().getContentAsString());
        String refreshToken = authResponse.get("refreshToken").asText();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }
}
