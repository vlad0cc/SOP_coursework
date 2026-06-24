package edu.rutmiit.demo.documentrest.storage;

import edu.rutmiit.demo.documentsapicontract.dto.DocumentResponse;
import edu.rutmiit.demo.documentsapicontract.dto.EmployeeResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryStorage {
    public final Map<Long, EmployeeResponse> employees = new ConcurrentHashMap<>();
    public final Map<Long, DocumentResponse> documents = new ConcurrentHashMap<>();

    public final AtomicLong employeeSequence = new AtomicLong(0);
    public final AtomicLong documentSequence = new AtomicLong(0);

    @PostConstruct
    public void init() {
        EmployeeResponse employee1 = EmployeeResponse.builder()
                .id(employeeSequence.incrementAndGet())
                .fullName("Иванов Иван Иванович")
                .position("Главный бухгалтер")
                .documentsCount(2)
                .build();

        EmployeeResponse employee2 = EmployeeResponse.builder()
                .id(employeeSequence.incrementAndGet())
                .fullName("Петров Пётр Петрович")
                .position("Руководитель отдела закупок")
                .documentsCount(1)
                .build();

        employees.put(employee1.getId(), employee1);
        employees.put(employee2.getId(), employee2);

        long documentId1 = documentSequence.incrementAndGet();
        documents.put(documentId1, DocumentResponse.builder()
                .id(documentId1)
                .title("Служебная записка")
                .documentNumber("A-123-456")
                .sender(employee1)
                .recipient(employee2)
                .description("Документ для согласования условий поставки.")
                .status("SENT")
                .build());

        long documentId2 = documentSequence.incrementAndGet();
        documents.put(documentId2, DocumentResponse.builder()
                .id(documentId2)
                .title("Акт приёма-передачи")
                .documentNumber("B-234-567")
                .sender(employee2)
                .recipient(employee1)
                .description("Документ о передаче оборудования.")
                .status("RECEIVED_WITH_SIGNATURE")
                .signature("Петров П.П.")
                .build());

        long documentId3 = documentSequence.incrementAndGet();
        documents.put(documentId3, DocumentResponse.builder()
                .id(documentId3)
                .title("Заявка на оплату")
                .documentNumber("C-345-678")
                .sender(employee1)
                .recipient(employee2)
                .description("Документ для оплаты счёта поставщика.")
                .status("SENT")
                .build());
    }
}
