# 🛒 Shopping List

Aplicação **Shopping List** composta por:

- **Backend (API REST)** em **Java 21 + Spring Boot**
- **Frontend (App Mobile/Web)** em **React Native (Expo) + TypeScript**

O projeto foi construído com foco em **qualidade**, **Clean Architecture** e boas práticas para evoluir como produto.

---

## 📁 Estrutura do Repositório

```
.
├── backend/        # Shopping List API (Spring Boot)
└── frontend/       # Shopping List App (Expo / React Native)
```

> Cada projeto possui seu próprio README com detalhes específicos.

---

## ✅ Pré-requisitos

- **Java 21 (LTS)**
- **Docker + Docker Compose**
- **Node.js LTS + npm**
- (Opcional) Android Studio / Xcode

Verifique:
```bash
java -version
docker --version
docker compose version
node -v
npm -v
```

---

## 🚀 Quickstart (DEV)

### 1) Subir infraestrutura (MySQL)
```bash
cd backend
docker compose up -d
```

### 2) Subir o Backend
```bash
cd backend
./mvnw spring-boot:run
```

API disponível em:
```
http://localhost:8080
```

### 3) Subir o Frontend
```bash
cd frontend
npm install
cp .env.example .env
npm start
```

---

## ⚙️ Variáveis de Ambiente

### Backend (`backend/.env`)
Arquivo sensível (não commitar).

Exemplos:
- Credenciais MySQL
- JWT_SECRET
- Profile (dev/test/prod)

### Frontend (`frontend/.env`)
- `API_URL` deve apontar para o IP da máquina ao usar emulador/device.

---

## 🔌 Integração Front ↔ Back

- Base URL: `/api/v1`
- Auth: **JWT + Refresh Token**
- Perfil:
```
GET /api/v1/users/me
```
- Listas:
```
GET /api/v1/lists
POST /api/v1/lists
DELETE /api/v1/lists/{id}
```

---

## 🧪 Testes

### Backend
```bash
cd backend
./mvnw test
```

### Frontend
```bash
cd frontend
npm test
npm run check-all
```

---

## 📚 Documentação

- Backend: `backend/README.md`
- Frontend: `frontend/README.md`

Documentação adicional (recomendado):
```
docs/
├── ARCHITECTURE.md
├── AUTH.md
├── DECISIONS.md
└── ROADMAP.md
```

---

## 🗺️ Roadmap

- UX refinements
- CI/CD
- Observabilidade
- Testes E2E

---

## 🤝 Contribuição

1. Crie uma branch a partir da `main`
2. Garanta testes e lint passando
3. Abra um PR claro e objetivo
