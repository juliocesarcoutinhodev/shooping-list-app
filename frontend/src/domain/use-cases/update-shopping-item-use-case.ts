/**
 * UpdateShoppingItemUseCase
 *
 * Caso de uso para atualizar um item existente em uma lista de compras.
 * Valida os dados de entrada e delega a atualização ao repositório.
 */

import { ShoppingItem } from '../entities';
import { ShoppingListRepository } from '../repositories';

export interface UpdateShoppingItemInput {
  listId: string;
  itemId: string;
  name?: string;
  quantity?: number;
  unit?: string;
  unitPrice?: number;
}

export class UpdateShoppingItemUseCase {
  constructor(private readonly repository: ShoppingListRepository) {}

  async execute(input: UpdateShoppingItemInput): Promise<ShoppingItem> {
    // Validações básicas
    if (!input.listId || !input.listId.trim()) {
      throw new Error('ID da lista é obrigatório');
    }

    if (!input.itemId || !input.itemId.trim()) {
      throw new Error('ID do item é obrigatório');
    }

    // Valida que pelo menos um campo foi fornecido
    const hasName = input.name !== undefined && input.name !== null && input.name.trim().length > 0;
    const hasQuantity = input.quantity !== undefined && input.quantity !== null;
    const hasUnit = input.unit !== undefined && input.unit !== null;
    const hasUnitPrice = input.unitPrice !== undefined && input.unitPrice !== null;

    if (!hasName && !hasQuantity && !hasUnit && !hasUnitPrice) {
      throw new Error('Pelo menos um campo deve ser fornecido para atualização');
    }

    // Validações de domínio para campos fornecidos
    if (hasName) {
      if (input.name!.trim().length < 2 || input.name!.trim().length > 80) {
        throw new Error('Nome do item deve ter entre 2 e 80 caracteres');
      }
    }

    if (hasQuantity && input.quantity! < 1) {
      throw new Error('Quantidade deve ser maior ou igual a 1');
    }

    if (hasUnitPrice && input.unitPrice! < 0) {
      throw new Error('Preço unitário não pode ser negativo');
    }

    // Verifica se a lista existe
    const list = await this.repository.getById(input.listId.trim());
    if (!list) {
      throw new Error('Lista não encontrada');
    }

    // Verifica se o item existe na lista
    const item = list.items.find(i => i.id === input.itemId.trim());
    if (!item) {
      throw new Error('Item não encontrado na lista');
    }

    // Prepara dados para atualização (apenas campos fornecidos)
    const updateData: {
      name?: string;
      quantity?: number;
      unit?: string;
      unitPrice?: number;
    } = {};

    if (hasName) {
      updateData.name = input.name!.trim();
    }

    if (hasQuantity) {
      updateData.quantity = input.quantity!;
    }

    if (hasUnit) {
      updateData.unit = input.unit!.trim() || undefined;
    }

    if (hasUnitPrice) {
      updateData.unitPrice = input.unitPrice! >= 0 ? input.unitPrice! : undefined;
    }

    // Atualiza o item via repositório
    return this.repository.updateItem(input.listId.trim(), input.itemId.trim(), updateData);
  }
}
