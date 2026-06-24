package edu.rutmiit.demo.documentrest.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import edu.rutmiit.demo.documentsapicontract.dto.DocumentResponse;
import edu.rutmiit.demo.documentsapicontract.dto.EmployeeResponse;
import edu.rutmiit.demo.documentrest.service.EmployeeService;

/**
 * Вложенные резолверы для полей Document.sender и Document.recipient.
 */
@DgsComponent
public class DocumentEmployeeDataFetcher {

    private final EmployeeService employeeService;

    public DocumentEmployeeDataFetcher(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @DgsData(parentType = "Document", field = "sender")
    public EmployeeResponse sender(DgsDataFetchingEnvironment dfe) {
        DocumentResponse document = dfe.getSource();
        return document.getSender();
    }

    @DgsData(parentType = "Document", field = "recipient")
    public EmployeeResponse recipient(DgsDataFetchingEnvironment dfe) {
        DocumentResponse document = dfe.getSource();
        return document.getRecipient();
    }
}
