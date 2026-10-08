package com.badminton.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "RUN_MYSQL_TESTS", matches = "true")
class AuthMySqlIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;

  @Test
  void registerLoginProfileAddressAndPasswordRoundTrip() throws Exception {
    String email = "it-" + UUID.randomUUID() + "@example.com";
    String register =
        json.writeValueAsString(
            Map.of(
                "fullName",
                "Integration User",
                "email",
                email,
                "password",
                "Password123!",
                "confirmPassword",
                "Password123!"));
    mvc.perform(post("/api/auth/register").contentType("application/json").content(register))
        .andExpect(status().isCreated());
    mvc.perform(post("/api/auth/register").contentType("application/json").content(register))
        .andExpect(status().isConflict());
    String login =
        mvc.perform(
                post("/api/auth/login")
                    .contentType("application/json")
                    .content(
                        json.writeValueAsString(
                            Map.of("email", email, "password", "Password123!"))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String bearer = "Bearer " + json.readTree(login).path("data").path("accessToken").asText();
    mvc.perform(get("/api/users/me").header("Authorization", bearer))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.email").value(email))
        .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    String payload =
        json.writeValueAsString(
            Map.of(
                "receiverName",
                "User",
                "receiverPhone",
                "0901234567",
                "province",
                "HN",
                "district",
                "HD",
                "ward",
                "ML",
                "detailAddress",
                "123 ABC",
                "isDefault",
                true));
    String response =
        mvc.perform(
                post("/api/addresses")
                    .header("Authorization", bearer)
                    .contentType("application/json")
                    .content(payload))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    long id = json.readTree(response).path("data").path("addressId").asLong();
    mvc.perform(get("/api/addresses").header("Authorization", bearer))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].isDefault").value(true));
    mvc.perform(
            put("/api/addresses/" + id)
                .header("Authorization", bearer)
                .contentType("application/json")
                .content(payload))
        .andExpect(status().isOk());
    mvc.perform(delete("/api/addresses/" + id).header("Authorization", bearer))
        .andExpect(status().isOk());
    mvc.perform(
            put("/api/users/change-password")
                .header("Authorization", bearer)
                .contentType("application/json")
                .content(
                    """
{"currentPassword":"Password123!","newPassword":"NextPassword123!","confirmPassword":"NextPassword123!"}
"""))
        .andExpect(status().isOk());
    mvc.perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content(
                    json.writeValueAsString(
                        Map.of("email", email, "password", "NextPassword123!"))))
        .andExpect(status().isOk());
  }
}
