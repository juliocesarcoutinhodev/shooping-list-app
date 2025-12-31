package br.com.shooping.list.infrastructure.http;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro para adicionar Correlation ID em todas as requisições.
 * <p>
 * O Correlation ID permite rastrear uma requisição através de todos os logs
 * da aplicação, facilitando debugging e auditoria em produção.
 * <p>
 * Funcionalidade:
 * - Extrai correlation-id do header X-Correlation-Id (se enviado pelo cliente)
 * - Gera novo UUID se não houver correlation-id
 * - Adiciona ao MDC (Mapped Diagnostic Context) do Logback
 * - Adiciona ao response header para o cliente
 * - Todos os logs da requisição incluem automaticamente o correlation-id
 * <p>
 * Prioridade: HIGHEST para executar antes de qualquer outro filtro
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Extrai correlation-id do header ou gera um novo
        String correlationId = extractOrGenerateCorrelationId(request);

        // Adiciona ao MDC (Mapped Diagnostic Context) para logs
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);

        // Adiciona ao response header para o cliente poder rastrear
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            // Continua a cadeia de filtros
            filterChain.doFilter(request, response);
        } finally {
            // IMPORTANTE: Limpa o MDC após a requisição para evitar memory leak
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }

    /**
     * Extrai correlation-id do header da requisição ou gera um novo UUID.
     */
    private String extractOrGenerateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
            log.debug("Correlation ID gerado: {}", correlationId);
        } else {
            log.debug("Correlation ID recebido do cliente: {}", correlationId);
        }

        return correlationId;
    }
}

