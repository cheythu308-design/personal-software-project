package com.chetan.dms.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registersLogsInAndReturnsCurrentUserUsingJwt() throws Exception {
        String email = "auth-" + UUID.randomUUID() + "@example.test";
        String password = "strong-password-123";

        String registration = """
                {
                  "email": "%s",
                  "displayName": "Test Account",
                  "password": "%s"
                }
                """.formatted(email, password);

        String registrationResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registration))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(3600))
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.role").value("USER"))
                .andReturn().getResponse().getContentAsString();

        JsonNode registered = objectMapper.readTree(registrationResponse);
        String registrationToken = registered.get("accessToken").asText();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + registrationToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.displayName").value("Test Account"));

        String login = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value(email));
    }

    @Test
    void rejectsDuplicateRegistration() throws Exception {
        String email = "duplicate-" + UUID.randomUUID() + "@example.test";
        String request = """
                {
                  "email": "%s",
                  "displayName": "Duplicate Test",
                  "password": "strong-password-123"
                }
                """.formatted(email);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Email already registered"));
    }


    @Test
    void usersCanOnlyAccessTheirOwnDocuments() throws Exception {
        String emailA = "owner-" + UUID.randomUUID() + "@example.test";
        String emailB = "other-" + UUID.randomUUID() + "@example.test";
        String password = "strong-password-123";

        String tokenA = registerAndGetToken(emailA, "Owner A", password);
        String tokenB = registerAndGetToken(emailB, "Owner B", password);

        String document = """
                {"title":"Private document","description":"Only its owner can see this"}
                """;
        String created = mockMvc.perform(post("/api/documents")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(document))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long documentId = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(get("/api/documents")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/documents")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/documents/{id}", documentId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    @Test
    void regularUsersCannotAccessAdminEndpoints() throws Exception {
        String email = "regular-" + UUID.randomUUID() + "@example.test";
        String token = registerAndGetToken(email, "Regular User", "strong-password-123");

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private String registerAndGetToken(String email, String displayName, String password) throws Exception {
        String request = """
                {
                  "email": "%s",
                  "displayName": "%s",
                  "password": "%s"
                }
                """.formatted(email, displayName, password);
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    @Test
    void rejectsWeakRegistrationPassword() throws Exception {
        String request = """
                {
                  "email": "weak-password@example.test",
                  "displayName": "Weak Password",
                  "password": "short"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void protectsDocumentEndpointsAndCurrentUserEndpoint() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }
}
