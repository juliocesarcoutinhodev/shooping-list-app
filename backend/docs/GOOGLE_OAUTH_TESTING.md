# 🧪 Guia de Teste - Google OAuth2 Authentication

## 📋 Pré-requisitos

1. **Configurar Google Client ID:**
   
   Adicione ao arquivo `.env` na raiz do projeto:
   ```bash
   # Google OAuth2 Configuration
   GOOGLE_CLIENT_ID=407408718192.apps.googleusercontent.com
   ```
   
   **Nota:** Este é o Client ID do Google OAuth Playground. Para produção, use seu próprio Client ID.

2. **Container MySQL rodando:**
   ```bash
   docker-compose up -d
   ```

3. **Aplicação rodando localmente:**
   ```bash
   cd /home/julio/Documentos/Github/shopping-list/backend
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```
   
   **⚠️ IMPORTANTE:** Se você já estava rodando a aplicação antes de adicionar o `GOOGLE_CLIENT_ID`, reinicie-a para carregar a nova configuração!

---

## 🎯 Opção 1: Teste Rápido com Google OAuth Playground

### Passo 1: Obter ID Token

1. Acesse: https://developers.google.com/oauthplayground/
2. No lado esquerdo, em "Input your own scopes", adicione:
   ```
   openid
   profile
   email
   ```
3. Clique em "Authorize APIs"
4. Faça login com sua conta Google
5. Clique em "Exchange authorization code for tokens"
6. Copie o valor do campo `id_token`

### Passo 2: Testar no Postman

**Endpoint:** `POST http://localhost:8080/api/v1/auth/google`

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "idToken": "COLE_O_ID_TOKEN_AQUI"
}
```

**Resposta Esperada (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "expiresIn": 3600
}
```

---

## 🎯 Opção 2: Teste com HTML Local (Mais Realista)

### Passo 1: Criar Google Cloud Project

1. Acesse: https://console.cloud.google.com/
2. Crie um novo projeto ou selecione um existente
3. Vá para "APIs & Services" > "Credentials"
4. Clique em "CREATE CREDENTIALS" > "OAuth client ID"
5. Selecione "Web application"
6. Configure:
   - **Name:** Shopping List Dev
   - **Authorized JavaScript origins:**
     ```
     http://localhost
     http://localhost:8080
     http://127.0.0.1
     ```
   - **Authorized redirect URIs:**
     ```
     http://localhost
     ```
7. Copie o **Client ID** gerado

### Passo 2: Configurar no Backend

Edite o arquivo `.env` na raiz do projeto:
```bash
# Google OAuth2
GOOGLE_CLIENT_ID=seu-client-id-aqui.apps.googleusercontent.com
```

Ou edite diretamente o `application-dev.yml`:
```yaml
app:
  google:
    client-id: seu-client-id-aqui.apps.googleusercontent.com
```

### Passo 3: Usar o HTML Test Tool

1. Abra o arquivo `google-oauth-test.html` no navegador:
   ```bash
   # Linux/Mac
   xdg-open google-oauth-test.html
   
   # Ou simplesmente arraste para o navegador
   ```

2. Cole o **Google Client ID** no campo
3. Clique em "Configurar Google Sign-In"
4. Clique no botão "Sign in with Google"
5. Faça login com sua conta Google
6. O ID Token será exibido automaticamente
7. Clique em "📋 Copiar Token"

### Passo 4: Testar no Postman

Use o token copiado conforme mostrado na Opção 1.

---

## 🧪 Cenários de Teste

### ✅ Cenário 1: Primeiro Login (Novo Usuário)

**Request:**
```json
POST http://localhost:8080/api/v1/auth/google
Content-Type: application/json

{
  "idToken": "valid.google.id.token"
}
```

**Validações:**
- ✅ Status: 200 OK
- ✅ Retorna `accessToken`, `refreshToken` e `expiresIn`
- ✅ Cookie `refreshToken` é setado com `HttpOnly`
- ✅ Usuário é criado no banco com `provider=GOOGLE`
- ✅ Usuário recebe role `USER` automaticamente
- ✅ `passwordHash` é NULL

**Verificar no Banco:**
```sql
SELECT * FROM tb_user WHERE email = 'seu-email@gmail.com';
SELECT * FROM tb_user_role WHERE user_id = (SELECT id FROM tb_user WHERE email = 'seu-email@gmail.com');
```

---

### ✅ Cenário 2: Login Subsequente (Usuário Existente)

**Request:** (mesmo do Cenário 1)

**Validações:**
- ✅ Status: 200 OK
- ✅ Não cria usuário duplicado
- ✅ Retorna tokens normalmente
- ✅ Novo refresh token é criado

