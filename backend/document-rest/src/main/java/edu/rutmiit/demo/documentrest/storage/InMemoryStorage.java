package edu.rutmiit.demo.documentrest.storage;

import edu.rutmiit.demo.documentsapicontract.dto.DocumentResponse;
import edu.rutmiit.demo.documentsapicontract.dto.EmployeeResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryStorage {
    public final Map<Long, EmployeeResponse> employees = new ConcurrentHashMap<>();
    public final Map<Long, DocumentResponse> documents = new ConcurrentHashMap<>();
    public final Map<String, String> passwords = new ConcurrentHashMap<>();
    public final Map<String, Long> userEmployees = new ConcurrentHashMap<>();

    public final AtomicLong employeeSequence = new AtomicLong(0);
    public final AtomicLong documentSequence = new AtomicLong(0);

    @PostConstruct
    public void init() {
        EmployeeResponse employee1 = EmployeeResponse.builder()
                .id(employeeSequence.incrementAndGet())
                .fullName("Иванов Алексей Сергеевич")
                .position("Руководитель отдела продаж")
                .documentsCount(0)
                .build();

        EmployeeResponse employee2 = EmployeeResponse.builder()
                .id(employeeSequence.incrementAndGet())
                .fullName("Петрова Мария Андреевна")
                .position("Главный бухгалтер")
                .documentsCount(0)
                .build();

        EmployeeResponse employee3 = EmployeeResponse.builder()
                .id(employeeSequence.incrementAndGet())
                .fullName("Смирнов Дмитрий Олегович")
                .position("Юрист")
                .documentsCount(0)
                .build();

        employees.put(employee1.getId(), employee1);
        employees.put(employee2.getId(), employee2);
        employees.put(employee3.getId(), employee3);

        passwords.put("user1", "user1");
        passwords.put("user2", "user2");
        passwords.put("user3", "user3");
        userEmployees.put("user1", employee1.getId());
        userEmployees.put("user2", employee2.getId());
        userEmployees.put("user3", employee3.getId());

        addDocument("Договор поставки ноутбуков", "A-101-202", "Договор", employee1, employee2,
                LocalDate.now().minusDays(5), LocalDate.now().plusDays(2), "Срочный",
                "Поставка 12 ноутбуков для отдела продаж. Требуется проверка суммы и условий оплаты.",
                "SENT", null, null);
        addDocument("Акт выполненных работ по CRM", "B-214-330", "Акт", employee2, employee3,
                LocalDate.now().minusDays(3), LocalDate.now().plusDays(4), "Обычный",
                "Акт по доработке карточки клиента и отчетности. Нужна юридическая проверка.",
                "SENT", null, null);
        addDocument("Служебная записка о командировке", "C-310-118", "Служебная записка", employee3, employee1,
                LocalDate.now().minusDays(8), LocalDate.now().minusDays(1), "Низкий",
                "Согласование командировки на переговоры с поставщиком.",
                "SIGNED", employee1.getFullName(), null);
        addDocument("Заявка на оплату счета", "D-447-902", "Заявка", employee1, employee2,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), "Срочный",
                "Оплата счета за лицензии аналитической системы.",
                "DECLINED", null, "Не приложен счет от поставщика.");
        addDocument("Договор аренды офиса", "E-120-441", "Договор", employee2, employee1,
                LocalDate.now().minusDays(2), LocalDate.now().plusDays(6), "Обычный",
                "Согласование договора аренды переговорной зоны для нового отдела.",
                "SENT", null, null);
        addDocument("Заявка на закупку мебели", "F-238-510", "Заявка", employee3, employee1,
                LocalDate.now().minusDays(1), LocalDate.now(), "Срочный",
                "Закупка рабочих столов и кресел для проектной группы.",
                "SENT", null, null);
        addDocument("Акт приемки оборудования", "G-501-728", "Акт", employee1, employee3,
                LocalDate.now().minusDays(6), LocalDate.now().minusDays(2), "Обычный",
                "Приемка серверного оборудования после установки в офисе.",
                "SIGNED", employee3.getFullName(), null);
        addDocument("Служебная записка по обучению", "H-342-114", "Служебная записка", employee2, employee1,
                LocalDate.now().minusDays(4), LocalDate.now().plusDays(3), "Низкий",
                "Прошу согласовать обучение сотрудников финансового отдела.",
                "DECLINED", null, "Нужно уточнить программу обучения и стоимость участия.");
        addDocument("Договор технической поддержки", "I-713-606", "Договор", employee3, employee2,
                LocalDate.now().minusDays(2), LocalDate.now().plusDays(7), "Обычный",
                "Проверка условий технической поддержки корпоративного портала.",
                "SENT", null, null);
        addDocument("Заявка на доступ к архиву", "J-884-230", "Заявка", employee2, employee3,
                LocalDate.now().minusDays(7), LocalDate.now().minusDays(1), "Срочный",
                "Предоставление временного доступа к архиву договоров.",
                "RETURNED_BY_AUDIT", null, "Аудит вернул документ на уточнение основания доступа.");
        addDocument("Акт сверки расчетов", "K-459-812", "Акт", employee1, employee2,
                LocalDate.now().minusDays(9), LocalDate.now().plusDays(5), "Обычный",
                "Сверка расчетов с поставщиком строительных материалов.",
                "SIGNED", employee2.getFullName(), null);
        addDocument("Договор консультационных услуг", "L-640-337", "Договор", employee2, employee1,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(10), "Низкий",
                "Проверка договора на консультационные услуги для отдела продаж.",
                "SENT", null, null);
    }

    private void addDocument(String title,
                             String documentNumber,
                             String type,
                             EmployeeResponse sender,
                             EmployeeResponse recipient,
                             LocalDate createdAt,
                             LocalDate dueDate,
                             String priority,
                             String description,
                             String status,
                             String signature,
                             String declineReason) {
        long documentId = documentSequence.incrementAndGet();
        documents.put(documentId, DocumentResponse.builder()
                .id(documentId)
                .title(title)
                .documentNumber(documentNumber)
                .type(type)
                .sender(sender)
                .recipient(recipient)
                .createdAt(createdAt)
                .dueDate(dueDate)
                .priority(priority)
                .description(description)
                .status(status)
                .signature(signature)
                .declineReason(declineReason)
                .build());
    }
}
