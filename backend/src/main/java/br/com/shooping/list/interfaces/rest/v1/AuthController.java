package br.com.shooping.list.interfaces.rest.v1;

import br.com.shooping.list.application.dto.auth.GoogleLoginRequest;
import br.com.shooping.list.application.dto.auth.LoginRequest;
import br.com.shooping.list.application.dto.auth.LoginResponse;
import br.com.shooping.list.application.dto.auth.LogoutRequest;
import br.com.shooping.list.application.dto.auth.RefreshTokenRequest;
import br.com.shooping.list.application.dto.auth.RefreshTokenResponse;
import br.com.shooping.list.application.dto.auth.RegisterRequest;
import br.com.shooping.list.application.dto.auth.RegisterResponse;
import br.com.shooping.list.application.usecase.GoogleLoginUseCase;
import br.com.shooping.list.application.usecase.LoginUserUseCase;
import br.com.shooping.list.application.usecase.LogoutUseCase;
import br.com.shooping.list.application.usecase.RefreshTokenUseCase;
import br.com.shooping.list.application.usecase.RegisterUserUseCase;
import br.com.shooping.list.infrastructure.security.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para operações de autenticação
 * Base path: /api/v1/auth
 *
 * Suporta refresh token via cookie HttpOnly (seguro) e body (dev/test)
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final GoogleLoginUseCase googleLoginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final CookieService cookieService;

    /**
     * Endpoint para registro de novo usuário LOCAL
     *
     * @param request dados do usuário (email, nome, senha)
     * @return usuário criado (sem dados sensíveis)
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Requisição de registro recebida para email: {}", request.getEmail());

        var response = registerUserUseCase.execute(request);

        log.info("Usuário registrado com sucesso: id={}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint para login de usuário LOCAL
     *
     * @param request dados de login (email, senha)
     * @param httpRequest requisição HTTP para extrair metadata
     * @param httpResponse resposta HTTP para adicionar cookie
     * @return tokens de acesso e refresh (refresh também vai no cookie)
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        log.info("Requisição de login recebida para email: {}", request.getEmail());

        String userAgent = httpRequest.getHeader("User-Agent");
        String ip = extractClientIp(httpRequest);

        var response = loginUserUseCase.execute(request, userAgent, ip);

        // Adiciona refresh token no cookie HttpOnly
        cookieService.addRefreshTokenCookie(httpResponse, response.getRefreshToken());

        // Se cookie-only está ativado, remove refresh token do body (mais seguro)
        if (cookieService.isCookieOnly()) {
            response = new LoginResponse(
                    response.getAccessToken(),
                    null, // refresh token só no cookie
                    response.getExpiresIn()
            );
            log.debug("Modo cookie-only ativado: refresh token removido do body");
        }

        log.info("Login realizado com sucesso para email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para login via Google OAuth2
     *
     * @param request ID Token emitido pelo Google
     * @param httpRequest requisição HTTP para extrair metadata
     * @param httpResponse resposta HTTP para adicionar cookie
     * @return tokens de acesso e refresh (refresh também vai no cookie)
     */
    @PostMapping("/google")
    public ResponseEntity<LoginResponse> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        log.info("Requisição de login via Google OAuth2 recebida");

        var response = googleLoginUseCase.execute(request.getIdToken(), httpRequest);

        // Adiciona refresh token no cookie HttpOnly
        cookieService.addRefreshTokenCookie(httpResponse, response.getRefreshToken());

        log.info("Login via Google realizado com sucesso");
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para renovar access token usando refresh token
     *
     *
     * @param request refresh token a ser validado e rotacionado (opcional se vier no cookie)
     * @param httpRequest requisição HTTP para extrair metadata e cookie
     * @param httpResponse resposta HTTP para adicionar novo cookie
     * @return novo access token e novo refresh token (refresh também vai no cookie)
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        log.info("Requisição de refresh token recebida");

        // Prioriza cookie, mas aceita body se não houver cookie (backward compatibility)
        String refreshToken = cookieService.getRefreshTokenFromCookie(httpRequest)
                .orElseGet(() -> {
                    if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
                        log.warn("Refresh token não encontrado nem no cookie nem no body");
                        throw new IllegalArgumentException("Refresh token é obrigatório");
                    }
                    log.debug("Usando refresh token do body (cookie não encontrado)");
                    return request.getRefreshToken();
                });

        String userAgent = httpRequest.getHeader("User-Agent");
        String ip = extractClientIp(httpRequest);

        var refreshRequest = new RefreshTokenRequest(refreshToken);
        var response = refreshTokenUseCase.execute(refreshRequest, userAgent, ip);

        // Adiciona novo refresh token no cookie HttpOnly (rotação)
        cookieService.addRefreshTokenCookie(httpResponse, response.getRefreshToken());

        // Se cookie-only está ativado, remove refresh token do body (mais seguro)
        if (cookieService.isCookieOnly()) {
            response = new RefreshTokenResponse(
                    response.getAccessToken(),
                    null, // refresh token só no cookie
                    response.getExpiresIn()
            );
            log.debug("Modo cookie-only ativado: refresh token removido do body");
        }

        log.info("Refresh token rotacionado com sucesso");
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para logout (revogação de refresh token)
     * Aceita refresh token via cookie (preferencial) ou body (dev/test)
     *
     * @param request refresh token a ser revogado (opcional se vier no cookie)
     * @param httpRequest requisição HTTP para extrair cookie
     * @param httpResponse resposta HTTP para remover cookie
     * @return 204 No Content
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody(required = false) LogoutRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        log.info("Requisição de logout recebida");

        // Prioriza cookie, mas aceita body se não houver cookie (backward compatibility)
        String refreshToken = cookieService.getRefreshTokenFromCookie(httpRequest)
                .orElseGet(() -> {
                    if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
                        log.warn("Refresh token não encontrado nem no cookie nem no body");
                        throw new IllegalArgumentException("Refresh token é obrigatório");
                    }
                    log.debug("Usando refresh token do body (cookie não encontrado)");
                    return request.getRefreshToken();
                });

        var logoutRequest = new LogoutRequest(refreshToken);
        logoutUseCase.execute(logoutRequest);

        // Remove cookie do navegador
        cookieService.clearRefreshTokenCookie(httpResponse);

        log.info("Logout realizado com sucesso");
        return ResponseEntity.noContent().build();
    }

    /**
     * Extrai o IP real do cliente, considerando proxies e load balancers
     */
    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Se houver múltiplos IPs (proxy chain), pega o primeiro
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
