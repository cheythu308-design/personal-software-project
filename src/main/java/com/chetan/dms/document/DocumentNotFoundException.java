package com.chetan.dms.document;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException(Long id) {
        super("Document with id " + id + " was not found");
    }
}
