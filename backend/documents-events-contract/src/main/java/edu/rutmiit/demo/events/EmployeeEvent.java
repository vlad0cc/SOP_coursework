package edu.rutmiit.demo.events;

/**
 * Семейство событий, связанных с сотрудниками.
 */
public sealed interface EmployeeEvent {

    /**
     * Сотрудник создан.
     */
    record Created(
            Long employeeId,
            String fullName,
            String position
    ) implements EmployeeEvent {}

    /**
     * Сотрудник удалён. В нашей системе удаление каскадное — вместе с документами.
     */
    record Deleted(
            Long employeeId,
            String fullName,
            int deletedDocumentsCount
    ) implements EmployeeEvent {}
}
