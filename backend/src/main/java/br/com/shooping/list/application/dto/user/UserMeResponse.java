package br.com.shooping.list.application.dto.user;

import br.com.shooping.list.domain.user.AuthProvider;
import br.com.shooping.list.domain.user.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO de resposta com dados do usuário autenticado.
 * Retornado pelo endpoint GET /api/v1/users/me
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMeResponse {

    /**
     * ID único do usuário
     */
    private Long id;

    /**
     * Email do usuário
     */
    private String email;

    /**
     * Nome completo do usuário
     */
    private String name;

    /**
     * Provedor de autenticação (LOCAL ou GOOGLE)
     */
    private AuthProvider provider;

    /**
     * Status da conta (ACTIVE ou DISABLED)
     */
    private UserStatus status;

    /**
     * Data de criação da conta
     */
    private Instant createdAt;

    /**
     * Data da última atualização
     */
    private Instant updatedAt;
}

