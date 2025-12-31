package br.com.shooping.list.test.support;

import br.com.shooping.list.domain.user.Role;
import br.com.shooping.list.domain.user.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Classe auxiliar para setup de dados nos testes de integração.
 * Garante que roles padrão existam no banco H2 antes dos testes.
 */
@Component
public class TestDataSetup {

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Cria roles padrão no banco se não existirem.
     * Deve ser chamado no @BeforeEach dos testes de integração.
     */
    public void createDefaultRoles() {
        if (!roleRepository.existsByName("USER")) {
            Role userRole = Role.create("USER", "Usuário padrão com permissões básicas");
            roleRepository.save(userRole);
        }

        if (!roleRepository.existsByName("ADMIN")) {
            Role adminRole = Role.create("ADMIN", "Administrador com permissões completas");
            roleRepository.save(adminRole);
        }
    }
}

