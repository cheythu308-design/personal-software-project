package com.chetan.dms.document;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DocumentService {

    private final DocumentRepository repository;

    public DocumentService(DocumentRepository repository) {
        this.repository = repository;
    }

    public DocumentResponse create(DocumentRequest request) {
        Document document = new Document(
                request.title().trim(),
                request.description(),
                request.fileName(),
                request.contentType(),
                request.fileSizeBytes()
        );
        return DocumentResponse.from(repository.save(document));
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> findAll() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentResponse findById(Long id) {
        return DocumentResponse.from(getDocument(id));
    }

    public DocumentResponse update(Long id, DocumentRequest request) {
        Document document = getDocument(id);
        document.update(
                request.title().trim(),
                request.description(),
                request.fileName(),
                request.contentType(),
                request.fileSizeBytes()
        );
        return DocumentResponse.from(repository.save(document));
    }

    public void delete(Long id) {
        repository.delete(getDocument(id));
    }

    private Document getDocument(Long id) {
        return repository.findById(id).orElseThrow(() -> new DocumentNotFoundException(id));
    }
}
