# 🔒 Security Checklist - Shopping List API

## ✅ Checklist de Segurança Implementado

### 1. Proteção de Dados Sensíveis em Logs

- [x] **Senhas NUNCA são logadas**
  - LoginRequest.password anotado com @Sensitive
  - RegisterRequest.password anotado com @Sensitive
  - ToString() sobrescrito usando LogSanitizer
  
- [x] **Tokens parcialmente mascarados em logs**
  - LogSanitizer.maskToken() disponível para debug
  - Refresh tokens apenas logados como tokenId (hash, não o valor)
  - Access tokens JWT nunca logados (apenas gerados)

- [x] **Dados pessoais sensíveis protegidos**
  - Annotation @Sensitive criada para marcar campos
  - LogSanitizer.sanitize() mascara campos automaticamente
  - Utility class com métodos mask() e maskToken()

### 2. Correlation ID (Request Tracking)

- [x] **CorrelationIdFilter implementado**
  - Extrai X-Correlation-Id do header ou gera UUID
  - Adiciona ao MDC (Mapped Diagnostic Context)
  - Retorna no header X-Correlation-Id da resposta
  - Incluído automaticamente em todos os logs
  
- [x] **Logback configurado com correlation-id**
  - Pattern de console inclui [correlationId]
  - Pattern de arquivo inclui [correlationId]
  - Cor cyan no console para melhor visualização

- [x] **ErrorResponse inclui correlationId**
  - Campo correlationId adicionado ao DTO
  - Factory methods extraem do MDC automaticamente
  - Cliente pode usar para rastrear erros

### 3. Mensagens de Erro Seguras

- [x] **Credenciais inválidas - mensagem genérica**
  - "Credenciais inválidas" tanto para email não encontrado quanto senha errada
  - Não revela se o email existe no sistema
  - Previne enumeração de usuários

- [x] **Tokens inválidos - sem detalhes internos**
  - "Token do Google inválido ou expirado" (sem stacktrace para cliente)
  - "Refresh token inválido" (sem revelar motivo específico)
  - "Token JWT inválido" (genérico)

- [x] **Erros de validação - apenas campos e mensagens**
  - Não expõe estrutura interna da aplicação
  - Mensagens amigáveis para o usuário
  - Sem stacktraces em produção

- [x] **Erro 500 - mensagem genérica**
  - "Erro interno do servidor" (sem detalhes)
  - Stacktrace apenas em logs (não na resposta)
  - Correlation ID para rastreamento

### 4. Security Headers

- [x] **X-Content-Type-Options: nosniff**
  - Previne MIME type sniffing
  - Protege contra ataques de upload malicioso

- [x] **X-XSS-Protection: 1; mode=block**
  - Ativa proteção XSS do navegador
  - Modo block: para renderização se XSS detectado

- [x] **X-Frame-Options: SAMEORIGIN**
  - Previne clickjacking
  - Permite apenas frames do mesmo domínio
  - Necessário para H2 Console em dev

- [x] **Referrer-Policy: strict-origin-when-cross-origin**
  - Protege informações de navegação
  - Não vaza URLs com dados sensíveis

- [x] **Cache-Control**
  - Desabilita cache de respostas sensíveis
  - Previne exposição de dados em cache do navegador

### 5. Autenticação e Autorização

- [x] **Passwords com BCrypt**
  - 10 rounds (padrão seguro)
  - Salt automático por senha
  - Nunca armazenado em texto puro

- [x] **JWT com HS256**
  - Chave secreta mínima de 256 bits
  - Issuer definido
  - Expirações configuráveis por perfil

- [x] **Refresh Token com SHA-256**
  - Armazenado apenas como hash
  - UUID único por token
  - Rotação automática (one-time use)
  - Detecção de reuso (alerta de segurança)

