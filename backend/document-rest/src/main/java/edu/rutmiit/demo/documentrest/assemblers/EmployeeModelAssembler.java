package edu.rutmiit.demo.documentrest.assemblers;

import edu.rutmiit.demo.documentsapicontract.dto.EmployeeResponse;
import edu.rutmiit.demo.documentrest.controllers.EmployeeController;
import edu.rutmiit.demo.documentrest.controllers.DocumentController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class EmployeeModelAssembler implements RepresentationModelAssembler<EmployeeResponse, EntityModel<EmployeeResponse>> {

    @Override
    public EntityModel<EmployeeResponse> toModel(EmployeeResponse employee) {
        return EntityModel.of(employee,
                linkTo(methodOn(EmployeeController.class).getEmployeeById(employee.getId())).withSelfRel(),
                linkTo(methodOn(DocumentController.class).getAllDocuments(employee.getId(), null, null, null, 0, 20)).withRel("documents"),
                linkTo(methodOn(EmployeeController.class).getAllEmployees(0, 20)).withRel("collection")
        );
    }
}