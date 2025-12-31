/**
 * Domain Layer - Use Cases
 *
 * Business logic and application-specific rules.
 * Orchestrate data flow between repositories and presentation layer.
 */

import { ShoppingItem, ShoppingList } from '../entities';
import { ShoppingItemRepository, ShoppingListRepository } from '../repositories';

export * from './add-item-to-list-use-case';
export * from './create-list-use-case';
export * from './delete-shopping-list-use-case';
export * from './delete-shopping-item-use-case';
export * from './get-list-details-use-case';
export * from './get-my-lists-use-case';
export * from './toggle-item-purchased-use-case';
export * from './update-shopping-item-use-case';

export class GetShoppingListsUseCase {
  constructor(private shoppingListRepository: ShoppingListRepository) {}

  async execute(): Promise<ShoppingList[]> {
    return this.shoppingListRepository.getAll();
  }
}

export class CreateShoppingListUseCase {
  constructor(private shoppingListRepository: ShoppingListRepository) {}

  async execute(title: string): Promise<ShoppingList> {
    if (!title.trim()) {
      throw new Error('List title cannot be empty');
    }

    return this.shoppingListRepository.create({
      title: title.trim(),
      items: [],
    });
  }
}

export class ToggleItemCompletionUseCase {
  constructor(private shoppingItemRepository: ShoppingItemRepository) {}

  async execute(itemId: string): Promise<ShoppingItem> {
    return this.shoppingItemRepository.update(itemId, {
      isPurchased: true, // This would typically fetch current state and toggle
    });
  }
}
