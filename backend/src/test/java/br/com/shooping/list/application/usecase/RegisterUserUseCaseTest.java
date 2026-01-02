package br.com.shooping.list.application.usecase;

import br.com.shooping.list.application.dto.auth.RegisterRequest;
import br.com.shooping.list.application.dto.auth.RegisterResponse;
import br.com.shooping.list.domain.user.AuthProvider;
import br.com.shooping.list.domain.user.Role;
import br.com.shooping.list.domain.user.RoleRepository;
import br.com.shooping.list.domain.user.User;
import br.com.shooping.list.domain.user.UserRepository;
import br.com.shooping.list.domain.user.UserStatus;
import br.com.shooping.list.infrastructure.exception.EmailAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para RegisterUserUseCase
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterUserUseCase - Testes Unitários")
class RegisterUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUserUseCase registerUserUseCase;

    private RegisterRequest validRequest;
    private Role userRole;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest(
                "teste@email.com",
                "Teste User",
                "senha@123"
        );

        // Criar role USER para os testes
        userRole = Role.create("USER", "Usuário padrão");
        var idField = getFieldAndSetAccessible(Role.class, "id");
        setField(idField, userRole, 1L);
    }

    private java.lang.reflect.Field getFieldAndSetAccessible(Class<?> clazz, String fieldName) {
        try {
            var field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(java.lang.reflect.Field field, Object target, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Deve registrar usuário com sucesso quando email não existe")
    void shouldRegisterUserSuccessfully() {
        // Arrange
        String hashedPassword = "$2a$10$hashedPassword";
        when(userRepository.findByEmail(validRequest.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(validRequest.password())).thenReturn(hashedPassword);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));

        User savedUser = User.createLocalUser(
                validRequest.email(),
                validRequest.name(),
                hashedPassword
        );
        // Simulando ID gerado pelo banco
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            // Usando reflexão para setar o ID (já que não tem setter)
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, 1L);
            return user;
        });

        // Act
        RegisterResponse response = registerUserUseCase.execute(validRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo(validRequest.email());
        assertThat(response.name()).isEqualTo(validRequest.name());
        assertThat(response.provider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(response.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(response.createdAt()).isNotNull();

        // Verify
        verify(userRepository).findByEmail(validRequest.email());
        verify(passwordEncoder).encode(validRequest.password());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve fazer hash da senha antes de salvar")
    void shouldHashPasswordBeforeSaving() {
        // Arrange
        String hashedPassword = "$2a$10$differentHash";
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(validRequest.password())).thenReturn(hashedPassword);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        registerUserUseCase.execute(validRequest);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPasswordHash()).isEqualTo(hashedPassword);
        assertThat(savedUser.getPasswordHash()).isNotEqualTo(validRequest.password());
    }

    @Test
    @DisplayName("Deve lançar exceção quando email já existe")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Arrange
        User existingUser = User.createLocalUser(
                validRequest.email(),
                "Outro User",
                "hashedPassword"
        );
        when(userRepository.findByEmail(validRequest.email())).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThatThrownBy(() -> registerUserUseCase.execute(validRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining(validRequest.email());

        // Verify
        verify(userRepository).findByEmail(validRequest.email());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve criar usuário com provider LOCAL")
    void shouldCreateUserWithLocalProvider() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RegisterResponse response = registerUserUseCase.execute(validRequest);

        // Assert
        assertThat(response.provider()).isEqualTo(AuthProvider.LOCAL);
    }

    @Test
    @DisplayName("Deve criar usuário com status ACTIVE")
    void shouldCreateUserWithActiveStatus() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RegisterResponse response = registerUserUseCase.execute(validRequest);

        // Assert
        assertThat(response.status()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Resposta não deve expor senha ou hash")
    void responseShouldNotExposePassword() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            try {
                var idField = User.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(user, 1L);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return user;
        });

        // Act
        RegisterResponse response = registerUserUseCase.execute(validRequest);

        // Assert
        assertThat(response).hasNoNullFieldsOrProperties(); // Verifica que não há campo password/hash
        assertThat(response.toString()).doesNotContain("senha@123");
        assertThat(response.toString()).doesNotContain("hashedPassword");
    }
}

