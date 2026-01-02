package br.com.shooping.list.interfaces.rest.v1;

import br.com.shooping.list.application.dto.user.UserMeResponse;
import br.com.shooping.list.application.usecase.GetCurrentUserUseCase;
import br.com.shooping.list.interfaces.rest.v1.docs.UserAPI;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para operações relacionadas ao usuário autenticado.
 * Base path: /api/v1/users
 * Todos os endpoints deste controller requerem autenticação JWT.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController implements UserAPI {

    private final GetCurrentUserUseCase getCurrentUserUseCase;

    /**
     * Retorna os dados do usuário autenticado.
     *
     * @return dados do usuário extraídos do JWT
     */
    @GetMapping("/me")
    @Override
    public ResponseEntity<UserMeResponse> getCurrentUser() {
        log.info("Requisição GET /api/v1/users/me recebida");

        // Extrai o userId do SecurityContext (colocado pelo JwtAuthenticationFilter)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal();

        log.debug("Extraindo dados do usuário: userId={}", userId);

        var response = getCurrentUserUseCase.execute(Long.parseLong(userId));

        log.info("Dados do usuário retornados com sucesso: userId={}", userId);
        return ResponseEntity.ok(response);
    }
}

