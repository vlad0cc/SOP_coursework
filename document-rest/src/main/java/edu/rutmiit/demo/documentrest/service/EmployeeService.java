package edu.rutmiit.demo.documentrest.service;

import edu.rutmiit.demo.documentsapicontract.dto.*;
import edu.rutmiit.demo.documentsapicontract.exception.ResourceNotFoundException;
import edu.rutmiit.demo.documentrest.event.EmployeeEventPublisher;
import edu.rutmiit.demo.documentrest.storage.InMemoryStorage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {
    private final InMemoryStorage storage;
    private final DocumentService documentService;
    private final EmployeeEventPublisher eventPublisher;

    public EmployeeService(InMemoryStorage storage,
                           @Lazy DocumentService documentService,
                           EmployeeEventPublisher eventPublisher) {
        this.storage = storage;
        this.documentService = documentService;
        this.eventPublisher = eventPublisher;
    }

    public PagedResponse<EmployeeResponse> findAll(int page, int size) {
        List<EmployeeResponse> all = storage.employees.values().stream()
                .sorted(Comparator.comparingLong(EmployeeResponse::getId))
                .toList();
        int totalElements = all.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        int from = page * size;
        int to = Math.min(from + size, totalElements);
        List<EmployeeResponse> content = (from >= totalElements) ? List.of() : all.subList(from, to);
        return new PagedResponse<>(content, page, size, totalElements, totalPages, page >= totalPages - 1);
    }

    public EmployeeResponse findById(Long id) {
        return Optional.ofNullable(storage.employees.get(id))
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
    }

    public EmployeeResponse create(EmployeeRequest request) {
        long id = storage.employeeSequence.incrementAndGet();
        EmployeeResponse employee = EmployeeResponse.builder()
                .id(id)
                .fullName(request.fullName())
                .position(request.position())
                .documentsCount(0)
                .build();
        storage.employees.put(id, employee);
        eventPublisher.publishCreated(employee);
        return employee;
    }

    public EmployeeResponse update(Long id, EmployeeRequest request) {
        EmployeeResponse existing = findById(id);
        EmployeeResponse updatedEmployee = EmployeeResponse.builder()
                .id(id)
                .fullName(request.fullName())
                .position(request.position())
                .documentsCount(existing.getDocumentsCount())
                .build();
        storage.employees.put(id, updatedEmployee);
        return updatedEmployee;
    }

    public EmployeeResponse patchEmployee(Long id, PatchEmployeeRequest request) {
        EmployeeResponse existing = findById(id);
        EmployeeResponse updated = EmployeeResponse.builder()
                .id(id)
                .fullName(request.fullName() != null ? request.fullName() : existing.getFullName())
                .position(request.position() != null ? request.position() : existing.getPosition())
                .documentsCount(existing.getDocumentsCount())
                .build();
        storage.employees.put(id, updated);
        return updated;
    }

    public void delete(Long id) {
        EmployeeResponse employee = findById(id);

        int documentsCount = (int) storage.documents.values().stream()
                .filter(d -> (d.getSender() != null && d.getSender().getId().equals(id))
                        || (d.getRecipient() != null && d.getRecipient().getId().equals(id)))
                .count();

        documentService.deleteDocumentsByEmployeeId(id);
        storage.employees.remove(id);
        eventPublisher.publishDeleted(employee, documentsCount);
    }
}
