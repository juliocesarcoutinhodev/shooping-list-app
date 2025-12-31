/**
 * Domain Layer - Repository Interfaces
 *
 * Abstract contracts for data access.
 * These define what data operations are needed without caring about implementation.
 */

import { AuthSession, ShoppingItem, ShoppingList, User } from '../entities';

export interface AuthRepository {
  login(email: string, password: string): Promise<AuthSession>;
  register(name: string, email: string, password: string): Promise<AuthSession>;
  loginWithGoogle(idToken: string): Promise<AuthSession>;
  logout(refreshToken: string): Promise<void>;
  refreshToken(refreshToken: string): Promise<AuthSession>;
  getCurrentUser(): Promise<User>;
}

export interface ShoppingListRepository {
  getMyLists(): Promise<ShoppingList[]>;
  getAll(): Promise<ShoppingList[]>;
  getById(id: string): Promise<ShoppingList | null>;
  create(list: Omit<ShoppingList, 'id' | 'createdAt' | 'updatedAt'>): Promise<ShoppingList>;
  update(id: string, list: Partial<ShoppingList>): Promise<ShoppingList>;
  delete(id: string): Promise<void>;
  addItem(
    listId: string,
    item: { name: string; quantity: number; unit?: string; unitPrice?: number }
  ): Promise<ShoppingItem>;
  updateItem(
    listId: string,
    itemId: string,
    data: {
      status?: 'PENDING' | 'PURCHASED';
      name?: string;
      quantity?: number;
      unit?: string;
      unitPrice?: number;
    }
  ): Promise<ShoppingItem>;
  deleteItem(listId: string, itemId: string): Promise<void>;
}

export interface ShoppingItemRepository {
  create(
    listId: string,
    item: Omit<ShoppingItem, 'id' | 'createdAt' | 'updatedAt'>
  ): Promise<ShoppingItem>;
  update(id: string, item: Partial<ShoppingItem>): Promise<ShoppingItem>;
  delete(id: string): Promise<void>;
}

export interface UserRepository {
  getProfile(): Promise<User | null>;
  updateProfile(user: Partial<User>): Promise<User>;
}
