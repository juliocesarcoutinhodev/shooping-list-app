# 🧪 Testes de Integração com Testcontainers

## 📋 Visão Geral

Este projeto possui dois níveis de testes de integração:

1. **Testes Rápidos com H2** (padrão) - Executam automaticamente em qualquer ambiente
2. **Testes com MySQL Real via Testcontainers** (opcional) - Requerem Docker rodando

## 🚀 Testes com H2 (Padrão - Sempre Disponíveis)

### Características
- ✅ **Rápidos**: ~15 segundos para executar todos os testes
- ✅ **Sem dependências**: Não requer Docker ou MySQL
- ✅ **CI/CD friendly**: Funciona em GitHub Actions, GitLab CI, etc.
- ✅ **Modo MySQL**: H2 configurado com compatibilidade MySQL
- ✅ **Isolamento**: Schema recriado a cada execução

### Como Executar
```bash
# Todos os testes
./mvnw test

# Testes específicos
./mvnw test -Dtest=AuthControllerTest
```

### Perfil Usado
- **Profile**: `test`
- **Banco**: H2 em memória (`jdbc:h2:mem:testdb`)
- **Hibernate**: `ddl-auto: create-drop`
- **Flyway**: Desabilitado (Hibernate gerencia schema)

---

## 🐳 Testes com Testcontainers (MySQL Real)

### Características
- ✅ **Banco Real**: Usa MySQL 9 (mesma versão de produção)
- ✅ **Validação Completa**: Testa migrations, constraints, índices reais
- ✅ **Flyway Ativo**: Valida que migrations funcionam corretamente
- ✅ **Isolamento**: Container MySQL isolado por execução
- ⚠️ **Requer Docker**: Docker deve estar rodando localmente

### Pré-requisitos

1. **Docker instalado e rodando:**
   ```bash
   docker --version
   # Docker version 24.0.0 ou superior
   
   docker ps
   # Deve retornar lista de containers (mesmo que vazia)
   ```

2. **Testcontainers configurado:**
   - Dependências já estão no `pom.xml`
   - Imagem MySQL será baixada automaticamente na primeira execução

### Como Executar

```bash
# Apenas testes de integração end-to-end com Testcontainers
./mvnw test -Dtest=EndToEndAuthenticationFlowIntegrationTest

# Verificar se Docker está rodando
docker ps

# Se Docker não estiver rodando, iniciar:
sudo systemctl start docker   # Linux
# ou
open -a Docker                 # macOS
```

### Perfil Usado
- **Profile**: `integration-test`
- **Banco**: MySQL 9 via Testcontainers
- **Hibernate**: `ddl-auto: validate`
- **Flyway**: Habilitado (executa migrations)

### Container MySQL
```yaml
Image: mysql:9
Database: testdb
Username: test
Password: test
Porta: Dinâmica (atribuída pelo Testcontainers)
Reuse: true (container reutilizado entre testes para performance)
```

---

## 📊 Comparação: H2 vs Testcontainers

| Aspecto | H2 (Padrão) | Testcontainers (Opcional) |
|---------|-------------|---------------------------|
| **Velocidade** | ⚡ Rápido (~15s) | 🐌 Mais lento (~60s primeira vez) |
| **Requer Docker** | ❌ Não | ✅ Sim |
| **Banco Real** | ❌ H2 (compatibilidade MySQL) | ✅ MySQL 9 real |
| **Flyway** | ❌ Desabilitado | ✅ Habilitado |
| **Constraints/Índices** | ⚠️ Parcial | ✅ Completo |
| **CI/CD** | ✅ Funciona em qualquer ambiente | ⚠️ Requer Docker no CI |
| **Uso Recomendado** | Desenvolvimento e CI padrão | Validação pré-produção |

---

## 🧪 Testes End-to-End Implementados

Os testes de integração cobrem os fluxos principais:

### 1. Fluxo Completo de Autenticação
```
Register → Login → Acesso a Endpoint Protegido
```
- Registra usuário LOCAL
- Faz login e obtém tokens
- Acessa `/api/v1/users/me` com JWT
- Valida que dados do usuário estão corretos

### 2. Refresh Token com Rotação
```
Login → Refresh (obtém novos tokens) → Tentativa de Reuso (falha)
```
- Faz login inicial
- Usa refresh token para obter novos tokens
- Valida que token antigo foi revogado
- Tenta reusar token antigo (deve retornar 401)

