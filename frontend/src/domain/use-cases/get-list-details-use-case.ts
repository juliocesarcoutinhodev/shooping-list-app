/**
 * Use Case: Buscar Detalhes de uma Lista
 *
 * Orquestra a busca de uma lista específica por ID, incluindo todos os itens.
 * Responsável por aplicar regras de negócio, validações e ordenação.
 */

import { ShoppingList } from '../entities';
import { ShoppingListRepository } from '../repositories';

export class GetListDetailsUseCase {
  constructor(private readonly repository: ShoppingListRepository) {}

  /**
   * Busca uma lista específica por ID e ordena os itens
   *
   * Regra de ordenação:
   * 1. Itens não comprados (isPurchased: false) primeiro
   * 2. Depois itens comprados (isPurchased: true)
   * 3. Dentro de cada grupo, ordenar por updatedAt desc (mais recente primeiro)
   *
   * @param listId - ID da lista a ser buscada
   * @returns Lista com itens ordenados ou null se não encontrada
   * @throws Error com status e message normalizados em caso de erro
   */
  async execute(listId: string): Promise<ShoppingList | null> {
    // Valido entrada
    if (!listId || listId.trim().length === 0) {
      throw new Error('ID da lista é obrigatório');
    }

    // Busco no repository (já retorna com items mapeados)
    const list = await this.repository.getById(listId.trim());

    // Se lista não encontrada, retorno null
    if (!list) {
      return null;
    }

    // Aplico regras de ordenação nos itens
    const sortedItems = [...list.items].sort((a, b) => {
      // Primeiro critério: isPurchased (false antes de true)
      if (a.isPurchased !== b.isPurchased) {
        return a.isPurchased ? 1 : -1;
      }

      // Segundo critério: updatedAt desc (mais recente primeiro)
      return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime();
    });

    // Retorno lista com itens ordenados
    return {
      ...list,
      items: sortedItems,
    };
  }
}
