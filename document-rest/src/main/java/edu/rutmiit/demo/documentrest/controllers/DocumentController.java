package edu.rutmiit.demo.documentrest.controllers;

import edu.rutmiit.demo.documentsapicontract.dto.*;
import edu.rutmiit.demo.documentsapicontract.endpoints.DocumentApi;
import edu.rutmiit.demo.documentrest.assemblers.DocumentModelAssembler;
import edu.rutmiit.demo.documentrest.service.DocumentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DocumentController implements DocumentApi {

    private final DocumentService documentService;
    private final DocumentModelAssembler documentModelAssembler;
    private final PagedResourcesAssembler<DocumentResponse> pagedResourcesAssembler;

    public DocumentController(DocumentService documentService, DocumentModelAssembler documentModelAssembler,
                          PagedResourcesAssembler<DocumentResponse> pagedResourcesAssembler) {
        this.documentService = documentService;
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
}
