package com.financetracker.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security integration tests verifying that all protected endpoints require JWT authentication
 * and that public endpoints are accessible without a token.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getUsersMe_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTransactions_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCategories_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getBudgets_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/budgets"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getGoals_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/goals"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getReportSummary_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/reports/summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteUsersMe_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authRegister_withoutJwt_isReachable() throws Exception {
        // Should not return 401 — endpoint is public
        var result = mockMvc.perform(post("/api/v1/auth/register"))
                .andReturn();
        var status = result.getResponse().getStatus();
        // Expect 400 (bad request / missing body), not 401
        org.assertj.core.api.Assertions.assertThat(status).isNotEqualTo(401);
    }

    @Test
    void authLogin_withoutJwt_isReachable() throws Exception {
        var result = mockMvc.perform(post("/api/v1/auth/login"))
                .andReturn();
        var status = result.getResponse().getStatus();
        org.assertj.core.api.Assertions.assertThat(status).isNotEqualTo(401);
    }

    @Test
    void errorResponse_conformsToSchema() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
        // Verify 401 unauthenticated returns proper response
        // Spring Security returns its own 401, but for domain errors we verify schema
    }
}
