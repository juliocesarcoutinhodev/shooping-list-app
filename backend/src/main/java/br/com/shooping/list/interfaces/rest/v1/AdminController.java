package br.com.shooping.list.interfaces.rest.v1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Controller REST para operações administrativas.
 * Base path: /api/v1/admin
 * Todos os endpoints deste controller requerem autenticação JWT e role ADMIN.
 * A autorização é aplicada via SecurityFilterChain (configuração centralizada).
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    /**
     * Endpoint de teste para validar autorização ADMIN.
     * Usado para validar que apenas usuários com role ADMIN podem acessar rotas administrativas.
     * Retorna informações básicas do usuário autenticado e timestamp.
     *
     * @return pong com informações do admin autenticado
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        log.info("Requisição GET /api/v1/admin/ping recebida");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal();
        var authorities = authentication.getAuthorities();

        Map<String, Object> response = Map.of(
                "message", "pong",
                "userId", userId,
                "authorities", authorities.stream().map(Object::toString).toList(),
                "timestamp", Instant.now()
        );

        log.info("Admin ping bem-sucedido: userId={}, authorities={}", userId, authorities);
        return ResponseEntity.ok(response);
    }
}

