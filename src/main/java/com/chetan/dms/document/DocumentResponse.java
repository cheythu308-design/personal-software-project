package com.chetan.dms.document;

import java.time.Instant;

public record DocumentResponse(
        Long id,
        String title,
        String description,
        String fileName,
        String contentType,
        Long fileSizeBytes,
        Instant createdAt,
        Instant updatedAt
) {
    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getTitle(),
                document.getDescription(),
                document.getFileName(),
                document.getContentType(),
                document.getFileSizeBytes(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
