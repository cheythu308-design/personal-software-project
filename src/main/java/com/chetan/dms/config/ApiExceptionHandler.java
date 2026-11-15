package com.chetan.dms.config;

import com.chetan.dms.document.DocumentNotFoundException;
import com.chetan.dms.auth.EmailAlreadyRegisteredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import com.chetan.dms.document.DocumentFileNotFoundException;
import com.chetan.dms.storage.FileStorageException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {



    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, "You do not have permission to perform this action.");
        problem.setTitle("Access denied");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateEmail(EmailAlreadyRegisteredException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setTitle("Email already registered");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentials(BadCredentialsException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, "The email or password is incorrect.");
        problem.setTitle("Authentication failed");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(DocumentNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Document not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }


    @ExceptionHandler(DocumentFileNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleFileNotFound(DocumentFileNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Document file not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleBadRequest(IllegalArgumentException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Invalid request");
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> handleUploadTooLarge(MaxUploadSizeExceededException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.PAYLOAD_TOO_LARGE, "The uploaded file exceeds the configured size limit.");
        problem.setTitle("File too large");
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(problem);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ProblemDetail> handleStorageFailure(FileStorageException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "The document could not be read or stored.");
        problem.setTitle("File storage failure");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "One or more request fields are invalid.");
        problem.setTitle("Validation failed");

        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        problem.setProperty("errors", errors);

        return ResponseEntity.badRequest().body(problem);
    }
}