---

### ❌ Cenário 3: Token Inválido

**Request:**
```json
POST http://localhost:8080/api/v1/auth/google
Content-Type: application/json

{
  "idToken": "token.invalido.aqui"
}
```

**Resposta Esperada (401 Unauthorized):**
```json
{
  "timestamp": "2025-12-26T10:30:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token do Google inválido: ...",
  "path": "/api/v1/auth/google"
}
```

---

### ❌ Cenário 4: Token Expirado

Use um token antigo (>1 hora)

**Resposta Esperada (401 Unauthorized):**
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Token do Google inválido: Token expirado"
}
```

---

### ❌ Cenário 5: ID Token Vazio

**Request:**
```json
POST http://localhost:8080/api/v1/auth/google
Content-Type: application/json

{
  "idToken": ""
}
```

**Resposta Esperada (400 Bad Request):**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Erro de validação. Verifique os campos enviados.",
  "details": [
    {
      "field": "idToken",
      "message": "ID Token do Google é obrigatório",
      "rejectedValue": ""
    }
  ]
}
```

---

## 🔍 Verificação Completa

### 1. Verificar Access Token Gerado

Use: https://jwt.io/

Cole o `accessToken` e verifique:
- ✅ Payload contém: `sub`, `email`, `name`, `provider`, `roles`
- ✅ `roles` contém `["USER"]`
- ✅ `provider` = `"GOOGLE"`
- ✅ `exp` está correto (1 hora no futuro)

### 2. Verificar Refresh Token no Banco

```sql
SELECT 
    rt.id,
    rt.token_hash,
    rt.expires_at,
    rt.revoked_at,
    rt.user_agent,
    rt.ip,
    u.email
FROM tb_refresh_token rt
JOIN tb_user u ON rt.user_id = u.id
WHERE u.email = 'seu-email@gmail.com'
ORDER BY rt.created_at DESC
LIMIT 5;
```

**Validações:**
- ✅ `token_hash` é um SHA-256 (não é o UUID original)
- ✅ `expires_at` é 7 dias no futuro
- ✅ `revoked_at` é NULL
- ✅ `user_agent` e `ip` estão preenchidos

### 3. Testar Endpoint Protegido

**Request:**
```
GET http://localhost:8080/api/v1/users/me
Authorization: Bearer {accessToken}
```

**Resposta Esperada (200 OK):**
```json
{
  "id": 1,
  "email": "seu-email@gmail.com",
  "name": "Seu Nome",
  "provider": "GOOGLE",
  "status": "ACTIVE",
  "roles": ["USER"],
  "createdAt": "2025-12-26T10:00:00Z"
}
```

---

## 🔄 Testar Refresh Token

**Request:**
```
POST http://localhost:8080/api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "uuid-do-refresh-token"
}
```

**Resposta Esperada (200 OK):**
```json
{
  "accessToken": "novo.access.token",
  "refreshToken": "novo-uuid-refresh",
  "expiresIn": 3600
}
```

---

## 🚪 Testar Logout

**Request:**
```
POST http://localhost:8080/api/v1/auth/logout
Content-Type: application/json

{
  "refreshToken": "uuid-do-refresh-token"
}
```

**Resposta Esperada (204 No Content)**

Após logout, o refresh token não pode mais ser usado.

---

## 📝 Checklist Completo

- [ ] Container MySQL rodando
- [ ] Aplicação rodando em dev
- [ ] Google Client ID configurado
- [ ] ID Token obtido com sucesso
- [ ] Login com Google funciona (200 OK)
- [ ] Usuário criado no banco
- [ ] Access token válido (verificado no jwt.io)
- [ ] Refresh token persiste no banco
- [ ] Endpoint `/users/me` funciona com token
- [ ] Refresh token rotation funciona
- [ ] Logout revoga o token

---

## 🐛 Troubleshooting

### Erro: "Token do Google inválido"

**Causa:** Token expirado ou inválido
**Solução:** Gere um novo token

### Erro: "Role USER não encontrada"

**Causa:** Banco não tem seed de roles
**Solução:** Execute as migrations do Flyway

### Erro: Connection refused

**Causa:** MySQL não está rodando
**Solução:** `docker-compose up -d`

### Erro: "GOOGLE_CLIENT_ID" não configurado

**Causa:** Variável de ambiente não setada
**Solução:** Configure no `.env` ou `application-dev.yml`

---

## 📞 Suporte

Se encontrar problemas:
1. Verifique os logs da aplicação
2. Verifique se o MySQL está rodando
3. Verifique se o token não expirou
4. Use `./mvnw test` para garantir que os testes passam

---

**Última atualização:** 26/12/2025

