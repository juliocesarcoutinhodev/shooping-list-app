/**
 * ToggleItemPurchasedUseCase
 *
 * Caso de uso para alternar o status de comprado/não comprado de um item.
 * Implementa atualização otimista e reversão em caso de erro.
 */

import { ShoppingItem } from '../entities';
import { ShoppingListRepository } from '../repositories';

export interface ToggleItemPurchasedInput {
  listId: string;
  itemId: string;
  isPurchased: boolean;
}

export class ToggleItemPurchasedUseCase {
  constructor(private readonly repository: ShoppingListRepository) {}

  async execute(input: ToggleItemPurchasedInput): Promise<ShoppingItem> {
    // Validações de entrada
    if (!input.listId || !input.listId.trim()) {
      throw new Error('ID da lista é obrigatório');
    }

    if (!input.itemId || !input.itemId.trim()) {
      throw new Error('ID do item é obrigatório');
    }

    // Verifica se a lista existe
    const list = await this.repository.getById(input.listId);
    if (!list) {
      throw new Error('Lista não encontrada');
    }

    // Verifica se o item existe na lista
    const item = list.items.find(i => i.id === input.itemId);
    if (!item) {
      throw new Error('Item não encontrado na lista');
    }

    // Atualiza o item via repositório
    // O backend espera status: "PENDING" ou "PURCHASED"
    return this.repository.updateItem(input.listId, input.itemId, {
      status: input.isPurchased ? 'PURCHASED' : 'PENDING',
    });
  }
}
