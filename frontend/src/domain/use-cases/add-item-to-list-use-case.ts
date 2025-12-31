/**
 * AddItemToListUseCase
 *
 * Caso de uso para adicionar um novo item em uma lista de compras.
 * Valida os dados de entrada e delega a criação ao repositório.
 */

import { ShoppingItem } from '../entities';
import { ShoppingListRepository } from '../repositories';

export interface AddItemToListInput {
  listId: string;
  name: string;
  quantity: number;
  unit?: string;
  unitPrice?: number;
}

export class AddItemToListUseCase {
  constructor(private readonly repository: ShoppingListRepository) {}

  async execute(input: AddItemToListInput): Promise<ShoppingItem> {
    // Validações de domínio
    if (!input.name || !input.name.trim()) {
      throw new Error('Nome do item é obrigatório');
    }

    if (input.name.trim().length < 2 || input.name.trim().length > 80) {
      throw new Error('Nome do item deve ter entre 2 e 80 caracteres');
    }

    if (!input.quantity || input.quantity < 1) {
      throw new Error('Quantidade deve ser maior ou igual a 1');
    }

    if (input.unitPrice !== undefined && input.unitPrice !== null && input.unitPrice < 0) {
      throw new Error('Preço unitário não pode ser negativo');
    }

    // Verifica se a lista existe
    const list = await this.repository.getById(input.listId);
    if (!list) {
      throw new Error('Lista não encontrada');
    }

    // Adiciona o item via repositório
    return this.repository.addItem(input.listId, {
      name: input.name.trim(),
      quantity: input.quantity,
      unit: input.unit?.trim() || undefined,
      unitPrice:
        input.unitPrice !== undefined && input.unitPrice !== null ? input.unitPrice : undefined,
    });
  }
}
