// Data source remoto para listas de compras
// Responsável por consumir GET /api/v1/lists usando o apiClient padrão

import { apiClient } from '@/src/infrastructure/http/apiClient';

import {
  AddItemRequestDto,
  ShoppingItemDto,
  ShoppingListDto,
  UpdateItemRequestDto,
} from '../models';

export interface CreateListDto {
  title: string;
  description?: string;
}

const API_V1_PREFIX = '/api/v1';

export class ShoppingListRemoteDataSource {
  async getMyLists(): Promise<ShoppingListDto[]> {
    try {
      return await apiClient.get<ShoppingListDto[]>(`${API_V1_PREFIX}/lists`);
    } catch (error) {
      // Normalização de erro conforme padrão do projeto
      if (error && typeof error === 'object' && 'response' in error) {
        const err = error as any;
        throw {
          message: err.response?.data?.message || 'Erro ao buscar listas',
          status: err.response?.status,
        };
      }
      throw { message: 'Erro desconhecido ao buscar listas' };
    }
  }

  /**
   * Busca uma lista específica por ID com todos os itens
   *
   * IMPORTANTE: Este endpoint (GET /api/v1/lists/{id}) precisa ser implementado no backend.
   * Atualmente o backend só possui:
   * - GET /api/v1/lists (retorna resumos sem itens)
   * - POST /api/v1/lists (criar)
   * - PATCH /api/v1/lists/{id} (atualizar)
   * - DELETE /api/v1/lists/{id} (deletar)
   *
   * Este método retornará erro 500 até que o endpoint seja implementado no backend.
   */
  async getListById(listId: string): Promise<ShoppingListDto> {
    try {
      return await apiClient.get<ShoppingListDto>(`${API_V1_PREFIX}/lists/${listId}`);
    } catch (error) {
      // Repasso erro já normalizado pelo apiClient
      throw error;
    }
  }

  async createList(data: CreateListDto): Promise<ShoppingListDto> {
    try {
      return await apiClient.post<ShoppingListDto>(`${API_V1_PREFIX}/lists`, data);
    } catch (error) {
      // Repasso erro já normalizado pelo apiClient
      throw error;
    }
  }

  async deleteList(listId: string): Promise<void> {
    try {
      await apiClient.delete(`${API_V1_PREFIX}/lists/${listId}`);
    } catch (error) {
      // Repasso erro já normalizado pelo apiClient
      throw error;
    }
  }

  /**
   * Adiciona um novo item em uma lista de compras
   * POST /api/v1/lists/{listId}/items
   */
  async addItem(listId: string, data: AddItemRequestDto): Promise<ShoppingItemDto> {
    try {
      return await apiClient.post<ShoppingItemDto>(`${API_V1_PREFIX}/lists/${listId}/items`, data);
    } catch (error) {
      // Repasso erro já normalizado pelo apiClient
      throw error;
    }
  }

  /**
   * Atualiza um item existente em uma lista de compras
   * PATCH /api/v1/lists/{listId}/items/{itemId}
   */
  async updateItem(
    listId: string,
    itemId: string,
    data: UpdateItemRequestDto
  ): Promise<ShoppingItemDto> {
    try {
      return await apiClient.patch<ShoppingItemDto>(
        `${API_V1_PREFIX}/lists/${listId}/items/${itemId}`,
        data
      );
    } catch (error) {
      // Repasso erro já normalizado pelo apiClient
      throw error;
    }
  }

  /**
   * Remove um item de uma lista de compras
   * DELETE /api/v1/lists/{listId}/items/{itemId}
   */
  async deleteItem(listId: string, itemId: string): Promise<void> {
    try {
      await apiClient.delete(`${API_V1_PREFIX}/lists/${listId}/items/${itemId}`);
    } catch (error) {
      // Repasso erro já normalizado pelo apiClient
      throw error;
    }
  }
}
