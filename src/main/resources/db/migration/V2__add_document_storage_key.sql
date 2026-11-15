ALTER TABLE documents
    ADD COLUMN storage_key VARCHAR(100) NULL,
    ADD CONSTRAINT uk_documents_storage_key UNIQUE (storage_key);
