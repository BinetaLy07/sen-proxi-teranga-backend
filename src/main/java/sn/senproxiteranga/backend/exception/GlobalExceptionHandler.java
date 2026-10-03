package sn.senproxiteranga.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 : élément introuvable
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex,
                                                   HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    // 400 : règle métier non respectée
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex,
                                                   HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    // 400 : champs du formulaire invalides (@NotBlank, @Size...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                     HttpServletRequest request) {
        Map<String, String> erreurs = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> erreurs.put(e.getField(), e.getDefaultMessage()));
        return build(HttpStatus.BAD_REQUEST, "Données invalides", request, erreurs);
    }

    // 400 : JSON mal écrit, champ dans un mauvais format (date, nombre, statut...)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleJsonInvalide(HttpMessageNotReadableException ex,
                                                       HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "Requête invalide : vérifiez le format des données envoyées (dates, nombres, valeurs autorisées)",
                request, null);
    }

    // 413 : fichier plus gros que la limite fixée dans application.yaml (20 Mo)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleFichierTropGros(MaxUploadSizeExceededException ex,
                                                          HttpServletRequest request) {
        return build(HttpStatus.CONTENT_TOO_LARGE,
                "Fichier trop volumineux : 20 Mo maximum par fichier",
                request, null);
    }

    // 400 : envoi sans le champ "fichiers"
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiError> handleFichierManquant(MissingServletRequestPartException ex,
                                                          HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "Aucun fichier envoyé : le champ '" + ex.getRequestPartName() + "' est obligatoire",
                request, null);
    }

    // Méthode commune : fabrique la réponse d'erreur
    private ResponseEntity<ApiError> build(HttpStatus status, String message,
                                           HttpServletRequest request,
                                           Map<String, String> fieldErrors) {
        ApiError body = new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(status).body(body);
    }
}