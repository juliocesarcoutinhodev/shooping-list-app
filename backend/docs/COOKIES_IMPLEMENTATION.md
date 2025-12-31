# Refresh Token via Cookie HttpOnly - Implementação Completa

## 🎯 Objetivo
Entregar refresh token da forma mais segura e alinhada com o mercado, usando **cookies HttpOnly + Secure + SameSite**.

## ✅ Implementação Realizada

### 1. Classes Criadas

#### RefreshTokenCookieProperties.java
Configuração centralizada do cookie, permite configuração diferente por perfil:
- **HttpOnly**: true (sempre) - JavaScript não pode acessar
- **Secure**: false (dev/test), true (prod) - Apenas HTTPS em produção
- **SameSite**: "Lax" (dev/test), "Strict" (prod) - Proteção CSRF
- **Path**: "/api/v1/auth" - Restringe onde o cookie é enviado
- **MaxAge**: 604800 segundos (7 dias) - Igual ao tempo do token
- **Cookie-Only**: false (dev/test), true (prod) - Se retorna no body também
- **Domain**: null (usa domínio da requisição)

#### CookieService.java
Utilitário para gerenciar cookies com segurança:
- `addRefreshTokenCookie()` - Cria e adiciona cookie na resposta HTTP
- `clearRefreshTokenCookie()` - Remove cookie (logout)
- `getRefreshTokenFromCookie()` - Extrai token do cookie da requisição
- `isCookieOnly()` - Verifica se deve retornar no body também

### 2. Classes Modificadas

#### AuthController.java
Atualizado para suportar cookies em todos os endpoints de autenticação:

**Login (POST /api/v1/auth/login)**:
- Adiciona refresh token no cookie HttpOnly
- Se `cookie-only=true`: Remove token do body
- Se `cookie-only=false`: Token vai no body E no cookie

**Refresh (POST /api/v1/auth/refresh)**:
- Prioriza cookie (mais seguro)
- Aceita body se cookie não estiver presente (backward compatibility)
- Adiciona novo refresh token no cookie (rotação)
- Se `cookie-only=true`: Novo token só no cookie

**Logout (POST /api/v1/auth/logout)**:
- Prioriza cookie
- Aceita body se cookie não estiver presente
- Remove cookie do navegador (Max-Age=0)

### 3. Configurações por Perfil

#### application-dev.yml
```yaml
app:
  security:
    refresh-token:
      cookie:
        http-only: true
        secure: false      # HTTP permitido
        same-site: Lax     # Mais permissivo
        cookie-only: false # Retorna no body também
```

#### application-test.yml
```yaml
app:
  security:
    refresh-token:
      cookie:
        http-only: true
        secure: false      # HTTP permitido
        same-site: Lax
        cookie-only: false # Facilita testes
        max-age: 86400     # 1 dia
```

#### application-prod.yml (NOVO)
```yaml
app:
  security:
    refresh-token:
      cookie:
        http-only: true
        secure: true       # Apenas HTTPS
        same-site: Strict  # Máxima proteção CSRF
        cookie-only: true  # Apenas cookie (mais seguro)
```

## 🔒 Segurança Implementada

### Proteções Ativas

1. **HttpOnly** (sempre true):
   - JavaScript NÃO pode acessar o cookie
   - Protege contra ataques XSS

2. **Secure** (prod):
   - Cookie enviado apenas via HTTPS
   - Protege contra man-in-the-middle

3. **SameSite** (Strict em prod):
   - Protege contra ataques CSRF
   - Cookie não é enviado em requisições cross-site

4. **Path Restrito**:
   - Cookie só enviado para `/api/v1/auth/*`
   - Reduz superfície de ataque

5. **Cookie-Only em Prod**:
   - Token NUNCA vai no body da resposta
   - Apenas no cookie (máxima segurança)

### Estratégia Híbrida

| Ambiente | Cookie | Body | Secure | SameSite |
|----------|--------|------|--------|----------|
| **dev**  | ✅     | ✅   | ❌     | Lax      |
| **test** | ✅     | ✅   | ❌     | Lax      |
| **prod** | ✅     | ❌   | ✅     | Strict   |

## 📋 Critérios de Aceite - TODOS ATENDIDOS

✅ **Em login e refresh, refresh pode ser:**
- Setado via cookie HttpOnly (recomendado) ✅
- (Opcional) Body em dev/test somente ✅

✅ **Cookie configurável por profile:**
- Secure true em prod ✅
- Secure false em dev/test ✅

✅ **Documentação clara:**
- README atualizado ✅
- Comentários no código ✅

## 🧪 Testes

### CookieServiceTest.java
13 testes unitários criados:
- Adicionar cookie com configurações corretas
- Adicionar cookie Secure
- Adicionar cookie com SameSite=Strict
- Remover cookie
- Extrair token do cookie
- Extrair token com múltiplos cookies
- Retornar vazio quando sem cookies
- Verificar cookie-only
- Adicionar cookie com domínio

### AuthControllerCookieTest.java
5 testes de integração criados:
- Login adiciona cookie
- Refresh aceita cookie e cria novo
- Logout remove cookie
- Refresh aceita body (backward compatibility)
- Refresh retorna 400 sem token

## 🚀 DoD - Definition of Done

✅ **Refresh funciona via cookie em ambiente configurado**
- Implementado e testado
- Funcionando em dev/test/prod

✅ **Backward Compatibility**
- Dev/test continuam suportando body
- Produção usa apenas cookie

✅ **Rotação de Tokens**
- Novo cookie criado a cada refresh
- Cookie antigo removido no logout

## 📝 Como Usar

### Cliente Web (JavaScript)

```javascript
// Login
const loginResponse = await fetch('/api/v1/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password }),
  credentials: 'include' // IMPORTANTE: inclui cookies
});

// Refresh (automático via cookie)
const refreshResponse = await fetch('/api/v1/auth/refresh', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: '{}', // Body vazio, usa cookie
  credentials: 'include' // IMPORTANTE: inclui cookies
});

// Logout
await fetch('/api/v1/auth/logout', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: '{}', // Body vazio, usa cookie
  credentials: 'include' // IMPORTANTE: inclui cookies
});
```

### Postman/Insomnia (Dev/Test)

**Opção 1: Usar body (backward compatibility)**
```json
POST /api/v1/auth/refresh
{
  "refreshToken": "uuid-do-token"
}
```

**Opção 2: Usar cookie (recomendado)**
```
POST /api/v1/auth/refresh
Cookie: refreshToken=uuid-do-token
Body: {}
```

### Produção

Em produção (`cookie-only: true`), o cliente DEVE usar cookies:
- Body não retornará refresh token
- Refresh token apenas no cookie
- Máxima segurança

## 🎉 Benefícios

1. **Segurança Máxima**:
   - HttpOnly: Protege contra XSS
   - Secure: Protege contra man-in-the-middle
   - SameSite: Protege contra CSRF

2. **Flexibilidade**:
   - Dev/test: Body + Cookie (facilita desenvolvimento)
   - Prod: Apenas Cookie (máxima segurança)

3. **Backward Compatibility**:
   - Clientes antigos continuam funcionando em dev/test
   - Migração gradual para cookies

4. **Alinhado com Mercado**:
   - Padrão OAuth2/OIDC
   - Recomendações OWASP
   - Best practices de segurança

## ✅ Status

**IMPLEMENTAÇÃO COMPLETA E FUNCIONAL** 🎉

Todos os critérios de aceite foram atendidos. A funcionalidade está pronta para uso em desenvolvimento, teste e produção.

