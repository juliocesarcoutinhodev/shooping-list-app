package br.com.shooping.list.infrastructure.security;

import br.com.shooping.list.application.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Handler customizado para tratar erros de acesso negado (403 Forbidden).
 * Chamado quando um usuário autenticado tenta acessar um recurso sem permissão.
 * Retorna resposta JSON padronizada com detalhes do erro.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        log.warn("Acesso negado: {} {} - Usuário não possui permissões necessárias. Erro: {}",
                method, requestUri, accessDeniedException.getMessage());

        // Cria resposta de erro padronizada
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpServletResponse.SC_FORBIDDEN,
                "Forbidden",
                "Acesso negado. Você não possui permissões necessárias para acessar este recurso.",
                requestUri,
                null,
                null
        );

        // Configura resposta HTTP
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Serializa e escreve resposta JSON
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }
}

