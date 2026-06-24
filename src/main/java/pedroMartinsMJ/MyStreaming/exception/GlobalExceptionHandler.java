package pedroMartinsMJ.MyStreaming.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import pedroMartinsMJ.MyStreaming.dto.ErrorResponseDTO;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Handler global de exceções.
 * Garante que erros retornem respostas estruturadas sem expor stack traces.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- Exceções customizadas do domínio ---

    @ExceptionHandler(VideoNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleVideoNotFound(VideoNotFoundException ex, HttpServletRequest request) {
        log.warn("Vídeo não encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponseDTO.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .error("VIDEO_NOT_FOUND")
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .timestamp(java.time.LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        log.warn("Usuário não encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponseDTO.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .error("USER_NOT_FOUND")
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .timestamp(java.time.LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(EncodingException.class)
    public ResponseEntity<ErrorResponseDTO> handleEncodingError(EncodingException ex, HttpServletRequest request) {
        log.error("Erro de encoding: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponseDTO.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error("ENCODING_ERROR")
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .timestamp(java.time.LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorResponseDTO> handleStorageError(StorageException ex, HttpServletRequest request) {
        log.error("Erro de armazenamento: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponseDTO.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error("STORAGE_ERROR")
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .timestamp(java.time.LocalDateTime.now())
                        .build());
    }

    // --- Exceções comuns ---

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Requisição inválida: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDTO.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("BAD_REQUEST")
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .timestamp(java.time.LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        log.warn("Estado inválido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDTO.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("ILLEGAL_STATE")
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .timestamp(java.time.LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDTO> handleMaxUploadSize(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        log.warn("Tamanho de upload excedido");
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ErrorResponseDTO.builder()
                        .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
                        .error("MAX_UPLOAD_SIZE_EXCEEDED")
                        .message("Arquivo excede o tamanho máximo permitido (5GB)")
                        .path(request.getRequestURI())
                        .timestamp(java.time.LocalDateTime.now())
                        .build());
    }

    // --- Fallback para qualquer exceção não tratada ---

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericError(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponseDTO.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error("INTERNAL_ERROR")
                        .message("Ocorreu um erro interno no servidor. Tente novamente mais tarde.")
                        .path(request.getRequestURI())
                        .timestamp(java.time.LocalDateTime.now())
                        .build());
    }
}