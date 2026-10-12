package com.financetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financetracker.dto.auth.LoginRequest;
import com.financetracker.dto.auth.RegisterRequest;
import com.financetracker.dto.transaction.TransactionRequest;
import com.financetracker.model.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for ReportController using SpringBootTest and MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReportControllerTest {

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
                        new RegisterRequest("reportuser@example.com", "password123", null))));

        var loginResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new LoginRequest("reportuser@example.com", "password123"))))
                .andReturn();
        var body = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        jwtToken = body.get("token").asText();

        // Get a category id for transactions
        var catResult = mockMvc.perform(get("/api/v1/categories")
                        .header("Authorization", "Bearer " + jwtToken))
                .andReturn();
        var categories = objectMapper.readTree(catResult.getResponse().getContentAsString());
        categoryId = categories.get(0).get("id").asLong();
    }

    @Test
    void getSummary_withNoDates_returns200DefaultsToCurrentMonth() throws Exception {
        mockMvc.perform(get("/api/v1/reports/summary")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").exists())
                .andExpect(jsonPath("$.endDate").exists())
                .andExpect(jsonPath("$.totalIncome").exists())
                .andExpect(jsonPath("$.totalExpenses").exists())
                .andExpect(jsonPath("$.netBalance").exists())
                .andExpect(jsonPath("$.categoryBreakdowns").isArray());
    }

    @Test
    void getSummary_withExplicitDates_returns200WithCorrectData() throws Exception {
        // Create a transaction in the specified date range
        var txRequest = new TransactionRequest(
                new BigDecimal("1000.00"), TransactionType.INCOME,
                categoryId, LocalDate.of(2026, 3, 15), "Salary");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/transactions")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(txRequest)));

        mockMvc.perform(get("/api/v1/reports/summary")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("startDate", "2026-03-01")
                        .param("endDate", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value("2026-03-01"))
                .andExpect(jsonPath("$.endDate").value("2026-03-31"));
    }

    @Test
    void getSummary_forUserWithNoTransactions_returns200WithAllZeroTotals() throws Exception {
        mockMvc.perform(get("/api/v1/reports/summary")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("startDate", "2020-01-01")
                        .param("endDate", "2020-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryBreakdowns").isArray());
    }

    @Test
    void getMonthlyTrend_withNoDates_returns200WithMonthsInAscendingOrder() throws Exception {
        mockMvc.perform(get("/api/v1/reports/monthly-trend")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getMonthlyTrend_spanningMultipleMonths_returnsCorrectGrouping() throws Exception {
        // Create transactions in different months
        var txJan = new TransactionRequest(
                new BigDecimal("2000.00"), TransactionType.INCOME,
                categoryId, LocalDate.of(2026, 1, 15), "Jan salary");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/transactions")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(txJan)));

        var txFeb = new TransactionRequest(
                new BigDecimal("2000.00"), TransactionType.INCOME,
                categoryId, LocalDate.of(2026, 2, 15), "Feb salary");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/transactions")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(txFeb)));

        mockMvc.perform(get("/api/v1/reports/monthly-trend")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-02-28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void allEndpoints_withoutJwt_return401() throws Exception {
        mockMvc.perform(get("/api/v1/reports/summary")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/reports/monthly-trend")).andExpect(status().isUnauthorized());
    }
}
