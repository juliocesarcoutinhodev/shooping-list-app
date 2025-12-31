// Implementação concreta do repositório de listas de compras
// Usa o data source remoto e faz o mapeamento DTO -> domínio

import { ShoppingItem, ShoppingList } from '@/src/domain/entities';

import { ShoppingListRemoteDataSource } from '../data-sources/shopping-list-remote-data-source';
import { mapShoppingItemDtoToDomain } from '../mappers/shopping-item-mapper';
import { mapShoppingListDtoToDomain } from '../mappers/shopping-list-mapper';
import { AddItemRequestDto, UpdateItemRequestDto } from '../models';

export class ShoppingListRepositoryImpl {
  constructor(private readonly remote: ShoppingListRemoteDataSource) {}

  async getMyLists(): Promise<ShoppingList[]> {
    try {
      const dtos = await this.remote.getMyLists();
      return dtos.map(mapShoppingListDtoToDomain);
    } catch (error) {
      // Repassa erro já normalizado
      throw error;
    }
  }

  async getAll(): Promise<ShoppingList[]> {
    return this.getMyLists();
  }

  async getById(id: string): Promise<ShoppingList | null> {
    try {
      const dto = await this.remote.getListById(id);
      return mapShoppingListDtoToDomain(dto);
    } catch (error) {
      // Se for 404, retorno null conforme contrato
      if (error && typeof error === 'object' && 'status' in error) {
        const err = error as { status?: number };
        if (err.status === 404) {
          return null;
        }
      }
      // Repassa outros erros já normalizados
      throw error;
    }
  }

  async create(list: Omit<ShoppingList, 'id' | 'createdAt' | 'updatedAt'>): Promise<ShoppingList> {
    try {
      const dto = await this.remote.createList({
        title: list.title,
        description: list.description,
      });
      return mapShoppingListDtoToDomain(dto);
    } catch (error) {
      // Repassa erro já normalizado
      throw error;
    }
  }

  async update(_id: string, _list: Partial<ShoppingList>): Promise<ShoppingList> {
    // Implementar quando backend tiver endpoint
    throw new Error('Not implemented');
  }

  async delete(id: string): Promise<void> {
    try {
      await this.remote.deleteList(id);
    } catch (error) {
      // Repassa erro já normalizado
      throw error;
    }
  }

  async addItem(listId: string, item: AddItemRequestDto): Promise<ShoppingItem> {
    try {
      const dto = await this.remote.addItem(listId, item);
      return mapShoppingItemDtoToDomain(dto);
    } catch (error) {
      // Repassa erro já normalizado
      throw error;
    }
  }

  async updateItem(
    listId: string,
    itemId: string,
    data: UpdateItemRequestDto
  ): Promise<ShoppingItem> {
    try {
      const dto = await this.remote.updateItem(listId, itemId, data);
      return mapShoppingItemDtoToDomain(dto);
    } catch (error) {
      // Repassa erro já normalizado
      throw error;
    }
  }

  async deleteItem(listId: string, itemId: string): Promise<void> {
    try {
      await this.remote.deleteItem(listId, itemId);
    } catch (error) {
      // Repassa erro já normalizado
      throw error;
    }
  }
}