### 3. Logout e Revogação
```
Login → Logout → Tentativa de Refresh (falha)
```
- Faz login
- Faz logout (revoga refresh token)
- Tenta usar refresh token após logout (deve retornar 401)

### 4. Google OAuth2 Login
```
Google Login → Acesso a Endpoint Protegido
```
- Valida ID Token do Google (mockado)
- Provisiona usuário automaticamente
- Atribui role USER
- Acessa endpoint protegido com JWT

### 5. RBAC (Role-Based Access Control)
```
Login como USER → Tentativa de Acesso Admin (negado)
```
- Usuário comum tenta acessar `/api/v1/admin/ping`
- Sistema retorna 403 Forbidden

### 6. Correlation ID
```
Request com X-Correlation-Id → Response com mesmo ID
```
- Envia header X-Correlation-Id customizado
- Valida que resposta inclui o mesmo ID
- Valida que erros também incluem correlation ID

---

## 🔧 Troubleshooting

### Erro: "Could not find a valid Docker environment"

**Causa:** Docker não está rodando ou não está acessível

**Solução:**
```bash
# Verificar se Docker está rodando
docker ps

# Se não estiver, iniciar Docker
sudo systemctl start docker   # Linux
open -a Docker                 # macOS

# Verificar permissões (Linux)
sudo usermod -aG docker $USER
newgrp docker
```

### Erro: "Port already in use"

**Causa:** Porta do MySQL já está em uso

**Solução:**
```bash
# Testcontainers usa portas dinâmicas, então isso raramente acontece
# Se acontecer, pare outros containers MySQL
docker ps
docker stop <container_id>
```

### Testes Lentos na Primeira Execução

**Causa:** Testcontainers precisa baixar a imagem MySQL na primeira vez

**Solução:**
```bash
# Baixar imagem antecipadamente
docker pull mysql:9

# Verificar imagens disponíveis
docker images | grep mysql
```

### Container não Para após Testes

**Causa:** Testcontainers está configurado com `reuse=true`

**Solução:**
```bash
# Isso é intencional para performance
# Para parar manualmente:
docker ps
docker stop <container_id>

# Ou parar todos os containers Testcontainers:
docker ps | grep testcontainers | awk '{print $1}' | xargs docker stop
```

---

## 📝 Estratégia de Testes Recomendada

### Desenvolvimento Local (Dia a Dia)
```bash
# Use testes com H2 (rápidos)
./mvnw test
```

### Antes de Commit/Push
```bash
# Use testes com H2 (garantia básica)
./mvnw test
```

### Antes de Merge/Deploy
```bash
# Use Testcontainers (validação completa)
./mvnw test -Dtest=EndToEndAuthenticationFlowIntegrationTest
```

### Pipeline CI/CD

**GitHub Actions / GitLab CI:**
```yaml
# Use H2 para testes rápidos
- name: Run Tests
  run: ./mvnw test

# Opcional: Testcontainers em job separado
- name: Integration Tests (MySQL)
  run: ./mvnw test -Dtest=EndToEndAuthenticationFlowIntegrationTest
  services:
    docker:
      image: docker:latest
```

---

## ✅ Checklist de Validação

Antes de considerar os testes completos e aprovados:

- [ ] **Todos os testes H2 passando** (`./mvnw test`)
- [ ] **Docker instalado** (`docker --version`)
- [ ] **Docker rodando** (`docker ps`)
- [ ] **Imagem MySQL baixada** (`docker pull mysql:9`)
- [ ] **Teste Testcontainers executado com sucesso**
- [ ] **Fluxos end-to-end validados:**
  - [ ] Register → Login → Acesso protegido
  - [ ] Refresh token com rotação
  - [ ] Logout revoga token
  - [ ] Google OAuth2 login
  - [ ] RBAC funciona (user ≠ admin)
  - [ ] Correlation ID propagado

---

## 📚 Documentação Adicional

- **Testcontainers**: https://testcontainers.com/
- **Spring Boot Test**: https://docs.spring.io/spring-boot/reference/testing/
- **MockMvc**: https://docs.spring.io/spring-framework/reference/testing/spring-mvc-test-framework.html

---

**Última atualização:** 26/12/2025  
**Versão:** 1.0.0  
**Status:** ✅ Testes H2 completos | ⚠️ Testcontainers requer Docker

