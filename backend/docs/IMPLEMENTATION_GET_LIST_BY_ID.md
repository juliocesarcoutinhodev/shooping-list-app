# Implementação: GET /api/v1/lists/{id}

## Resumo

Implementado endpoint para buscar detalhes completos de uma lista de compras por ID, incluindo todos os itens, seguindo o padrão arquitetural do projeto.

## Arquivos Criados/Modificados

### Use Case
- `src/main/java/br/com/shooping/list/application/usecase/GetShoppingListByIdUseCase.java`
  - Busca lista por ID
  - Valida ownership (apenas dono pode ver)
  - Mapeia para DTO incluindo todos os itens
  - Retorna ShoppingListResponse completo

### DTO
- `src/main/java/br/com/shooping/list/application/dto/shoppinglist/ShoppingListResponse.java`
  - Adicionado campo `items` (List<ItemResponse>) opcional
  - Campo é null em outros endpoints para otimizar payload
  - Preenchido apenas no GET /api/v1/lists/{id}

### Controller
- `src/main/java/br/com/shooping/list/interfaces/rest/v1/ShoppingListController.java`
  - Adicionado endpoint `GET /api/v1/lists/{id}`
  - Segue padrão dos outros endpoints (autenticação, logging, validação)

### Use Case (Ajuste)
- `src/main/java/br/com/shooping/list/application/usecase/UpdateShoppingListUseCase.java`
  - Ajustado construtor para incluir items=null (não necessário neste endpoint)

### Testes
- `src/test/java/br/com/shooping/list/interfaces/rest/v1/ShoppingListControllerTest.java`
  - 5 novos testes de integração:
    - Buscar lista com itens (sucesso)
    - Buscar lista vazia (sem itens)
    - 404 quando lista não existe
    - 403 quando lista pertence a outro usuário
    - 401 sem token JWT

## Endpoint Implementado

```http
GET /api/v1/lists/{id}
Authorization: Bearer {token}
```

**Resposta (200 OK):**
```json
{
  "id": 1,
  "ownerId": 1,
  "title": "Lista da Feira",
  "description": "Compras semanais",
  "items": [
    {
      "id": 1,
      "name": "Arroz",
      "quantity": 2.0,
      "unit": "kg",
      "status": "PENDING",
      "createdAt": "2025-12-30T10:00:00.000Z",
      "updatedAt": "2025-12-30T10:00:00.000Z"
    }
  ],
  "itemsCount": 1,
  "pendingItemsCount": 1,
  "purchasedItemsCount": 0,
  "createdAt": "2025-12-30T09:00:00.000Z",
  "updatedAt": "2025-12-30T10:00:00.000Z"
}
```

## Características

- Autenticação JWT obrigatória
- Validação de ownership (apenas dono pode ver)
- Retorna lista completa com todos os itens
- Itens mapeados corretamente (name, quantity, unit, status)
- Tratamento de erros padronizado (404, 403, 401)
- Logging estruturado em todas as operações
- Transação read-only para performance

## Padrão Seguido

O endpoint segue exatamente o mesmo padrão dos outros endpoints:

1. **Controller**: Extrai ownerId do SecurityContext, chama use case, retorna resposta
2. **Use Case**: Busca no repository, valida ownership, mapeia para DTO
3. **Repository**: Usa método findById já existente
4. **DTO**: ShoppingListResponse com items opcional
5. **Testes**: Cobertura completa de cenários (sucesso, erros, auth)

## Compatibilidade

- Outros endpoints não são afetados (items é opcional e null)
- Frontend já está preparado para consumir este endpoint
- Não quebra contratos existentes

## Status

- Use case implementado
- DTO atualizado
- Controller atualizado
- Testes adicionados
- Pronto para uso



