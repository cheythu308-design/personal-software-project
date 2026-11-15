CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(254) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    user_role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (user_role IN ('USER', 'ADMIN'))
);

ALTER TABLE documents
    ADD COLUMN owner_id BIGINT NULL;

ALTER TABLE documents
    ADD CONSTRAINT fk_documents_owner
        FOREIGN KEY (owner_id) REFERENCES users (id);

CREATE INDEX idx_documents_owner_id ON documents (owner_id);