- [x] **RBAC (Role-Based Access Control)**
  - Roles persistidas no banco
  - Propagadas no JWT
  - Autorização centralizada no SecurityFilterChain
  - Rotas /admin/** protegidas

### 6. Session Management

- [x] **API Stateless**
  - SessionCreationPolicy.STATELESS
  - Sem JSESSIONID
  - Autenticação via Bearer token

- [x] **CSRF Desabilitado**
  - Apropriado para APIs REST stateless
  - Tokens no header (não em cookies de sessão)

- [x] **CORS Configurado**
  - Origins permitidas por perfil
  - Métodos HTTP whitelist
  - Headers whitelist
  - Credentials permitido (para cookies HttpOnly)

### 7. Cookies Seguros (Refresh Token)

- [x] **HttpOnly**
  - JavaScript não pode acessar
  - Protege contra XSS

- [x] **Secure (prod)**
  - Apenas HTTPS em produção
  - HTTP permitido apenas em dev/test

- [x] **SameSite**
  - Lax em dev (mais permissivo)
  - Strict em prod (máxima proteção CSRF)

- [x] **Path restrito**
  - /api/v1/auth (apenas endpoints de auth)
  - Minimiza exposição do cookie

### 8. Error Handling

- [x] **GlobalExceptionHandler**
  - Intercepta todas as exceções
  - Retorna ErrorResponse padronizado
  - Logs estruturados com correlationId

- [x] **Exception Handlers Específicos**
  - ValidationException (400)
  - AuthenticationException (401)
  - AccessDeniedException (403)
  - NotFoundException (404)
  - IllegalArgumentException (400)
  - IllegalStateException (409)
  - Generic Exception (500)

- [x] **Custom Authentication Entry Point**
  - Retorna 401 com mensagem padronizada
  - Não expõe detalhes de Spring Security

- [x] **Custom Access Denied Handler**
  - Retorna 403 com mensagem padronizada
  - Indica falta de permissão

### 9. Auditoria e Logs

- [x] **Logs Estruturados**
  - Timestamp ISO-8601
  - Log level (INFO, WARN, ERROR)
  - Thread name
  - Correlation ID
  - Logger name
  - Message

- [x] **Eventos de Segurança Logados**
  - Login bem-sucedido (userId, email)
  - Login falhou (email, motivo genérico)
  - Refresh token rotacionado (tokenId, userId)
  - **ALERTA: Reuso de token** (tokenId, userId)
  - Logout (tokenId, userId)
  - Google OAuth login (email, googleId)

- [x] **Metadata de Sessão**
  - User-Agent capturado
  - IP address capturado (X-Forwarded-For aware)
  - Timestamp de criação
  - Timestamp de último uso
  - Persistido com refresh token

### 10. Configurações por Perfil

- [x] **Dev Profile**
  - Logs DEBUG
  - HTTP permitido (Secure=false)
  - SameSite=Lax
  - Refresh token no body e cookie
  - H2 Console habilitado

- [x] **Test Profile**
  - Logs WARN (silencioso)
  - H2 em memória
  - Flyway desabilitado
  - Schema create-drop

- [x] **Prod Profile** (configuração recomendada)
  - Logs INFO
  - HTTPS obrigatório (Secure=true)
  - SameSite=Strict
  - Refresh token APENAS em cookie
  - Logs em arquivo com rotação

---

## ⚠️ Itens Pendentes (Future Enhancements)

### Rate Limiting (opcional para produção)
- [ ] Implementar rate limit básico para endpoints de auth
- [ ] Usar Bucket4j ou Spring Cloud Gateway
- [ ] Limite de 5 tentativas de login em 5 minutos
- [ ] Limite de 10 requisições/minuto para /auth/**

### Content Security Policy
- [ ] Adicionar CSP header
- [ ] Definir policy: `default-src 'self'`

### HSTS (HTTP Strict Transport Security)
- [ ] Habilitar em produção
- [ ] max-age: 31536000 (1 ano)
- [ ] includeSubDomains

### Certificate Pinning
- [ ] Considerar para mobile apps

### Brute Force Protection
- [ ] Account lockout após N tentativas falhas
- [ ] Captcha após 3 tentativas

### Token Revocation List
- [ ] Implementar blacklist de JWTs revogados
- [ ] Usar Redis/Memcached para performance

### 2FA (Two-Factor Authentication)
- [ ] TOTP (Time-based One-Time Password)
- [ ] SMS/Email verification code

---

## 📊 Resumo de Segurança

| Categoria | Status | Nota |
|-----------|--------|------|
| Proteção de Dados Sensíveis | ✅ Implementado | Senhas e tokens protegidos |
| Correlation ID | ✅ Implementado | Rastreamento completo |
| Mensagens de Erro | ✅ Implementado | Genéricas e seguras |
| Security Headers | ✅ Implementado | Headers essenciais |
| Autenticação | ✅ Implementado | BCrypt + JWT + Rotação |
| Autorização | ✅ Implementado | RBAC com roles |
| Session Management | ✅ Implementado | Stateless + Cookies seguros |
| Error Handling | ✅ Implementado | Centralizado e padronizado |
| Auditoria | ✅ Implementado | Logs estruturados |
| Rate Limiting | ⚠️ Opcional | Recomendado para prod |

---

## 🔍 Como Validar

### 1. Testar Correlation ID

```bash
# Enviar correlation-id customizado
curl -X GET http://localhost:8080/api/v1/health \
  -H "X-Correlation-Id: my-custom-id-123"

# Resposta deve incluir o mesmo correlation-id
# X-Correlation-Id: my-custom-id-123
```

### 2. Verificar Logs Sanitizados

```bash
# Fazer login e verificar logs
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"teste@email.com","password":"senha123"}'

# Logs devem mostrar:
# LoginRequest{email=teste@email.com, password=***REDACTED***}
# NUNCA: password=senha123
```

### 3. Validar Security Headers

```bash
curl -I http://localhost:8080/api/v1/health

# Deve retornar headers:
# X-Content-Type-Options: nosniff
# X-XSS-Protection: 1; mode=block
# X-Frame-Options: SAMEORIGIN
# Referrer-Policy: strict-origin-when-cross-origin
```

### 4. Testar Mensagens Genéricas

```bash
# Email não existe
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"naoexiste@email.com","password":"qualquer"}'

# Senha errada
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"existe@email.com","password":"errada"}'

# Ambos devem retornar MESMA mensagem:
# {"message": "Credenciais inválidas"}
```

---

**Última atualização:** 26/12/2025  
**Versão:** 1.0.0  
**Status:** ✅ Aprovado para produção (com recomendações de rate limiting)

