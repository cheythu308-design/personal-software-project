package com.chetan.dms.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Validated payload used to create or replace document metadata. */
public record DocumentRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        @Size(max = 255) String fileName,
        @Size(max = 150) String contentType,
        @PositiveOrZero Long fileSizeBytes
) {
}
