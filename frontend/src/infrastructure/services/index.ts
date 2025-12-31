/**
 * Infrastructure Layer - External Services
 *
 * Concrete implementations of data sources using infrastructure components.
 */

import { LocalDataSource, RemoteDataSource } from '../../data/data-sources';
import {
  AddItemRequestDto,
  CreateShoppingListRequest,
  ShoppingItemDto,
  ShoppingListDto,
  UpdateItemRequestDto,
  UserDto,
} from '../../data/models';
import { HttpClient } from '../http';
import { CacheService } from '../storage';

export * from './auth-service';
export * from './auth-service-instance';
export * from './google-auth-service';
export * from './user-service';
export * from './user-service-instance';

export class ApiRemoteDataSource implements RemoteDataSource {
  constructor(private httpClient: HttpClient) {}

  async getShoppingLists(): Promise<ShoppingListDto[]> {
    return this.httpClient.get<ShoppingListDto[]>('/shopping-lists');
  }

  async getShoppingList(id: string): Promise<ShoppingListDto> {
    return this.httpClient.get<ShoppingListDto>(`/shopping-lists/${id}`);
  }

  async createShoppingList(request: CreateShoppingListRequest): Promise<ShoppingListDto> {
    return this.httpClient.post<ShoppingListDto>('/shopping-lists', request);
  }

  async updateShoppingList(
    id: string,
    request: Partial<CreateShoppingListRequest>
  ): Promise<ShoppingListDto> {
    return this.httpClient.put<ShoppingListDto>(`/shopping-lists/${id}`, request);
  }

  async deleteShoppingList(id: string): Promise<void> {
    await this.httpClient.delete(`/shopping-lists/${id}`);
  }

  async createShoppingItem(listId: string, request: AddItemRequestDto): Promise<ShoppingItemDto> {
    return this.httpClient.post<ShoppingItemDto>(`/shopping-lists/${listId}/items`, request);
  }

  async updateShoppingItem(id: string, request: UpdateItemRequestDto): Promise<ShoppingItemDto> {
    return this.httpClient.put<ShoppingItemDto>(`/shopping-items/${id}`, request);
  }

  async deleteShoppingItem(id: string): Promise<void> {
    await this.httpClient.delete(`/shopping-items/${id}`);
  }

  async getUserProfile(): Promise<UserDto> {
    return this.httpClient.get<UserDto>('/user/profile');
  }

  async updateUserProfile(request: Partial<UserDto>): Promise<UserDto> {
    return this.httpClient.put<UserDto>('/user/profile', request);
  }
}

export class CacheLocalDataSource implements LocalDataSource {
  private static readonly SHOPPING_LISTS_CACHE_KEY = 'shopping_lists';
  private static readonly OFFLINE_ITEMS_KEY = 'offline_items';

  constructor(private cacheService: CacheService) {}

  async cacheShoppingLists(lists: ShoppingListDto[]): Promise<void> {
    await this.cacheService.set(CacheLocalDataSource.SHOPPING_LISTS_CACHE_KEY, lists);
  }

  async getCachedShoppingLists(): Promise<ShoppingListDto[]> {
    const cached = await this.cacheService.get<ShoppingListDto[]>(
      CacheLocalDataSource.SHOPPING_LISTS_CACHE_KEY
    );
    return cached || [];
  }

  async clearCache(): Promise<void> {
    await this.cacheService.clear();
  }

  async saveOfflineItem(listId: string, item: AddItemRequestDto): Promise<void> {
    const existingItems = await this.getOfflineItems();
    const newItem = { listId, item };
    const updatedItems = [...existingItems, newItem];

    await this.cacheService.set(CacheLocalDataSource.OFFLINE_ITEMS_KEY, updatedItems);
  }

  async getOfflineItems(): Promise<{ listId: string; item: AddItemRequestDto }[]> {
    const items = await this.cacheService.get<{ listId: string; item: AddItemRequestDto }[]>(
      CacheLocalDataSource.OFFLINE_ITEMS_KEY
    );
    return items || [];
  }

  async clearOfflineItems(): Promise<void> {
    await this.cacheService.remove(CacheLocalDataSource.OFFLINE_ITEMS_KEY);
  }
}
