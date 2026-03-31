# Deploy automático para VPS (GitHub Actions + GHCR + Caddy)

Este projeto foi configurado para publicar imagem Docker no GHCR e fazer deploy na VPS via SSH sem expor a porta `8080` no host. No staging, o MySQL sobe junto com a API no mesmo `docker-compose.prod.yml`.

## 1) Ajustes rápidos antes de usar

Altere os valores abaixo no arquivo `.github/workflows/deploy.yml`:

- `branches: [main]` para sua branch de deploy (`<branch-deploy>`)
- `APP_NAME` (nome do container/diretório em `/opt/<nome-app>`)
- `IMAGE_NAME` (nome da imagem no GHCR)

> Exemplo usado aqui: `shopping-list-api`.

## 2) Secrets obrigatórios no GitHub

No repositório, configure em **Settings > Secrets and variables > Actions**:

- `VPS_HOST`
- `VPS_USER`
- `VPS_SSH_KEY`
- `VPS_PORT`
- `GHCR_USERNAME`
- `GHCR_TOKEN`
- `MYSQL_ROOT_PASSWORD`
- `MYSQL_DATABASE`
- `MYSQL_USER`
- `MYSQL_PASSWORD`
- `JWT_SECRET`
- `JWT_ISSUER`
- `GOOGLE_CLIENT_ID`
- `COOKIE_DOMAIN`

### Observações importantes

- `GHCR_TOKEN` deve ter permissão de leitura de pacotes (`read:packages`) no GHCR.
- `VPS_SSH_KEY` deve ser a chave privada correspondente à chave pública autorizada na VPS.

## 3) Preparar VPS antes do primeiro deploy

Conecte na VPS por SSH e execute:

```bash
mkdir -p /opt/shopping-list-api
cd /opt/shopping-list-api
```

O arquivo `.env.production` sera criado/atualizado automaticamente pelo workflow usando os secrets do GitHub.

Neste fluxo, o banco de dados tambem sobe junto com a aplicacao via Compose (`service: mysql`) e fica acessivel internamente por `MYSQL_HOST=mysql` e `MYSQL_PORT=3306`.

Crie a rede externa compartilhada com o Caddy:

```bash
docker network create edge
```

Garanta que o container do Caddy esteja na rede `edge`:

```bash
docker network connect edge caddy-staging || true
```

## 4) Fluxo de deploy

No push para a branch configurada, o workflow:

1. Builda a imagem com `Dockerfile` de produção
2. Publica no GHCR com as tags:
   - `ghcr.io/<github-user>/<nome-imagem>:latest`
   - `ghcr.io/<github-user>/<nome-imagem>:<commit-sha>`
3. Conecta via SSH na VPS
4. Gera o `.env.production` na VPS com os secrets
5. Sobe/atualiza API + MySQL no mesmo compose
6. Executa:

```bash
cd /opt/<nome-app>
APP_IMAGE=ghcr.io/<github-user>/<nome-imagem>:<commit-sha> docker compose --env-file .env.production -f docker-compose.prod.yml up -d
```

## 5) Trecho do Caddyfile para rota por prefixo

Adicione no bloco do host `vps7348.integrator.host` (antes do fallback da outra app):

```caddy
vps7348.integrator.host {
    # Nova API em /shopping-list-api/*
    handle_path /shopping-list-api/* {
        reverse_proxy shopping-list-api:8080
    }

    # Fallback para a aplicação já existente
    handle {
        reverse_proxy app-staging:8080
    }
}
```

Depois recarregue o Caddy:

```bash
docker exec caddy-staging caddy reload --config /etc/caddy/Caddyfile
```

## 6) Prefixo de rota e Spring Boot

Com `handle_path`, o Caddy remove `/shopping-list-api` antes de encaminhar para o container.

- URL pública: `https://vps7348.integrator.host/shopping-list-api/api/v1/lists`
- URL que chega na aplicação: `/api/v1/lists`

Para este projeto, isso é o mais compatível, porque os controllers já usam `/api/v1/...`.

Se no futuro você optar por **não** remover prefixo (usando `handle` em vez de `handle_path`), então a aplicação precisará de `server.servlet.context-path=/shopping-list-api` em produção.

## 7) Alternativa futura (subdomínio)

Se quiser simplificar roteamento e observabilidade, alternativa recomendada:

- `https://api.vps7348.integrator.host` apontando direto para `shopping-list-api:8080`

Mas a solução principal deste guia permanece por caminho: `https://vps7348.integrator.host/shopping-list-api`.

