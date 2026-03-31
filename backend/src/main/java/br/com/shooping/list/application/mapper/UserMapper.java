package br.com.shooping.list.application.mapper;

import br.com.shooping.list.application.dto.auth.RegisterResponse;
import br.com.shooping.list.application.dto.user.UserMeResponse;
import br.com.shooping.list.domain.user.User;
import org.mapstruct.Mapper;

/**
 * Mapper MapStruct para conversão entre User (domínio) e DTOs de resposta.
 * <p>
 * Responsabilidades:
 * - Mapear User → UserMeResponse (dados do usuário autenticado)
 * - Mapear User → RegisterResponse (resposta de registro)
 * <p>
 * Configuração:
 * - componentModel = "spring" - gera bean Spring injetável
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Mapeia User para UserMeResponse.
     * Usado em: GetCurrentUserUseCase (GET /api/v1/users/me).
     * <p>
     * Retorna dados completos do usuário autenticado:
     * - id, email, name, provider, status, createdAt, updatedAt
     * <p>
     * Não expõe dados sensíveis (passwordHash não está no DTO).
     *
     * @param user entidade de domínio
     * @return DTO com dados do usuário
     */
    UserMeResponse toUserMeResponse(User user);

    /**
     * Mapeia User para RegisterResponse.
     * Usado em: RegisterUserUseCase (POST /api/v1/auth/register).
     * <p>
     * Retorna dados do usuário recém-registrado:
     * - id, email, name, provider, status, createdAt
     * <p>
     * Não expõe dados sensíveis (passwordHash não está no DTO).
     *
     * @param user entidade de domínio
     * @return DTO de resposta de registro
     */
    RegisterResponse toRegisterResponse(User user);
}

