/**
 * Mapper para ShoppingItemDto -> ShoppingItem
 *
 * Responsável por converter dados do DTO (API) para a entidade de domínio.
 * Suporta tanto snake_case quanto camelCase para compatibilidade com diferentes formatos de API.
 * Validações garantem campos obrigatórios e tipos corretos.
 */

import { ShoppingItem } from '../../domain/entities';
import { ShoppingItemDto } from '../models';

/**
 * Converte um ShoppingItemDto para ShoppingItem (domínio)
 *
 * @param dto - Objeto vindo da API
 * @returns Entidade de domínio ShoppingItem
 * @throws Error se campos obrigatórios estiverem ausentes
 */
export function mapShoppingItemDtoToDomain(dto: ShoppingItemDto): ShoppingItem {
  // Suporto tanto camelCase quanto snake_case para compatibilidade
  const id = dto.id ? String(dto.id) : undefined;
  const createdAt = dto.createdAt || dto.created_at;
  const updatedAt = dto.updatedAt || dto.updated_at;
  // Trato null como undefined para manter consistência
  const unitPriceRaw = dto.unitPrice ?? dto.unit_price;
  const unitPrice = unitPriceRaw !== null && unitPriceRaw !== undefined ? unitPriceRaw : undefined;

  // Suporto múltiplos formatos de status:
  // 1. Campo status do backend: "PENDING" ou "PURCHASED"
  // 2. Campos booleanos: isPurchased, is_purchased, isCompleted, is_completed
  let isPurchased = false;
  if (dto.status) {
    // Backend retorna "PENDING" ou "PURCHASED"
    isPurchased = dto.status === 'PURCHASED';
  } else {
    // Fallback para campos booleanos (compatibilidade)
    isPurchased =
      dto.isPurchased ?? dto.is_purchased ?? dto.isCompleted ?? dto.is_completed ?? false;
  }

  // Valido campos obrigatórios
  if (!id || !dto.name || dto.quantity === undefined || !createdAt || !updatedAt) {
    const missingFields = [];
    if (!id) missingFields.push('id');
    if (!dto.name) missingFields.push('name');
    if (dto.quantity === undefined) missingFields.push('quantity');
    if (!createdAt) missingFields.push('createdAt/created_at');
    if (!updatedAt) missingFields.push('updatedAt/updated_at');

    throw new Error(`Campos obrigatórios ausentes em ShoppingItemDto: ${missingFields.join(', ')}`);
  }

  // Valido tipos básicos
  if (typeof dto.name !== 'string') {
    throw new Error('Campo name deve ser uma string');
  }

  if (typeof dto.quantity !== 'number' || dto.quantity < 0) {
    throw new Error('Campo quantity deve ser um número positivo');
  }

  // Valida unitPrice apenas se for fornecido (não null/undefined)
  if (
    unitPrice !== undefined &&
    unitPrice !== null &&
    (typeof unitPrice !== 'number' || unitPrice < 0)
  ) {
    throw new Error('Campo unitPrice deve ser um número positivo quando fornecido');
  }

  return {
    id,
    name: dto.name.trim(),
    quantity: dto.quantity,
    unitPrice,
    isPurchased,
    createdAt,
    updatedAt,
  };
}
