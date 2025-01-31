package brack.bernardo.simplify_tech_desafio.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.charset.MalformedInputException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(MalformedInputException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedInputException(MalformedInputException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .path(request.getRequestURI())
                        .message(ex.getMessage())
                        .timestamp(getFormattedOffsetDateTimeNow())
                        .build()
        );
    }

    @ExceptionHandler(ContentNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleContentNotFoundException(ContentNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
          ApiErrorResponse.builder()
                  .status(HttpStatus.NOT_FOUND.value())
                  .path(request.getRequestURI())
                  .message(ex.getMessage())
                  .timestamp(getFormattedOffsetDateTimeNow())
                  .build()
        );
    }

    @ExceptionHandler(MalformedContentException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedContentException(MalformedContentException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .path(request.getRequestURI())
                        .message(ex.getMessage())
                        .timestamp(getFormattedOffsetDateTimeNow())
                        .build()
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
          ApiErrorResponse.builder()
                  .status(HttpStatus.NOT_FOUND.value())
                  .path(request.getRequestURI())
                  .message(ex.getMessage())
                  .timestamp(getFormattedOffsetDateTimeNow())
                  .build()
        );
    }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
                ApiErrorResponse.builder()
                        .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                        .path(request.getRequestURI())
                        .message(ex.getMessage())
                        .timestamp(getFormattedOffsetDateTimeNow())
                        .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .path(request.getRequestURI())
                        .message(
                                Arrays.stream(ex.getDetailMessageArguments())
                                        .map(Object::toString)
                                        .filter(str -> !str.trim().isEmpty())
                                        .sorted(Comparator.comparing(s -> s, String.CASE_INSENSITIVE_ORDER))
                                        .collect(Collectors.joining(", ")))
                        .timestamp(getFormattedOffsetDateTimeNow())
                        .build());
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        var requiredTypeName = (Objects.isNull(ex.getRequiredType())) ? "UNKNOWN" : ex.getRequiredType().getSimpleName();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .path(request.getRequestURI())
                        .timestamp(getFormattedOffsetDateTimeNow())
                        .message(ex.getPropertyName() + " should be of type " + requiredTypeName + "; Value encountered: " + ex.getValue())
                        .build()
        );

    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableExcepiton(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .path(request.getRequestURI())
                        .message(ex.getMessage())
                        .timestamp(getFormattedOffsetDateTimeNow())
                        .build()
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(
                ApiErrorResponse.builder()
                        .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                        .path(request.getRequestURI())
                        .message(ex.getMessage())
                        .timestamp(getFormattedOffsetDateTimeNow())
                        .build()
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<DefaultErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(new DefaultErrorResponse(ex.getStatusCode().value(), ex.getMessage()));
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<DefaultErrorResponse> handleException(Exception ex) {
        return ResponseEntity
                .internalServerError()
                .body(new DefaultErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error has occurred."));
    }

    private String getFormattedOffsetDateTimeNow() {
        return OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

}
