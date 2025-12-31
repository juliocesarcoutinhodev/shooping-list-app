// Use case para deletar uma lista de compras
// Sem dependência de React/Expo, apenas lógica de domínio

import { ShoppingListRepository } from '../repositories';

export class DeleteShoppingListUseCase {
  constructor(private readonly repository: ShoppingListRepository) {}

  async execute(listId: string): Promise<void> {
    // Validação básica
    if (!listId || typeof listId !== 'string' || listId.trim().length === 0) {
      throw new Error('ID da lista é obrigatório');
    }

    // Delega para o repository
    await this.repository.delete(listId.trim());
  }
}
