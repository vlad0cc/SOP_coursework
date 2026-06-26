package edu.rutmiit.demo.documentrest.event;

import edu.rutmiit.demo.documentsapicontract.dto.EmployeeResponse;
import edu.rutmiit.demo.events.EmployeeEvent;
import edu.rutmiit.demo.events.EventEnvelope;
import edu.rutmiit.demo.events.RoutingKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Публикация доменных событий сотрудников в RabbitMQ.
 */
@Component
public class EmployeeEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EmployeeEventPublisher.class);
    private static final String SOURCE = "document-rest";

    private final RabbitTemplate rabbitTemplate;

    public EmployeeEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishCreated(EmployeeResponse employee) {
        var event = new EmployeeEvent.Created(
                employee.getId(),
                employee.getFullName(),
                employee.getPosition()
        );
        send(RoutingKeys.EMPLOYEE_CREATED, event);
    }

    public void publishDeleted(EmployeeResponse employee, int deletedDocumentsCount) {
        var event = new EmployeeEvent.Deleted(
                employee.getId(),
                employee.getFullName(),
                deletedDocumentsCount
        );
        send(RoutingKeys.EMPLOYEE_DELETED, event);
    }

    private void send(String routingKey, EmployeeEvent event) {
        try {
            EventEnvelope<EmployeeEvent> envelope = EventEnvelope.wrap(event, SOURCE, routingKey);
            rabbitTemplate.convertAndSend(RoutingKeys.EXCHANGE, routingKey, envelope);
            log.info("Событие отправлено: {} [eventId={}]", routingKey, envelope.metadata().eventId());
        } catch (Exception e) {
            log.error("Не удалось отправить событие {}: {}", routingKey, e.getMessage());
        }
    }
}
