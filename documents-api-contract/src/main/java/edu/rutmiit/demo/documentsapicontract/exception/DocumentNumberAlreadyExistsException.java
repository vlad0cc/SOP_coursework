package edu.rutmiit.demo.documentsapicontract.exception;

public class DocumentNumberAlreadyExistsException extends RuntimeException {
    public DocumentNumberAlreadyExistsException(String documentNumber) {
        super("Document with number=" + documentNumber + " already exists");
    }
}
