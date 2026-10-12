package com.financetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financetracker.dto.auth.LoginRequest;
import com.financetracker.dto.auth.RegisterRequest;
import com.financetracker.dto.transaction.TransactionRequest;
import com.financetracker.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for TransactionController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;
    private Long categoryId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new RegisterRequest("txnuser@example.com", "password123", null))));

        var result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new LoginRequest("txnuser@example.com", "password123"))))
                .andReturn();
        var body = objectMapper.readTree(result.getResponse().getContentAsString());
        jwtToken = body.get("token").asText();

        var cats = mockMvc.perform(get("/api/v1/categories")
                        .header("Authorization", "Bearer " + jwtToken))
                .andReturn();
        var catsBody = objectMapper.readTree(cats.getResponse().getContentAsString());
        categoryId = catsBody.get(0).get("id").asLong();
    }

    private TransactionRequest validRequest() {
        return new TransactionRequest(new BigDecimal("100.00"), TransactionType.EXPENSE,
                categoryId, LocalDate.now(), "Test expense");
    }

    @Test
    void createTransaction_withValidBody_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(100.0));
    }

    @Test
    void createTransaction_withMissingAmount_returns400WithFieldErrors() throws Exception {
        var request = """
                {"type":"EXPENSE","categoryId":%d,"date":"%s"}""".formatted(categoryId, LocalDate.now());
        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void getTransactions_returns200WithPaginationDefaults() throws Exception {
        mockMvc.perform(get("/api/v1/transactions")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable").exists());
    }

    @Test
    void getTransactions_withFilterTypeExpense_returnsOnlyExpenses() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())));

        mockMvc.perform(get("/api/v1/transactions?type=EXPENSE")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    void getTransactions_withoutJwt_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTransaction_forOwnTransaction_returns200() throws Exception {
        var created = mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andReturn();
        var id = objectMapper.readTree(
                created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/v1/transactions/" + id)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void updateTransaction_withValidBody_returns200() throws Exception {
        var created = mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andReturn();
        var id = objectMapper.readTree(
                created.getResponse().getContentAsString()).get("id").asLong();

        var updated = new TransactionRequest(new BigDecimal("200.00"), TransactionType.INCOME,
                categoryId, LocalDate.now(), "Updated");
        mockMvc.perform(put("/api/v1/transactions/" + id)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(200.0));
    }

    @Test
    void deleteTransaction_returns204() throws Exception {
        var created = mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andReturn();
        var id = objectMapper.readTree(
                created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/v1/transactions/" + id)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }
}
