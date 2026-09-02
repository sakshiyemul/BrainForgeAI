package com.sakshi.brainforgeai.service;

import com.sakshi.brainforgeai.dto.DocumentResponse;
import com.sakshi.brainforgeai.entity.DocumentEntity;
import com.sakshi.brainforgeai.entity.UsageMetric;
import com.sakshi.brainforgeai.entity.User;
import com.sakshi.brainforgeai.exception.UnauthorizedActionException;
import com.sakshi.brainforgeai.repository.DocumentRepository;
import com.sakshi.brainforgeai.repository.UsageMetricRepository;
import com.sakshi.brainforgeai.repository.UserRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UsageMetricRepository usageMetricRepository;

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    @Transactional
    public DocumentResponse uploadDocument(MultipartFile file) throws IOException {
        User user = getAuthenticatedUser();
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.txt";
        String contentType = file.getContentType() != null ? file.getContentType() : "text/plain";
        long size = file.getSize();

        // Security check: Max 10MB
        if (size > 10 * 1024 * 1024) {
            throw new RuntimeException("File size exceeds 10MB limit");
        }

        String extractedText = "";

        if (originalFilename.toLowerCase().endsWith(".pdf") || "application/pdf".equals(contentType)) {
            try (PDDocument pdDoc = Loader.loadPDF(file.getBytes())) {
                PDFTextStripper stripper = new PDFTextStripper();
                extractedText = stripper.getText(pdDoc);
            } catch (Exception e) {
                logger.error("Failed to parse PDF document", e);
                throw new RuntimeException("Failed to extract text from PDF: " + e.getMessage());
            }
        } else {
            extractedText = new String(file.getBytes(), StandardCharsets.UTF_8);
        }

        extractedText = extractedText.trim();
        if (extractedText.isEmpty()) {
            throw new RuntimeException("Document contains no readable text");
        }

        List<String> chunks = chunkText(extractedText, 600, 100);

        DocumentEntity document = new DocumentEntity(originalFilename, contentType, size, extractedText, user);
        document.setChunkCount(chunks.size());

        DocumentEntity saved = documentRepository.save(document);

        // Record telemetry
        usageMetricRepository.save(new UsageMetric(user, "DOC_UPLOAD", chunks.size() * 150, "Uploaded: " + originalFilename));

        String preview = extractedText.length() > 200 ? extractedText.substring(0, 197) + "..." : extractedText;

        return new DocumentResponse(
                saved.getId(),
                saved.getFileName(),
                saved.getFileType(),
                saved.getFileSize(),
                saved.getChunkCount(),
                saved.getCreatedAt(),
                preview
        );
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getUserDocuments() {
        User user = getAuthenticatedUser();
        List<DocumentEntity> list = documentRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<DocumentResponse> result = new ArrayList<>();

        for (DocumentEntity d : list) {
            String preview = d.getExtractedText().length() > 200
                    ? d.getExtractedText().substring(0, 197) + "..."
                    : d.getExtractedText();
            result.add(new DocumentResponse(
                    d.getId(),
                    d.getFileName(),
                    d.getFileType(),
                    d.getFileSize(),
                    d.getChunkCount(),
                    d.getCreatedAt(),
                    preview
            ));
        }
        return result;
    }

    @Transactional
    public void deleteDocument(Long documentId) {
        User user = getAuthenticatedUser();
        DocumentEntity doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));

        if (!doc.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("You do not have permission to delete this document");
        }

        documentRepository.delete(doc);
    }

    @Transactional(readOnly = true)
    public DocumentEntity getDocument(Long documentId) {
        User user = getAuthenticatedUser();
        DocumentEntity doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));

        if (!doc.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("You do not have permission to access this document");
        }

        return doc;
    }

    public List<String> chunkText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) return chunks;

        int length = text.length();
        int start = 0;

        while (start < length) {
            int end = Math.min(start + chunkSize, length);
            chunks.add(text.substring(start, end));
            if (end == length) break;
            start += (chunkSize - overlap);
        }
        return chunks;
    }

    public List<String> findRelevantChunks(String query, String text, int topK) {
        List<String> chunks = chunkText(text, 600, 100);
        if (chunks.isEmpty()) return Collections.emptyList();

        String[] queryWords = query.toLowerCase().split("\\W+");
        Set<String> keywords = new HashSet<>(Arrays.asList(queryWords));
        keywords.removeIf(w -> w.length() <= 2 || Set.of("the", "and", "for", "with", "this", "that", "what", "how", "why", "are", "you", "can").contains(w));

        Map<String, Integer> scores = new HashMap<>();
        for (String chunk : chunks) {
            String lowerChunk = chunk.toLowerCase();
            int score = 0;
            for (String kw : keywords) {
                if (lowerChunk.contains(kw)) {
                    score += 10;
                    // Boost if word appears multiple times
                    int count = (lowerChunk.length() - lowerChunk.replace(kw, "").length()) / kw.length();
                    score += count * 2;
                }
            }
            scores.put(chunk, score);
        }

        return chunks.stream()
                .filter(c -> scores.getOrDefault(c, 0) > 0)
                .sorted((a, b) -> Integer.compare(scores.getOrDefault(b, 0), scores.getOrDefault(a, 0)))
                .limit(topK)
                .toList();
    }
}
