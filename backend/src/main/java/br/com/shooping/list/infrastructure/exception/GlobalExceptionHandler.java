package br.com.shooping.list.infrastructure.exception;

import br.com.shooping.list.application.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handler global de exceções da aplicação.
 * <p>
 * Intercepta todas as exceções lançadas pelos controllers e retorna
 * respostas padronizadas usando {@link ErrorResponse}.
 * <p>
 * Segue os princípios SOLID:
 * - Single Responsibility: Apenas trata exceções
 * - Open/Closed: Fácil adicionar novos handlers sem modificar existentes
 * - Liskov Substitution: Todas as exceções retornam ResponseEntity<ErrorResponse>
 * - Interface Segregation: Cada handler trata apenas seu tipo de exceção
 * - Dependency Inversion: Depende de abstrações (Exception, HttpServletRequest)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata erros de validação (@Valid em DTOs).
     * <p>
     * Retorna 400 Bad Request com detalhes dos campos inválidos.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        log.warn("Validation error on path: {}", request.getRequestURI(), ex);

        List<ErrorResponse.ValidationError> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapFieldError)
                .collect(Collectors.toList());

        var error = ErrorResponse.withDetails(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Erro de validação. Verifique os campos enviados.",
                request.getRequestURI(),
                details
        );

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Trata erros de JSON malformado.
     * <p>
     * Retorna 400 Bad Request.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        log.warn("Malformed JSON on path: {}", request.getRequestURI(), ex);

        var error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "JSON malformado. Verifique a sintaxe do corpo da requisição.",
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Trata erros de autenticação.
     * <p>
     * Retorna 401 Unauthorized.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        log.warn("Authentication error on path: {}", request.getRequestURI(), ex);

        var error = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Autenticação requerida. Por favor, forneça um token JWT válido.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Trata erros de token JWT expirado.
     * <p>
     * Retorna 401 Unauthorized.
     */
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwtException(
            ExpiredJwtException ex,
            HttpServletRequest request
    ) {
        log.warn("Expired JWT token on path: {}", request.getRequestURI());

        var error = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Token JWT expirado. Por favor, faça login novamente ou renove seu token.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Trata erros de token JWT inválido.
     * <p>
     * Retorna 401 Unauthorized.
     */
    @ExceptionHandler(InvalidJwtException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJwtException(
            InvalidJwtException ex,
            HttpServletRequest request
    ) {
        log.warn("Invalid JWT token on path: {}", request.getRequestURI());

        var error = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Token JWT inválido. Por favor, forneça um token válido.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Trata erros de permissão (usuário autenticado mas sem acesso).
     * <p>
     * Retorna 403 Forbidden.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        log.warn("Access denied on path: {}", request.getRequestURI(), ex);

        var error = ErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "Acesso negado. Você não tem permissão para acessar este recurso.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Trata erros de recurso não encontrado (404).
     * <p>
     * Inclui NoHandlerFoundException e NoResourceFoundException.
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.debug("Resource not found on path: {}", request.getRequestURI());

        var error = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                "Recurso não encontrado.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Trata erros de argumento ilegal (lógica de negócio).
     * <p>
     * Retorna 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        log.warn("Illegal argument on path: {}", request.getRequestURI(), ex);

        var error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Trata erros de estado ilegal (lógica de negócio).
     * <p>
     * Retorna 409 Conflict.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex,
            HttpServletRequest request
    ) {
        log.warn("Illegal state on path: {}", request.getRequestURI(), ex);

        var error = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Trata erros de email já cadastrado (lógica de negócio).
     * <p>
     * Retorna 409 Conflict.
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(
            EmailAlreadyExistsException ex,
            HttpServletRequest request
    ) {
        log.warn("Email already exists on path: {}", request.getRequestURI());

        var error = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Trata erros de credenciais inválidas (login).
     * <p>
     * Retorna 401 Unauthorized.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(
            InvalidCredentialsException ex,
            HttpServletRequest request
    ) {
        log.warn("Invalid credentials attempt on path: {}", request.getRequestURI());

        var error = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Trata erros de refresh token inválido (expirado, revogado, não encontrado).
     * <p>
     * Retorna 401 Unauthorized.
     */
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshTokenException(
            InvalidRefreshTokenException ex,
            HttpServletRequest request
    ) {
        log.warn("Invalid refresh token attempt on path: {}", request.getRequestURI());

        var error = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Trata erros de lista de compras não encontrada.
     * <p>
     * Retorna 404 Not Found.
     */
    @ExceptionHandler(ShoppingListNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleShoppingListNotFoundException(
            ShoppingListNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Shopping list not found on path: {}", request.getRequestURI());

        var error = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Trata erros de acesso não autorizado a lista de compras.
     * <p>
     * Retorna 403 Forbidden.
     */
    @ExceptionHandler(UnauthorizedShoppingListAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedShoppingListAccessException(
            UnauthorizedShoppingListAccessException ex,
            HttpServletRequest request
    ) {
        log.warn("Unauthorized shopping list access on path: {}", request.getRequestURI());

        var error = ErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Trata erros de validação de token do Google OAuth2.
     * <p>
     * Retorna 401 Unauthorized.
     */
    @ExceptionHandler(br.com.shooping.list.infrastructure.security.GoogleTokenValidationException.class)
    public ResponseEntity<ErrorResponse> handleGoogleTokenValidationException(
            br.com.shooping.list.infrastructure.security.GoogleTokenValidationException ex,
            HttpServletRequest request
    ) {
        log.warn("Google token validation error on path: {}", request.getRequestURI());

        var error = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Token do Google inválido: " + ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Trata erros de item não encontrado.
     *
     * @return 404 Not Found
     */
    @ExceptionHandler(br.com.shooping.list.domain.shoppinglist.ItemNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleItemNotFoundException(
            br.com.shooping.list.domain.shoppinglist.ItemNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Item not found on path: {}", request.getRequestURI());

        var error = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Trata erros de item duplicado.
     *
     * @return 400 Bad Request
     */
    @ExceptionHandler(br.com.shooping.list.domain.shoppinglist.DuplicateItemException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateItemException(
            br.com.shooping.list.domain.shoppinglist.DuplicateItemException ex,
            HttpServletRequest request
    ) {
        log.warn("Duplicate item on path: {}", request.getRequestURI());

        var error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Trata erros de limite de itens excedido.
     *
     * @return 400 Bad Request
     */
    @ExceptionHandler(br.com.shooping.list.domain.shoppinglist.ListLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleListLimitExceededException(
            br.com.shooping.list.domain.shoppinglist.ListLimitExceededException ex,
            HttpServletRequest request
    ) {
        log.warn("List limit exceeded on path: {}", request.getRequestURI());

        var error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Fallback para exceções não tratadas.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unexpected error on path: {}", request.getRequestURI(), ex);

        var error = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Erro interno do servidor. Por favor, tente novamente mais tarde.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Mapeia FieldError para ValidationError.
     */
    private ErrorResponse.ValidationError mapFieldError(FieldError fieldError) {
        return ErrorResponse.ValidationError.builder()
                .field(fieldError.getField())
                .message(fieldError.getDefaultMessage())
                .rejectedValue(fieldError.getRejectedValue())
                .build();
    }
}

