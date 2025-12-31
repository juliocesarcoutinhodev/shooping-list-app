# Domain-Driven Design - Shopping List

## Visão Geral

Este documento define a linguagem ubíqua, entidades, aggregates e regras de negócio do domínio de Listas de Compras.
O objetivo é estabelecer um vocabulário comum entre desenvolvedores e stakeholders, além de guiar a implementação futura.

---

## Bounded Context

**Shopping List Context**: Responsável pelo gerenciamento de listas de compras e seus itens.

### Limites do Contexto

**O que ENTRA neste contexto:**
- Criação, edição e exclusão de listas de compras
- Adição, edição, remoção e marcação de itens nas listas
- Organização de itens por categorias
- Gestão de quantidades e unidades de medida

**O que NÃO ENTRA (fora do escopo atual):**
- Autenticação e autorização (contexto separado: User Context)
- Compartilhamento de listas entre usuários (feature futura)
- Histórico de compras realizadas (feature futura)
- Sugestões automáticas de itens (feature futura)
- Sincronização offline (feature futura)
- Integração com sistemas de supermercados (feature futura)

---

## Linguagem Ubíqua

### Termos do Domínio

**Lista de Compras (Shopping List)**
Uma coleção de itens que um usuário pretende comprar. Cada lista possui um nome, um dono e pode conter zero ou mais itens.

**Item de Lista (List Item)**
Um produto ou item específico dentro de uma lista de compras. Possui nome, quantidade, unidade de medida, categoria e status de compra.

**Dono da Lista (List Owner)**
O usuário que criou a lista e possui controle total sobre ela (editar, deletar, adicionar itens).

**Status do Item**
Indica se um item foi comprado ou não. Valores possíveis: PENDING (não comprado) e PURCHASED (comprado).

**Categoria (Category)**
Agrupamento lógico de itens para facilitar organização (ex: Frutas, Laticínios, Limpeza, Bebidas).

**Quantidade**
Valor numérico que indica quantas unidades do item devem ser compradas (ex: 2, 1.5, 10).

**Unidade de Medida (Unit)**
Unidade que acompanha a quantidade (ex: kg, litros, unidades, pacotes).

---

## Entidades e Aggregates

### Aggregate Root: ShoppingList

**Responsabilidades:**
- Gerenciar ciclo de vida dos itens da lista
- Garantir invariantes do aggregate (ex: não permitir itens duplicados com mesmo nome)
- Controlar associação com o dono (User)

**Atributos:**
- id: Long (identificador único)
- name: String (nome da lista, obrigatório, 3-100 caracteres)
- description: String (descrição opcional, até 255 caracteres)
- owner: User (referência ao dono, obrigatório)
- items: Set&lt;ListItem&gt; (coleção de itens, gerenciada internamente)
- createdAt: Instant (timestamp de criação, imutável)
- updatedAt: Instant (timestamp de última atualização)

**Invariantes:**
- Nome da lista não pode ser vazio ou nulo
- Dono da lista não pode ser nulo
- Não é permitido adicionar itens com nome duplicado na mesma lista (case-insensitive)
- Lista não pode ter mais de 100 itens (limite de negócio)

**Comportamentos (Métodos de Negócio):**
- `create(name, description, owner)`: Factory method para criar nova lista
- `updateName(name)`: Atualiza nome da lista
- `updateDescription(description)`: Atualiza descrição da lista
- `addItem(name, quantity, unit, category)`: Adiciona novo item (valida duplicatas)
- `removeItem(itemId)`: Remove item da lista
- `clearPurchasedItems()`: Remove todos os itens marcados como comprados
- `countTotalItems()`: Retorna total de itens na lista
- `countPendingItems()`: Retorna total de itens não comprados
- `countPurchasedItems()`: Retorna total de itens comprados

### Entity: ListItem

**Responsabilidades:**
- Representar um item individual dentro de uma lista
- Controlar status de compra (pending/purchased)
- Validar quantidade e nome do item

**Atributos:**
- id: Long (identificador único)
- shoppingList: ShoppingList (referência à lista pai, obrigatório)
- name: String (nome do item, obrigatório, 2-100 caracteres)
- quantity: BigDecimal (quantidade, obrigatório, maior que zero)
- unit: String (unidade de medida, opcional, até 20 caracteres)
- category: Category (categoria do item, opcional)
- status: ItemStatus (PENDING ou PURCHASED, padrão PENDING)
- createdAt: Instant (timestamp de criação, imutável)
- updatedAt: Instant (timestamp de última atualização)

