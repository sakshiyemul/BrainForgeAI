package com.sakshi.brainforgeai.controller;

import com.sakshi.brainforgeai.dto.DocumentQueryRequest;
import com.sakshi.brainforgeai.dto.DocumentQueryResponse;
import com.sakshi.brainforgeai.dto.DocumentResponse;
import com.sakshi.brainforgeai.service.DocumentService;
import com.sakshi.brainforgeai.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/documents")
@SecurityRequirement(name = "Bearer Authentication")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private RagService ragService;

    @Operation(summary = "Upload document", description = "Uploads and parses a PDF or text file into the user's knowledge vault.")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentResponse uploadDocument(@RequestParam("file") MultipartFile file) throws IOException {
        return documentService.uploadDocument(file);
    }

    @Operation(summary = "Get user documents", description = "Returns all documents in the user's knowledge base.")
    @GetMapping
    public List<DocumentResponse> getUserDocuments() {
        return documentService.getUserDocuments();
    }

    @Operation(summary = "Delete document", description = "Deletes a document from the knowledge base.")
    @DeleteMapping("/{id}")
    public String deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return "Document deleted successfully.";
    }

    @Operation(summary = "Query document", description = "Asks a question grounded strictly within a specific document.")
    @PostMapping("/{id}/query")
    public DocumentQueryResponse queryDocument(
            @PathVariable Long id,
            @Valid @RequestBody DocumentQueryRequest request) {
        return ragService.queryDocument(id, request.getQuery());
    }
}
