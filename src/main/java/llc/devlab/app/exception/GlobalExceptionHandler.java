package llc.devlab.app.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler({ConflictException.class})
    ResponseEntity<ApiErrorResponse> handleConflict(RuntimeException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler({
            BadRequestException.class,
            MethodArgumentTypeMismatchException.class,
            ConstraintViolationException.class
    })
    ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
        String message = exception.getMessage();
        List<FieldValidationError> fieldErrors = List.of();
        if (exception instanceof MethodArgumentTypeMismatchException mismatch) {
            message = "Invalid value for parameter '%s'.".formatted(mismatch.getName());
        } else if (exception instanceof ConstraintViolationException violation) {
            message = "Validation failed.";
            fieldErrors = collectConstraintViolationErrors(violation);
        }
        return buildResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI(), fieldErrors);
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password.", request.getRequestURI(), List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "Access is denied.", request.getRequestURI(), List.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred.",
                request.getRequestURI(),
                List.of()
        );
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            WebRequest request
    ) {
        List<FieldValidationError> errors = collectValidationErrors(exception.getBindingResult());
        return new ResponseEntity<>(
                createBody(HttpStatus.BAD_REQUEST, "Validation failed.", request.getDescription(false), errors),
                headers,
                HttpStatus.BAD_REQUEST
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            String path,
            List<FieldValidationError> fieldErrors
    ) {
        return ResponseEntity.status(status).body(createBody(status, message, path, fieldErrors));
    }

    private ApiErrorResponse createBody(
            HttpStatus status,
            String message,
            String path,
            List<FieldValidationError> fieldErrors
    ) {
        String normalizedPath = path.startsWith("uri=") ? path.substring(4) : path;
        return new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                normalizedPath,
                fieldErrors
        );
    }

    private List<FieldValidationError> collectValidationErrors(org.springframework.validation.BindingResult bindingResult) {
        List<FieldValidationError> fieldErrors = bindingResult.getFieldErrors()
                .stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(error -> new FieldValidationError(error.getField(), error.getDefaultMessage()))
                .toList();

        List<FieldValidationError> globalErrors = bindingResult.getGlobalErrors()
                .stream()
                .map(error -> new FieldValidationError(error.getObjectName(), error.getDefaultMessage()))
                .toList();

        return java.util.stream.Stream.concat(fieldErrors.stream(), globalErrors.stream()).toList();
    }

    private List<FieldValidationError> collectConstraintViolationErrors(ConstraintViolationException exception) {
        return exception.getConstraintViolations()
                .stream()
                .sorted(Comparator.comparing(violation -> violation.getPropertyPath().toString()))
                .map(violation -> new FieldValidationError(extractFieldName(violation.getPropertyPath().toString()),
                        violation.getMessage()))
                .toList();
    }

    private String extractFieldName(String propertyPath) {
        int lastDot = propertyPath.lastIndexOf('.');
        return lastDot >= 0 ? propertyPath.substring(lastDot + 1) : propertyPath;
    }
}
