// Use case para deletar um item de uma lista de compras
// Sem dependência de React/Expo, apenas lógica de domínio

import { ShoppingListRepository } from '../repositories';

export class DeleteShoppingItemUseCase {
  constructor(private readonly repository: ShoppingListRepository) {}

  async execute(listId: string, itemId: string): Promise<void> {
    // Validação básica
    if (!listId || typeof listId !== 'string' || listId.trim().length === 0) {
      throw new Error('ID da lista é obrigatório');
    }

    if (!itemId || typeof itemId !== 'string' || itemId.trim().length === 0) {
      throw new Error('ID do item é obrigatório');
    }

    // Delega para o repository
    await this.repository.deleteItem(listId.trim(), itemId.trim());
  }
}
