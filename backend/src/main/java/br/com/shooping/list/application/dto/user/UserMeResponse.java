package br.com.shooping.list.application.dto.user;

import br.com.shooping.list.domain.user.AuthProvider;
import br.com.shooping.list.domain.user.UserStatus;

import java.time.Instant;

/**
 * DTO de resposta com dados do usuário autenticado.
 * Retornado pelo endpoint GET /api/v1/users/me
 */
public record UserMeResponse(
        /**
         * ID único do usuário
         */
        Long id,

        /**
         * Email do usuário
         */
        String email,

        /**
         * Nome completo do usuário
         */
        String name,

        /**
         * Provedor de autenticação (LOCAL ou GOOGLE)
         */
        AuthProvider provider,

        /**
         * Status da conta (ACTIVE ou DISABLED)
         */
        UserStatus status,

        /**
         * Data de criação da conta
         */
        Instant createdAt,

        /**
         * Data da última atualização
         */
        Instant updatedAt
) {}

