package llc.devlab.app.integration;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import llc.devlab.app.model.dto.auth.LoginRequest;
import llc.devlab.app.model.dto.auth.RefreshTokenRequest;
import llc.devlab.app.model.dto.auth.RegisterRequest;
import llc.devlab.app.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

class AuthControllerIntegrationTest extends IntegrationTestSupport {

    @Test
    void registerReturnsTokensAndAccount() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(new RegisterRequest("reader@example.com", "Password123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", not(nullValue())))
                .andExpect(jsonPath("$.refreshToken", not(nullValue())))
                .andExpect(jsonPath("$.account.email", equalTo("reader@example.com")))
                .andExpect(jsonPath("$.account.role", equalTo("USER")));
    }

    @Test
    void loginRefreshAndLogoutRotateAndRevokeRefreshTokens() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(new RegisterRequest("reader@example.com", "Password123"))))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(new LoginRequest("reader@example.com", "Password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken", not(nullValue())))
                .andReturn();

        String refreshToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("refreshToken")
                .asText();

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(new RefreshTokenRequest(refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken", not(equalTo(refreshToken))))
                .andReturn();

        String rotatedRefreshToken = objectMapper.readTree(refreshResult.getResponse().getContentAsString())
                .get("refreshToken")
                .asText();

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(new RefreshTokenRequest(rotatedRefreshToken))))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(new RefreshTokenRequest(rotatedRefreshToken))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("Refresh token has been revoked.")));
    }
}
