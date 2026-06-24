package edu.rutmiit.demo.events;

/**
 * Константы для маршрутизации событий в RabbitMQ.
 */
public final class RoutingKeys {

    private RoutingKeys() {
        // утилитарный класс — экземпляры не создаём
    }

    // Имя общего topic exchange для доменных событий
    public static final String EXCHANGE = "documents.events";

    // Routing keys для событий документов
    public static final String DOCUMENT_CREATED = "document.created";
    public static final String DOCUMENT_UPDATED = "document.updated";
    public static final String DOCUMENT_DELETED = "document.deleted";
    public static final String DOCUMENT_AUDITED = "document.audited";

    // Routing keys для событий сотрудников
    public static final String EMPLOYEE_CREATED = "employee.created";
    public static final String EMPLOYEE_DELETED = "employee.deleted";

    // Паттерны для подписки (wildcard)
    public static final String ALL_DOCUMENT_EVENTS = "document.*";
    public static final String ALL_EMPLOYEE_EVENTS = "employee.*";
    public static final String ALL_EVENTS = "#";
}
