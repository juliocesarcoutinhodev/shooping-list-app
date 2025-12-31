package br.com.shooping.list.application.usecase;

import br.com.shooping.list.application.dto.auth.RegisterRequest;
import br.com.shooping.list.application.dto.auth.RegisterResponse;
import br.com.shooping.list.domain.user.Role;
import br.com.shooping.list.domain.user.RoleRepository;
import br.com.shooping.list.domain.user.User;
import br.com.shooping.list.domain.user.UserRepository;
import br.com.shooping.list.infrastructure.exception.EmailAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: Registrar novo usuário LOCAL
 *
 * Responsabilidades:
 * - Validar email único
 * - Fazer hash da senha
 * - Criar usuário no domínio
 * - Atribuir role USER padrão
 * - Persistir via repositório
 * - Retornar resposta sem dados sensíveis
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterResponse execute(RegisterRequest request) {
        log.info("Iniciando registro de usuário: email={}", request.getEmail());

        // Validar email único
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Tentativa de registro com email duplicado: {}", request.getEmail());
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // Hash da senha
        String passwordHash = passwordEncoder.encode(request.getPassword());
        log.debug("Senha hashada com sucesso para email={}", request.getEmail());

        // Criar usuário no domínio
        User user = User.createLocalUser(
                request.getEmail(),
                request.getName(),
                passwordHash
        );

        // Buscar e atribuir role USER padrão
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> {
                    log.error("Role USER não encontrada no banco. Execute as migrations Flyway.");
                    return new IllegalStateException("Role USER não encontrada. Sistema mal configurado.");
                });

        user.addRole(userRole);
        log.debug("Role USER atribuída ao usuário: email={}", request.getEmail());

        // Persistir
        var savedUser = userRepository.save(user);
        log.info("Usuário registrado com sucesso: id={}, email={}", savedUser.getId(), savedUser.getEmail());

        // Mapear para resposta (sem dados sensíveis)
        return RegisterResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .provider(savedUser.getProvider())
                .status(savedUser.getStatus())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }
}

