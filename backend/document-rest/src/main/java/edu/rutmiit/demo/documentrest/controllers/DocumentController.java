package edu.rutmiit.demo.documentrest.controllers;

import edu.rutmiit.demo.documentsapicontract.dto.*;
import edu.rutmiit.demo.documentsapicontract.endpoints.DocumentApi;
import edu.rutmiit.demo.documentrest.assemblers.DocumentModelAssembler;
import edu.rutmiit.demo.documentrest.service.AuthService;
import edu.rutmiit.demo.documentrest.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DocumentController implements DocumentApi {

    private final DocumentService documentService;
    private final AuthService authService;
    private final DocumentModelAssembler documentModelAssembler;
    private final PagedResourcesAssembler<DocumentResponse> pagedResourcesAssembler;

    public DocumentController(DocumentService documentService, AuthService authService, DocumentModelAssembler documentModelAssembler,
                          PagedResourcesAssembler<DocumentResponse> pagedResourcesAssembler) {
        this.documentService = documentService;
        this.authService = authService;
        this.documentModelAssembler = documentModelAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @Override
    public EntityModel<DocumentResponse> getDocumentById(Long id) {
        return documentModelAssembler.toModel(documentService.findDocumentById(id));
    }

    @Override
    public PagedModel<EntityModel<DocumentResponse>> getAllDocuments(Long senderId, Long recipientId, String status,
                                                            String titleSearch, int page, int size) {
        PagedResponse<DocumentResponse> paged = documentService.findAllDocuments(senderId, recipientId, status, titleSearch, page, size);
        Page<DocumentResponse> springPage = new PageImpl<>(
                paged.content(),
                PageRequest.of(paged.pageNumber(), paged.pageSize()),
                paged.totalElements()
        );
        return pagedResourcesAssembler.toModel(springPage, documentModelAssembler);
    }

    @Override
    public ResponseEntity<EntityModel<DocumentResponse>> createDocument(DocumentRequest request) {
        DocumentResponse created = documentService.createDocument(request);
        EntityModel<DocumentResponse> model = documentModelAssembler.toModel(created);
        return ResponseEntity
                .created(model.getRequiredLink("self").toUri())
                .body(model);
    }

    @Override
    public EntityModel<DocumentResponse> updateDocument(Long id, UpdateDocumentRequest request) {
        return documentModelAssembler.toModel(documentService.updateDocument(id, request));
    }

    @Override
    public EntityModel<DocumentResponse> patchDocument(Long id, PatchDocumentRequest request) {
        return documentModelAssembler.toModel(documentService.patchDocument(id, request));
    }

    @Override
    public void deleteDocument(Long id) {
        documentService.deleteDocument(id);
    }

    @GetMapping("/my")
    public List<DocumentResponse> getMyDocuments(@RequestHeader("X-User-Id") Long userId) {
        authService.requireUser(userId);
        return documentService.findMyDocuments(userId);
    }

    @PostMapping("/my")
    public DocumentResponse createMyDocument(@RequestHeader("X-User-Id") Long userId,
                                             @Valid @RequestBody DocumentRequest request) {
        authService.requireUser(userId);
        return documentService.createDocumentForUser(userId, request);
    }

    @PostMapping("/{id}/sign")
    public DocumentResponse signDocument(@RequestHeader("X-User-Id") Long userId,
                                         @PathVariable Long id) {
        authService.requireUser(userId);
        return documentService.signDocument(id, userId);
    }

    @PostMapping("/{id}/decline")
    public DocumentResponse declineDocument(@RequestHeader("X-User-Id") Long userId,
                                            @PathVariable Long id,
                                            @Valid @RequestBody DeclineDocumentRequest request) {
        authService.requireUser(userId);
        return documentService.declineDocument(id, userId, request.reason());
    }
}