**Invariantes:**
- Nome do item não pode ser vazio ou nulo
- Quantidade deve ser maior que zero
- Status não pode ser nulo
- Referência à lista pai não pode ser nula

**Comportamentos (Métodos de Negócio):**
- `create(shoppingList, name, quantity, unit, category)`: Factory method para criar item
- `markAsPurchased()`: Marca item como comprado
- `markAsPending()`: Marca item como não comprado
- `updateQuantity(quantity)`: Atualiza quantidade (valida maior que zero)
- `updateName(name)`: Atualiza nome do item
- `updateUnit(unit)`: Atualiza unidade de medida
- `updateCategory(category)`: Atualiza categoria do item
- `isPurchased()`: Retorna true se status for PURCHASED
- `isPending()`: Retorna true se status for PENDING

### Value Object: Category

**Responsabilidades:**
- Representar categoria de um item de forma imutável
- Fornecer categorias padrão do sistema

**Atributos:**
- id: Long (identificador único)
- name: String (nome da categoria, obrigatório, 2-50 caracteres)
- createdAt: Instant (timestamp de criação, imutável)

**Invariantes:**
- Nome da categoria não pode ser vazio ou nulo
- Nome deve ser único no sistema

**Categorias Padrão (Seeds):**
- FRUTAS: Frutas e verduras
- LEGUMES: Legumes e hortaliças
- CARNES: Carnes e aves
- LATICINIOS: Leites, queijos e derivados
- PADARIA: Pães, bolos e massas
- BEBIDAS: Bebidas em geral
- LIMPEZA: Produtos de limpeza
- HIGIENE: Produtos de higiene pessoal
- OUTROS: Itens diversos

### Enum: ItemStatus

**Valores:**
- `PENDING`: Item não foi comprado ainda (padrão)
- `PURCHASED`: Item foi comprado

---

## Regras de Negócio (Invariantes)

### ShoppingList

1. Nome da lista é obrigatório e deve ter entre 3 e 100 caracteres
2. Descrição é opcional, mas se informada deve ter no máximo 255 caracteres
3. Dono da lista é obrigatório e deve ser um usuário válido
4. Não é permitido adicionar itens com nomes duplicados na mesma lista (case-insensitive)
5. Lista pode ter no máximo 100 itens
6. Apenas o dono da lista pode modificá-la ou deletá-la
7. Ao remover uma lista, todos os seus itens são removidos em cascata (órfãos não podem existir)

### ListItem

1. Nome do item é obrigatório e deve ter entre 2 e 100 caracteres
2. Quantidade é obrigatória e deve ser maior que zero
3. Unidade de medida é opcional, mas se informada deve ter no máximo 20 caracteres
4. Categoria é opcional (pode ser NULL para itens sem categoria)
5. Status padrão de um item novo é PENDING
6. Item não pode existir sem uma lista pai (relacionamento obrigatório)
7. Ao marcar item como comprado, timestamp updatedAt é atualizado

### Category

1. Nome da categoria é obrigatório e deve ter entre 2 e 50 caracteres
2. Nome da categoria deve ser único no sistema
3. Categorias padrão são criadas via seed no banco (migrations)
4. Categorias são imutáveis após criação (apenas leitura)

---

## Eventos de Domínio (Futuros)

Estes eventos podem ser implementados no futuro para suportar features avançadas como auditoria, notificações e integração com outros sistemas.

**ListCreated**
- Disparado quando uma nova lista é criada
- Payload: listId, ownerId, listName, timestamp

**ItemAdded**
- Disparado quando um item é adicionado à lista
- Payload: listId, itemId, itemName, quantity, timestamp

**ItemMarkedAsPurchased**
- Disparado quando um item é marcado como comprado
- Payload: listId, itemId, itemName, timestamp

**ItemMarkedAsPending**
- Disparado quando um item é desmarcado (volta para pending)
- Payload: listId, itemId, itemName, timestamp

**ItemRemoved**
- Disparado quando um item é removido da lista
- Payload: listId, itemId, itemName, timestamp

**ListDeleted**
- Disparado quando uma lista é deletada
- Payload: listId, ownerId, listName, itemCount, timestamp

**AllPurchasedItemsCleared**
- Disparado quando todos os itens comprados são removidos
- Payload: listId, removedCount, timestamp

---

## Repositories (Ports)

### ShoppingListRepository

