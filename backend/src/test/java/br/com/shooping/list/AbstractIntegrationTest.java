package br.com.shooping.list;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Classe base para testes de integração com Testcontainers.
 * <p>
 * Configura um container MySQL real para testes end-to-end,
 * garantindo que os testes reflitam o comportamento real em produção.
 * <p>
 * Benefícios:
 * - Testa com banco MySQL real (não H2)
 * - Container isolado para cada execução de teste
 * - Flyway executa migrations automaticamente
 * - Schema real validado
 * - Constraints e índices testados
 * <p>
 * Uso:
 * <pre>
 * &#64;SpringBootTest
 * class MeuTesteIntegracao extends AbstractIntegrationTest {
 *     // Testes aqui
 * }
 * </pre>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@Testcontainers
public abstract class AbstractIntegrationTest {

    /**
     * Container MySQL compartilhado entre todos os testes.
     * <p>
     * Configurações:
     * - MySQL 9 (mesma versão de produção)
     * - Database: testdb
     * - User/Password: test/test
     * - Reusável (não recria a cada teste)
     */
    @Container
    protected static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:9")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    /**
     * Configura propriedades dinâmicas do Spring para apontar para o container.
     * <p>
     * Sobrescreve as propriedades do application-integration-test.yml
     * com os valores dinâmicos do container (porta, host, etc).
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }
}

