package com.sakshi.brainforgeai.service;

import com.sakshi.brainforgeai.ai.AiService;
import com.sakshi.brainforgeai.dto.DocumentQueryResponse;
import com.sakshi.brainforgeai.entity.DocumentEntity;
import com.sakshi.brainforgeai.entity.UsageMetric;
import com.sakshi.brainforgeai.entity.User;
import com.sakshi.brainforgeai.repository.DocumentRepository;
import com.sakshi.brainforgeai.repository.UsageMetricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RagService {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UsageMetricRepository usageMetricRepository;

    @Autowired
    private AiService aiService;

    public String augmentPromptWithKnowledge(String userPrompt, User user) {
        List<DocumentEntity> userDocs = documentRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        if (userDocs.isEmpty()) {
            return userPrompt;
        }

        List<String> aggregatedContext = new ArrayList<>();

        for (DocumentEntity doc : userDocs) {
            List<String> matched = documentService.findRelevantChunks(userPrompt, doc.getExtractedText(), 2);
            for (String chunk : matched) {
                aggregatedContext.add("[Source Document: " + doc.getFileName() + "]\n" + chunk);
            }
            if (aggregatedContext.size() >= 5) break;
        }

        if (aggregatedContext.isEmpty()) {
            return userPrompt;
        }

        StringBuilder augmented = new StringBuilder();
        augmented.append("You are an intelligent AI assistant with access to the user's personal knowledge base.\n");
        augmented.append("Below is relevant context extracted from their uploaded documents. Prioritize this context when answering:\n\n");
        augmented.append("=== KNOWLEDGE BASE CONTEXT ===\n");
        for (String ctx : aggregatedContext) {
            augmented.append(ctx).append("\n---\n");
        }
        augmented.append("=== END CONTEXT ===\n\n");
        augmented.append("User Query: ").append(userPrompt);

        return augmented.toString();
    }

    public DocumentQueryResponse queryDocument(Long documentId, String query) {
        DocumentEntity doc = documentService.getDocument(documentId);
        List<String> relevantSnippets = documentService.findRelevantChunks(query, doc.getExtractedText(), 4);

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert AI analysis engine. Answer the question below strictly based on the following document excerpts from '")
                .append(doc.getFileName()).append("':\n\n");

        if (relevantSnippets.isEmpty()) {
            prompt.append("Document excerpt:\n")
                    .append(doc.getExtractedText().length() > 2000 ? doc.getExtractedText().substring(0, 2000) : doc.getExtractedText())
                    .append("\n\n");
        } else {
            for (String snippet : relevantSnippets) {
                prompt.append(snippet).append("\n---\n");
            }
        }

        prompt.append("\nQuestion: ").append(query);

        String answer = aiService.generateAnswer(prompt.toString());

        int tokens = (prompt.length() + answer.length()) / 4;
        usageMetricRepository.save(new UsageMetric(doc.getUser(), "RAG_DOC_QUERY", tokens, "Document: " + doc.getFileName()));

        return new DocumentQueryResponse(answer, relevantSnippets);
    }
}