Interface que define operações de persistência para ShoppingList.

**Métodos:**
- `save(shoppingList)`: Persiste ou atualiza lista
- `findById(id)`: Busca lista por ID
- `findByOwnerId(ownerId)`: Busca todas as listas de um usuário
- `delete(shoppingList)`: Remove lista do banco
- `existsByIdAndOwnerId(listId, ownerId)`: Verifica se lista pertence ao usuário

### ListItemRepository

Interface que define operações de persistência para ListItem.

**Métodos:**
- `save(listItem)`: Persiste ou atualiza item
- `findById(id)`: Busca item por ID
- `findByShoppingListId(listId)`: Busca todos os itens de uma lista
- `delete(listItem)`: Remove item do banco
- `existsByNameAndShoppingListId(name, listId)`: Verifica duplicatas na lista

### CategoryRepository

Interface que define operações de persistência para Category.

**Métodos:**
- `findAll()`: Retorna todas as categorias do sistema
- `findById(id)`: Busca categoria por ID
- `findByName(name)`: Busca categoria por nome
- `existsByName(name)`: Verifica se categoria existe

---

## Diagramas

### Relacionamento entre Entidades

```
User (1) ------- (N) ShoppingList
                        |
                        | (1)
                        |
                        | (N)
                     ListItem
                        |
                        | (N)
                        |
                        | (1)
                     Category
```

### Ciclo de Vida de um Item

```
[CRIADO] --> [PENDING] --> [PURCHASED] --> [REMOVIDO]
                 ^              |
                 |              |
                 +--------------+
                  (desmarca)
```

---

## Validações de Input (DTOs)

### CreateShoppingListRequest
- name: obrigatório, 3-100 caracteres
- description: opcional, até 255 caracteres

### UpdateShoppingListRequest
- name: opcional, 3-100 caracteres (se informado)
- description: opcional, até 255 caracteres (se informado)

### AddItemRequest
- name: obrigatório, 2-100 caracteres
- quantity: obrigatório, maior que zero
- unit: opcional, até 20 caracteres
- categoryId: opcional, deve existir no banco

### UpdateItemRequest
- name: opcional, 2-100 caracteres (se informado)
- quantity: opcional, maior que zero (se informado)
- unit: opcional, até 20 caracteres (se informado)
- categoryId: opcional, deve existir no banco (se informado)

---

## Casos de Uso (Application Layer)

### Lista de Compras

**CreateShoppingListUseCase**
- Cria nova lista para o usuário autenticado
- Valida nome e descrição
- Retorna lista criada com ID

**UpdateShoppingListUseCase**
- Atualiza nome e/ou descrição da lista
- Valida que usuário é o dono da lista
- Retorna lista atualizada

**DeleteShoppingListUseCase**
- Remove lista do sistema
- Valida que usuário é o dono da lista
- Remove todos os itens em cascata

**GetShoppingListUseCase**
- Busca lista por ID
- Valida que usuário é o dono da lista
- Retorna lista com todos os itens

**ListUserShoppingListsUseCase**
- Lista todas as listas do usuário autenticado
- Ordena por data de criação (mais recentes primeiro)
- Retorna resumo (ID, nome, total de itens, itens pendentes)

### Itens da Lista

**AddItemToListUseCase**
- Adiciona novo item à lista
- Valida que usuário é o dono da lista
- Valida que não existe item com mesmo nome
- Valida quantidade e categoria (se informada)
- Retorna item criado

**UpdateListItemUseCase**
- Atualiza atributos do item (nome, quantidade, unidade, categoria)
- Valida que usuário é o dono da lista
- Valida novos valores
- Retorna item atualizado

**MarkItemAsPurchasedUseCase**
- Marca item como comprado
- Valida que usuário é o dono da lista
- Retorna item atualizado

**MarkItemAsPendingUseCase**
- Desmarca item (volta para pending)
- Valida que usuário é o dono da lista
- Retorna item atualizado

**RemoveItemFromListUseCase**
- Remove item da lista
- Valida que usuário é o dono da lista
- Não retorna conteúdo (204 No Content)

**ClearPurchasedItemsUseCase**
- Remove todos os itens marcados como comprados
- Valida que usuário é o dono da lista
- Retorna quantidade de itens removidos

### Categorias

**ListCategoriesUseCase**
- Lista todas as categorias disponíveis no sistema
- Não requer autenticação (endpoint público)
- Retorna lista de categorias ordenada por nome

