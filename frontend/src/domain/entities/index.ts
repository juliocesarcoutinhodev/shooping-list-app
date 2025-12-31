/**
 * Domain Layer - Entities
 *
 * Business entities representing the core data models of the application.
 * These are pure TypeScript interfaces/types with business logic.
 */

export interface User {
  id: string;
  email: string;
  name: string;
  provider: 'LOCAL' | 'GOOGLE';
  status: 'ACTIVE' | 'INACTIVE';
  createdAt: string;
  updatedAt?: string;
}

export interface AuthSession {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: User;
}

export interface ShoppingItem {
  id: string;
  name: string;
  quantity: number;
  unitPrice?: number;
  isPurchased: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ShoppingList {
  id: string;
  title: string;
  description?: string;
  items: ShoppingItem[];
  itemsCount?: number;
  pendingItemsCount?: number;
  createdAt: string;
  updatedAt: string;
}
