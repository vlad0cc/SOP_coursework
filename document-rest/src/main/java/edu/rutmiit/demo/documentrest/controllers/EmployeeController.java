package edu.rutmiit.demo.documentrest.controllers;

import edu.rutmiit.demo.documentsapicontract.dto.*;
import edu.rutmiit.demo.documentsapicontract.endpoints.EmployeeApi;
import edu.rutmiit.demo.documentrest.assemblers.EmployeeModelAssembler;
import edu.rutmiit.demo.documentrest.assemblers.DocumentModelAssembler;
import edu.rutmiit.demo.documentrest.service.EmployeeService;
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
public class EmployeeController implements EmployeeApi {

    private final EmployeeService employeeService;
    private final DocumentService documentService;
    private final EmployeeModelAssembler employeeModelAssembler;
    private final DocumentModelAssembler documentModelAssembler;
    private final PagedResourcesAssembler<EmployeeResponse> pagedEmployeesAssembler;
    private final PagedResourcesAssembler<DocumentResponse> pagedDocumentsAssembler;

    public EmployeeController(EmployeeService employeeService,
                            DocumentService documentService,
                            EmployeeModelAssembler employeeModelAssembler,
                            DocumentModelAssembler documentModelAssembler,
                            PagedResourcesAssembler<EmployeeResponse> pagedEmployeesAssembler,
                            PagedResourcesAssembler<DocumentResponse> pagedDocumentsAssembler) {
        this.employeeService = employeeService;
        this.documentService = documentService;
        this.employeeModelAssembler = employeeModelAssembler;
        this.documentModelAssembler = documentModelAssembler;
        this.pagedEmployeesAssembler = pagedEmployeesAssembler;
        this.pagedDocumentsAssembler = pagedDocumentsAssembler;
    }

    @Override
    public PagedModel<EntityModel<EmployeeResponse>> getAllEmployees(int page, int size) {
        PagedResponse<EmployeeResponse> paged = employeeService.findAll(page, size);
        Page<EmployeeResponse> springPage = new PageImpl<>(
                paged.content(),
                PageRequest.of(paged.pageNumber(), paged.pageSize()),
                paged.totalElements()
        );
        return pagedEmployeesAssembler.toModel(springPage, employeeModelAssembler);
    }

    @Override
    public EntityModel<EmployeeResponse> getEmployeeById(Long id) {
        return employeeModelAssembler.toModel(employeeService.findById(id));
    }

    @Override
    public ResponseEntity<EntityModel<EmployeeResponse>> createEmployee(EmployeeRequest request) {
        EmployeeResponse created = employeeService.create(request);
        EntityModel<EmployeeResponse> model = employeeModelAssembler.toModel(created);
        return ResponseEntity
                .created(model.getRequiredLink("self").toUri())
                .body(model);
    }

    @Override
    public EntityModel<EmployeeResponse> updateEmployee(Long id, EmployeeRequest request) {
        return employeeModelAssembler.toModel(employeeService.update(id, request));
    }

    @Override
    public EntityModel<EmployeeResponse> patchEmployee(Long id, PatchEmployeeRequest request) {
        return employeeModelAssembler.toModel(employeeService.patchEmployee(id, request));
    }

    @Override
    public void deleteEmployee(Long id) {
        employeeService.delete(id);
    }

    @Override
    public PagedModel<EntityModel<DocumentResponse>> getDocumentsByEmployee(Long id, int page, int size) {
        // Проверяем что сотрудник существует (выбросит 404 если нет)
        employeeService.findById(id);
        PagedResponse<DocumentResponse> paged = documentService.findAllDocuments(id, null, null, null, page, size);
        Page<DocumentResponse> springPage = new PageImpl<>(
                paged.content(),
                PageRequest.of(paged.pageNumber(), paged.pageSize()),
                paged.totalElements()
        );
        return pagedDocumentsAssembler.toModel(springPage, documentModelAssembler);
    }
}
