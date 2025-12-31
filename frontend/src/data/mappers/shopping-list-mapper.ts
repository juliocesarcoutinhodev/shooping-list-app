// Mapper para ShoppingListDto <-> ShoppingList
// Sigo o padrão do projeto, mantendo o domínio desacoplado dos DTOs e sem dependências externas.

import { ShoppingList } from '../../domain/entities';
import { ShoppingListDto } from '../models';

import { mapShoppingItemDtoToDomain } from './shopping-item-mapper';

export function mapShoppingListDtoToDomain(dto: ShoppingListDto): ShoppingList {
  // Suporto tanto camelCase quanto snake_case para compatibilidade
  const createdAt = dto.createdAt || dto.created_at;
  const updatedAt = dto.updatedAt || dto.updated_at;

  // Valido apenas campos realmente obrigatórios
  if (!dto.id || !dto.title || !createdAt || !updatedAt) {
    throw new Error('Campos obrigatórios ausentes em ShoppingListDto');
  }

  return {
    id: String(dto.id),
    title: dto.title,
    description: dto.description,
    // Items pode ser null/undefined, trato como array vazio
    items: Array.isArray(dto.items) ? dto.items.map(mapShoppingItemDtoToDomain) : [],
    // Campos de contagem vindos da API (útil quando items não está incluído)
    itemsCount: dto.itemsCount,
    pendingItemsCount: dto.pendingItemsCount,
    createdAt,
    updatedAt,
  };
}

// Se necessário, pode-se criar o caminho inverso (domain -> dto) futuramente.
