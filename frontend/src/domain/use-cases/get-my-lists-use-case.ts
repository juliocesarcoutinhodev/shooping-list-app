// Use case para buscar listas do usuário autenticado, ordenando por updatedAt desc
// Sem dependência de React/Expo, apenas lógica de domínio

import { ShoppingList } from '../entities';
import { ShoppingListRepository } from '../repositories';

export class GetMyListsUseCase {
  constructor(private readonly repository: ShoppingListRepository) {}

  async execute(): Promise<ShoppingList[]> {
    const lists = await this.repository.getMyLists();
    // Ordena por updatedAt desc (mais recente primeiro)
    return (lists ?? []).slice().sort((a: ShoppingList, b: ShoppingList) => {
      return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime();
    });
  }
}
