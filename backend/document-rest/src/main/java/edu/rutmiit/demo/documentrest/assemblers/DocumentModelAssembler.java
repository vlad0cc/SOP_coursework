package edu.rutmiit.demo.documentrest.assemblers;

import edu.rutmiit.demo.documentsapicontract.dto.DocumentResponse;
import edu.rutmiit.demo.documentrest.controllers.EmployeeController;
import edu.rutmiit.demo.documentrest.controllers.DocumentController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class DocumentModelAssembler implements RepresentationModelAssembler<DocumentResponse, EntityModel<DocumentResponse>> {

    @Override
    public EntityModel<DocumentResponse> toModel(DocumentResponse document) {
        EntityModel<DocumentResponse> model = EntityModel.of(document,
                linkTo(methodOn(DocumentController.class).getDocumentById(document.getId())).withSelfRel(),
                linkTo(methodOn(DocumentController.class).getAllDocuments(null, null, null, null, 0, 20)).withRel("collection")
        );
        if (document.getSender() != null) {
            model.add(linkTo(methodOn(EmployeeController.class)
                    .getEmployeeById(document.getSender().getId())).withRel("sender"));
        }
        if (document.getRecipient() != null) {
            model.add(linkTo(methodOn(EmployeeController.class)
                    .getEmployeeById(document.getRecipient().getId())).withRel("recipient"));
        }
        return model;
    }
}
