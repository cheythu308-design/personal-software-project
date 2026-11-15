package com.chetan.dms.document;

public class DocumentFileNotFoundException extends RuntimeException {
    public DocumentFileNotFoundException(Long id) {
        super("No uploaded file exists for document " + id);
    }
}
