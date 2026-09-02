package com.sakshi.brainforgeai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentServiceTest {

    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        documentService = new DocumentService();
    }

    @Test
    void testChunkText() {
        String sample = "BrainForge AI is an advanced enterprise AI knowledge platform. ".repeat(20);
        List<String> chunks = documentService.chunkText(sample, 200, 50);

        assertFalse(chunks.isEmpty());
        assertTrue(chunks.size() > 1);
    }

    @Test
    void testFindRelevantChunks() {
        String docText = "Spring Security protects the REST API with stateless JWT tokens. " +
                "Database migrations are handled by Hibernate JPA. " +
                "Google Gemini 2.5 Flash is used for generative AI.";

        List<String> relevant = documentService.findRelevantChunks("stateless JWT tokens", docText, 2);

        assertFalse(relevant.isEmpty());
        assertTrue(relevant.get(0).contains("Spring Security protects"));
    }
}
