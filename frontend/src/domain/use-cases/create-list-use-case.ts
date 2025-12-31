// Use case para criar uma nova lista de compras
// Valida dados e delega ao repositório

import { ShoppingList } from '../entities';
import { ShoppingListRepository } from '../repositories';

export interface CreateListInput {
  title: string;
  description?: string;
}

export class CreateListUseCase {
  constructor(private readonly repository: ShoppingListRepository) {}

  async execute(input: CreateListInput): Promise<ShoppingList> {
    // Valido regras de negócio básicas
    if (!input.title || input.title.trim().length === 0) {
      throw new Error('Título é obrigatório');
    }

    if (input.title.length < 3) {
      throw new Error('Título deve ter no mínimo 3 caracteres');
    }

    if (input.title.length > 100) {
      throw new Error('Título deve ter no máximo 100 caracteres');
    }

    if (input.description && input.description.length > 255) {
      throw new Error('Descrição deve ter no máximo 255 caracteres');
    }

    // Delego criação ao repositório
    return await this.repository.create({
      title: input.title.trim(),
      description: input.description?.trim(),
      items: [],
    });
  }
}
