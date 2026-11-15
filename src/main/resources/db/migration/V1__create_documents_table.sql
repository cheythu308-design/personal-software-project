CREATE TABLE documents (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    file_name VARCHAR(255),
    content_type VARCHAR(150),
    file_size_bytes BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT pk_documents PRIMARY KEY (id)
);
