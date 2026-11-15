package com.chetan.dms.document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.chetan.dms.user.UserAccount;
import com.chetan.dms.user.UserRepository;
import com.chetan.dms.support.TestSecurity;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentFileControllerIntegrationTest {

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
    void uploadsAndDownloadsOriginalFileBytes() throws Exception {
        byte[] fileBytes = "Project Sirius milestone two".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "project-notes.txt", MediaType.TEXT_PLAIN_VALUE, fileBytes);

        String response = mockMvc.perform(multipart("/api/documents/upload").with(TestSecurity.user())
                        .file(file)
                        .param("title", "Project Notes")
                        .param("description", "Milestone two test document"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Project Notes"))
                .andExpect(jsonPath("$.fileName").value("project-notes.txt"))
                .andExpect(jsonPath("$.fileAvailable").value(true))
                .andExpect(jsonPath("$.downloadUrl").exists())
                .andReturn().getResponse().getContentAsString();

        JsonNode created = objectMapper.readTree(response);
        long id = created.get("id").asLong();

        byte[] downloaded = mockMvc.perform(get("/api/documents/{id}/file", id).with(TestSecurity.user()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("attachment")))
                .andReturn().getResponse().getContentAsByteArray();

        assertArrayEquals(fileBytes, downloaded);
    }

    @Test
    void rejectsEmptyUploads() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.txt", MediaType.TEXT_PLAIN_VALUE, new byte[0]);

        mockMvc.perform(multipart("/api/documents/upload").file(emptyFile).with(TestSecurity.user()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"));
    }

    @Test
    void returnsNotFoundWhenDocumentHasNoUploadedFile() throws Exception {
        Document saved = new Document("Metadata only", "No binary attached", null, null, null);
        saved.assignOwner(userRepository.findByEmail(TestSecurity.USER_EMAIL).orElseThrow());
        saved = repository.save(saved);

        mockMvc.perform(get("/api/documents/{id}/file", saved.getId()).with(TestSecurity.user()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Document file not found"));
    }

    @Test
    void deletingDocumentAlsoRemovesItsDownloadableFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "delete-me.txt", MediaType.TEXT_PLAIN_VALUE, "temporary".getBytes());

        String response = mockMvc.perform(multipart("/api/documents/upload").file(file))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/documents/{id}", id).with(TestSecurity.user()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/documents/{id}/file", id))
                .andExpect(status().isNotFound());
    }
}