---

## Endpoints REST (Interface Layer)

### Shopping Lists

```
GET    /api/v1/lists              - Lista todas as listas do usuário
POST   /api/v1/lists              - Cria nova lista
GET    /api/v1/lists/{id}         - Busca lista por ID
PUT    /api/v1/lists/{id}         - Atualiza lista
DELETE /api/v1/lists/{id}         - Remove lista
```

### List Items

```
POST   /api/v1/lists/{listId}/items              - Adiciona item à lista
PUT    /api/v1/lists/{listId}/items/{itemId}     - Atualiza item
PATCH  /api/v1/lists/{listId}/items/{itemId}/purchase  - Marca como comprado
PATCH  /api/v1/lists/{listId}/items/{itemId}/unpurchase - Marca como pending
DELETE /api/v1/lists/{listId}/items/{itemId}     - Remove item
DELETE /api/v1/lists/{listId}/items/purchased    - Remove todos comprados
```

### Categories

```
GET    /api/v1/categories         - Lista todas as categorias (público)
```

---

## Estrutura de Pacotes

```
domain/
├── shoppinglist/
│   ├── ShoppingList.java              (Aggregate Root)
│   ├── ListItem.java                  (Entity)
│   ├── Category.java                  (Value Object)
│   ├── ItemStatus.java                (Enum)
│   ├── ShoppingListRepository.java    (Port)
│   ├── ListItemRepository.java        (Port)
│   └── CategoryRepository.java        (Port)

application/
├── dto/
│   ├── CreateShoppingListRequest.java
│   ├── UpdateShoppingListRequest.java
│   ├── ShoppingListResponse.java
│   ├── AddItemRequest.java
│   ├── UpdateItemRequest.java
│   ├── ListItemResponse.java
│   └── CategoryResponse.java
├── usecase/
│   ├── CreateShoppingListUseCase.java
│   ├── UpdateShoppingListUseCase.java
│   ├── DeleteShoppingListUseCase.java
│   ├── GetShoppingListUseCase.java
│   ├── ListUserShoppingListsUseCase.java
│   ├── AddItemToListUseCase.java
│   ├── UpdateListItemUseCase.java
│   ├── MarkItemAsPurchasedUseCase.java
│   ├── MarkItemAsPendingUseCase.java
│   ├── RemoveItemFromListUseCase.java
│   ├── ClearPurchasedItemsUseCase.java
│   └── ListCategoriesUseCase.java

infrastructure/
├── persistence/
│   └── shoppinglist/
│       ├── JpaShoppingListRepository.java    (Adapter)
│       ├── JpaListItemRepository.java        (Adapter)
│       └── JpaCategoryRepository.java        (Adapter)

interfaces/
└── rest/
    └── v1/
        ├── ShoppingListController.java
        ├── ListItemController.java
        └── CategoryController.java
```

---

## Migrations (Database Schema)

### V7__create_categories.sql
Cria tabela de categorias e insere seeds das categorias padrão.

### V8__create_shopping_lists.sql
Cria tabela de listas de compras com referência ao dono (user_id).

### V9__create_list_items.sql
Cria tabela de itens com referência à lista e categoria.

---

## Notas de Implementação

1. **Factory Methods**: Sempre use factory methods (`create()`) para criar entidades, nunca construtores públicos
2. **Encapsulamento**: Setters são privados, apenas métodos de negócio públicos
3. **Validações**: Validações são feitas no domínio (entidades) e na camada de aplicação (DTOs)
4. **Imutabilidade**: Timestamps `createdAt` são imutáveis (final, não pode ser alterado)
5. **Relacionamentos**: Aggregate Root (ShoppingList) gerencia ciclo de vida dos itens (cascade)
6. **Autorização**: Sempre validar que usuário autenticado é o dono da lista antes de qualquer operação

---

## Próximos Passos

1. Implementar entidades do domínio (ShoppingList, ListItem, Category, ItemStatus)
2. Criar repositories (ports)
3. Implementar adapters JPA (persistence layer)
4. Criar migrations Flyway (V7, V8, V9)
5. Implementar casos de uso (application layer)
6. Criar DTOs de request/response
7. Implementar controllers REST (interface layer)
8. Escrever testes unitários do domínio
9. Escrever testes de integração dos casos de uso
10. Escrever testes end-to-end dos endpoints REST

---

**Versão:** 1.0.0
**Data:** 27/12/2025
**Status:** Aprovado para implementação
