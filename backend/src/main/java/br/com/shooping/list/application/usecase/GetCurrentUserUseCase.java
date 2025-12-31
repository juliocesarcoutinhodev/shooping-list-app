package br.com.shooping.list.application.usecase;

import br.com.shooping.list.application.dto.user.UserMeResponse;
import br.com.shooping.list.domain.user.User;
import br.com.shooping.list.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para obter dados do usuário autenticado.
 * <p>
 * Endpoint: GET /api/v1/users/me
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetCurrentUserUseCase {

    private final UserRepository userRepository;

    /**
     * Busca os dados do usuário autenticado pelo ID extraído do JWT.
     *
     * @param userId ID do usuário (extraído do token JWT)
     * @return dados do usuário
     * @throws IllegalArgumentException se usuário não for encontrado
     */
    @Transactional(readOnly = true)
    public UserMeResponse execute(Long userId) {
        log.info("Buscando dados do usuário: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado: userId={}", userId);
                    return new IllegalArgumentException("Usuário não encontrado");
                });

        log.debug("Usuário encontrado: email={}, provider={}", user.getEmail(), user.getProvider());

        return UserMeResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .provider(user.getProvider())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

