package com.chetan.dms.document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.chetan.dms.user.UserRepository;
import com.chetan.dms.support.TestSecurity;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DocumentRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
        TestSecurity.ensureUser(userRepository, passwordEncoder);
    }

    @Test
    void supportsDocumentCreateReadUpdateAndDelete() throws Exception {
        String createPayload = """
                {
                  "title": "Architecture Guide",
                  "description": "Initial system architecture",
                  "fileName": "architecture.pdf",
                  "contentType": "application/pdf",
                  "fileSizeBytes": 2048
                }
                """;

        String response = mockMvc.perform(post("/api/documents").with(TestSecurity.user())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Architecture Guide"))
                .andExpect(jsonPath("$.fileName").value("architecture.pdf"))
                .andReturn().getResponse().getContentAsString();

        JsonNode created = objectMapper.readTree(response);
        long id = created.get("id").asLong();

        mockMvc.perform(get("/api/documents").with(TestSecurity.user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id));

        mockMvc.perform(get("/api/documents/{id}", id).with(TestSecurity.user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Architecture Guide"));

        String updatePayload = """
                {
                  "title": "Updated Architecture Guide",
                  "description": "Reviewed architecture",
                  "fileName": "architecture-v2.pdf",
                  "contentType": "application/pdf",
                  "fileSizeBytes": 4096
                }
                """;

        mockMvc.perform(put("/api/documents/{id}", id).with(TestSecurity.user())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Architecture Guide"))
                .andExpect(jsonPath("$.fileSizeBytes").value(4096));

        mockMvc.perform(delete("/api/documents/{id}", id).with(TestSecurity.user()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/documents/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsBlankDocumentTitle() throws Exception {
        String invalidPayload = """
                {
                  "title": "   ",
                  "description": "This request should fail validation"
                }
                """;

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void returnsNotFoundForUnknownDocument() throws Exception {
        mockMvc.perform(get("/api/documents/{id}", 999999L).with(TestSecurity.user()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Document not found"));
    }
}
