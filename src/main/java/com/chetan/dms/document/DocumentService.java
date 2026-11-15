package com.chetan.dms.document;

import com.chetan.dms.storage.FileStorageService;
import com.chetan.dms.storage.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
public class DocumentService {

    private final DocumentRepository repository;
    private final FileStorageService fileStorageService;
    private final long maxFileSizeBytes;

    public DocumentService(DocumentRepository repository,
                           FileStorageService fileStorageService,
                           @Value("${dms.storage.max-file-size:10485760}") long maxFileSizeBytes) {
        this.repository = repository;
        this.fileStorageService = fileStorageService;
        this.maxFileSizeBytes = maxFileSizeBytes;
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

    public DocumentResponse upload(String title, String description, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("An empty file cannot be uploaded.");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new IllegalArgumentException("The uploaded file exceeds the maximum allowed size.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "document";
        }
        String safeFilename = StringUtils.cleanPath(originalFilename);
        if (safeFilename.contains("..") || safeFilename.length() > 255) {
            throw new IllegalArgumentException("Invalid file name.");
        }
        String resolvedTitle = (title == null || title.isBlank()) ? safeFilename : title.trim();
        if (resolvedTitle.length() > 200) {
            throw new IllegalArgumentException("Document title must not exceed 200 characters.");
        }
        String contentType = (file.getContentType() == null || file.getContentType().isBlank())
                ? "application/octet-stream" : file.getContentType();

        String storageKey;
        try {
            storageKey = fileStorageService.store(file.getInputStream(), safeFilename);
        } catch (IOException exception) {
            throw new FileStorageException("Unable to read uploaded file.", exception);
        }

        try {
            Document document = new Document(resolvedTitle, description, null, null, null);
            document.attachFile(safeFilename, contentType, file.getSize(), storageKey);
            return DocumentResponse.from(repository.save(document));
        } catch (RuntimeException exception) {
            fileStorageService.delete(storageKey);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public DocumentDownload download(Long id) {
        Document document = getDocument(id);
        if (document.getStorageKey() == null) {
            throw new DocumentFileNotFoundException(id);
        }
        Resource resource = fileStorageService.loadAsResource(document.getStorageKey());
        return new DocumentDownload(resource, document.getFileName(),
                document.getContentType(), document.getFileSizeBytes());
    }

    public void delete(Long id) {
        Document document = getDocument(id);
        repository.delete(document);
        fileStorageService.delete(document.getStorageKey());
    }

    private Document getDocument(Long id) {
        return repository.findById(id).orElseThrow(() -> new DocumentNotFoundException(id));
    }
}
