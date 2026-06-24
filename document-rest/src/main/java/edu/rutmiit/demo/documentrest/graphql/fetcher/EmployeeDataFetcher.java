package edu.rutmiit.demo.documentrest.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import edu.rutmiit.demo.documentsapicontract.dto.EmployeeRequest;
import edu.rutmiit.demo.documentsapicontract.dto.EmployeeResponse;
import edu.rutmiit.demo.documentsapicontract.dto.PagedResponse;
import edu.rutmiit.demo.documentrest.graphql.types.EmployeeConnectionGql;
import edu.rutmiit.demo.documentrest.graphql.types.CreateEmployeeInputGql;
import edu.rutmiit.demo.documentrest.graphql.types.PageInfoGql;
import edu.rutmiit.demo.documentrest.graphql.types.UpdateEmployeeInputGql;
import edu.rutmiit.demo.documentrest.service.EmployeeService;

/**
 * DataFetcher для операций с сотрудниками.
 *
 * Обрабатывает корневые поля Query и Mutation, связанные с сотрудниками.
 * Вложенные поля (Employee.documents) обрабатываются в EmployeeDocumentsDataFetcher.
 *
 * Принцип разделения: один DataFetcher — одна группа связанных операций.
 * Это делает код более читаемым и тестируемым.
 */
@DgsComponent
public class EmployeeDataFetcher {

    private final EmployeeService employeeService;

    public EmployeeDataFetcher(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Получение сотрудника по идентификатору.
     * Соответствует полю Query.employee(id: ID!) в схеме.
     */
    @DgsQuery
    public EmployeeResponse employee(@InputArgument String id) {
        return employeeService.findById(Long.parseLong(id));
    }

    /**
     * Список сотрудников с пагинацией.
     * Соответствует полю Query.employees(page, size) в схеме.
     */
    @DgsQuery
    public EmployeeConnectionGql employees(
            @InputArgument Integer page,
            @InputArgument Integer size) {

        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        PagedResponse<EmployeeResponse> paged = employeeService.findAll(pageNum, pageSize);

        return new EmployeeConnectionGql(
                paged.content(),
                new PageInfoGql(paged.pageNumber(), paged.pageSize(), paged.totalPages(), paged.last()),
                (int) paged.totalElements());
    }

    /**
     * Создание сотрудника.
     * Соответствует полю Mutation.createEmployee(input) в схеме.
     */
    @DgsMutation
    public EmployeeResponse createEmployee(@InputArgument CreateEmployeeInputGql input) {
        EmployeeRequest request = new EmployeeRequest(
                input.fullName(),
                input.position()
        );
        return employeeService.create(request);
    }

    /**
     * Обновление сотрудника.
     * Соответствует полю Mutation.updateEmployee(id, input) в схеме.
     */
    @DgsMutation
    public EmployeeResponse updateEmployee(@InputArgument String id, @InputArgument UpdateEmployeeInputGql input) {
        EmployeeRequest request = new EmployeeRequest(
                input.fullName(),
                input.position()
        );
        return employeeService.update(Long.parseLong(id), request);
    }

    /**
     * Удаление сотрудника и всех его документов (каскадно).
     * Соответствует полю Mutation.deleteEmployee(id) в схеме.
     */
    @DgsMutation
    public boolean deleteEmployee(@InputArgument String id) {
        employeeService.delete(Long.parseLong(id));
        return true;
    }
}
