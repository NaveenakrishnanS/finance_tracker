package com.financetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financetracker.dto.auth.LoginRequest;
import com.financetracker.dto.auth.RegisterRequest;
import com.financetracker.dto.goal.ContributionRequest;
import com.financetracker.dto.goal.GoalRequest;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for GoalController using SpringBootTest and MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new RegisterRequest("goaluser@example.com", "password123", null))));

        var loginResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new LoginRequest("goaluser@example.com", "password123"))))
                .andReturn();
        var body = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        jwtToken = body.get("token").asText();
    }

    private GoalRequest validGoalRequest() {
        return new GoalRequest("Emergency Fund", new BigDecimal("10000.00"),
                LocalDate.of(2027, 1, 1));
    }

    @Test
    void createGoal_withValidBody_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/goals")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validGoalRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Emergency Fund"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void createGoal_withMissingName_returns400() throws Exception {
        var invalidRequest = new GoalRequest("", new BigDecimal("10000.00"),
                LocalDate.of(2027, 1, 1));
        mockMvc.perform(post("/api/v1/goals")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void getGoals_returns200WithProgressPercentageAndRemainingAmount() throws Exception {
        mockMvc.perform(post("/api/v1/goals")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validGoalRequest())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/goals")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].progressPercentage").exists())
                .andExpect(jsonPath("$[0].remainingAmount").exists());
    }

    @Test
    void getGoalById_returns200() throws Exception {
        var created = mockMvc.perform(post("/api/v1/goals")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validGoalRequest())))
                .andExpect(status().isCreated())
                .andReturn();
        var id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/v1/goals/" + id)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void updateGoal_returns200() throws Exception {
        var created = mockMvc.perform(post("/api/v1/goals")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validGoalRequest())))
                .andReturn();
        var id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        var updateRequest = new GoalRequest("Updated Fund", new BigDecimal("20000.00"),
                LocalDate.of(2028, 1, 1));
        mockMvc.perform(put("/api/v1/goals/" + id)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Fund"));
    }

    @Test
    void deleteGoal_returns204() throws Exception {
        var created = mockMvc.perform(post("/api/v1/goals")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validGoalRequest())))
                .andReturn();
        var id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/v1/goals/" + id)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void addContribution_withPositiveAmount_returns200WithIncreasedContributedAmount() throws Exception {
        var created = mockMvc.perform(post("/api/v1/goals")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validGoalRequest())))
                .andReturn();
        var id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/v1/goals/" + id + "/contributions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ContributionRequest(new BigDecimal("500.00"), "Monthly savings"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contributedAmount").value(
                        org.hamcrest.Matchers.greaterThan(0.0)));
    }

    @Test
    void addContribution_withNegativeAmount_returns400() throws Exception {
        var created = mockMvc.perform(post("/api/v1/goals")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validGoalRequest())))
                .andReturn();
        var id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/v1/goals/" + id + "/contributions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ContributionRequest(new BigDecimal("-100.00"), null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addContribution_thatMeetsTarget_returns200WithStatusAchieved() throws Exception {
        // Create goal with small target
        var smallGoalRequest = new GoalRequest("Small Goal", new BigDecimal("100.00"),
                LocalDate.of(2027, 1, 1));
        var created = mockMvc.perform(post("/api/v1/goals")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(smallGoalRequest)))
                .andReturn();
        var id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/v1/goals/" + id + "/contributions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ContributionRequest(new BigDecimal("100.00"), null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACHIEVED"));
    }

    @Test
    void allEndpoints_withoutJwt_return401() throws Exception {
        mockMvc.perform(get("/api/v1/goals")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/goals").contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/goals/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/v1/goals/1").contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/v1/goals/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/goals/1/contributions").contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isUnauthorized());
    }
}
