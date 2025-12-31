/**
 * Testes unitários para ToggleItemPurchasedUseCase
 *
 * Cobre:
 * - Toggle bem-sucedido (marcar como comprado)
 * - Toggle bem-sucedido (marcar como não comprado)
 * - Validações de entrada (listId, itemId)
 * - Lista não encontrada
 * - Item não encontrado na lista
 * - Propagação de erros do repository
 */

import { ShoppingItem, ShoppingList } from '../../entities';
import { ShoppingListRepository } from '../../repositories';
import { ToggleItemPurchasedUseCase } from '../toggle-item-purchased-use-case';

describe('ToggleItemPurchasedUseCase', () => {
  let mockRepository: jest.Mocked<ShoppingListRepository>;
  let useCase: ToggleItemPurchasedUseCase;

  beforeEach(() => {
    mockRepository = {
      getMyLists: jest.fn(),
      getAll: jest.fn(),
      getById: jest.fn(),
      create: jest.fn(),
      update: jest.fn(),
      delete: jest.fn(),
      addItem: jest.fn(),
      updateItem: jest.fn(),
      deleteItem: jest.fn(),
    };
    useCase = new ToggleItemPurchasedUseCase(mockRepository);
  });

  describe('Toggle bem-sucedido', () => {
    it('deve marcar item como comprado', async () => {
      const mockList: ShoppingList = {
        id: '1',
        title: 'Minha Lista',
        items: [
          {
            id: 'item1',
            name: 'Arroz',
            quantity: 2,
            isPurchased: false,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
          },
        ],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      const mockUpdatedItem: ShoppingItem = {
        id: 'item1',
        name: 'Arroz',
        quantity: 2,
        isPurchased: true,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      mockRepository.getById.mockResolvedValue(mockList);
      mockRepository.updateItem.mockResolvedValue(mockUpdatedItem);

      const result = await useCase.execute({
        listId: '1',
        itemId: 'item1',
        isPurchased: true,
      });

      expect(result).toEqual(mockUpdatedItem);
      expect(result.isPurchased).toBe(true);
      expect(mockRepository.getById).toHaveBeenCalledWith('1');
      expect(mockRepository.updateItem).toHaveBeenCalledWith('1', 'item1', {
        status: 'PURCHASED',
      });
    });

    it('deve marcar item como não comprado', async () => {
      const mockList: ShoppingList = {
        id: '1',
        title: 'Minha Lista',
        items: [
          {
            id: 'item1',
            name: 'Arroz',
            quantity: 2,
            isPurchased: true,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
          },
        ],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      const mockUpdatedItem: ShoppingItem = {
        id: 'item1',
        name: 'Arroz',
        quantity: 2,
        isPurchased: false,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      mockRepository.getById.mockResolvedValue(mockList);
      mockRepository.updateItem.mockResolvedValue(mockUpdatedItem);

      const result = await useCase.execute({
        listId: '1',
        itemId: 'item1',
        isPurchased: false,
      });

      expect(result).toEqual(mockUpdatedItem);
      expect(result.isPurchased).toBe(false);
      expect(mockRepository.updateItem).toHaveBeenCalledWith('1', 'item1', {
        status: 'PENDING',
      });
    });
  });

  describe('Validações de entrada', () => {
    it('deve lançar erro se listId estiver vazio', async () => {
      await expect(
        useCase.execute({
          listId: '',
          itemId: 'item1',
          isPurchased: true,
        })
      ).rejects.toThrow('ID da lista é obrigatório');
      expect(mockRepository.getById).not.toHaveBeenCalled();
    });

    it('deve lançar erro se listId for apenas espaços', async () => {
      await expect(
        useCase.execute({
          listId: '   ',
          itemId: 'item1',
          isPurchased: true,
        })
      ).rejects.toThrow('ID da lista é obrigatório');
      expect(mockRepository.getById).not.toHaveBeenCalled();
    });

    it('deve lançar erro se itemId estiver vazio', async () => {
      await expect(
        useCase.execute({
          listId: '1',
          itemId: '',
          isPurchased: true,
        })
      ).rejects.toThrow('ID do item é obrigatório');
      expect(mockRepository.getById).not.toHaveBeenCalled();
    });

    it('deve lançar erro se itemId for apenas espaços', async () => {
      await expect(
        useCase.execute({
          listId: '1',
          itemId: '   ',
          isPurchased: true,
        })
      ).rejects.toThrow('ID do item é obrigatório');
      expect(mockRepository.getById).not.toHaveBeenCalled();
    });
  });

  describe('Validação de lista e item', () => {
    it('deve lançar erro se lista não for encontrada', async () => {
      mockRepository.getById.mockResolvedValue(null);

      await expect(
        useCase.execute({
          listId: '999',
          itemId: 'item1',
          isPurchased: true,
        })
      ).rejects.toThrow('Lista não encontrada');
      expect(mockRepository.getById).toHaveBeenCalledWith('999');
      expect(mockRepository.updateItem).not.toHaveBeenCalled();
    });

    it('deve lançar erro se item não for encontrado na lista', async () => {
      const mockList: ShoppingList = {
        id: '1',
        title: 'Minha Lista',
        items: [
          {
            id: 'item1',
            name: 'Arroz',
            quantity: 2,
            isPurchased: false,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
          },
        ],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      mockRepository.getById.mockResolvedValue(mockList);

      await expect(
        useCase.execute({
          listId: '1',
          itemId: 'item999',
          isPurchased: true,
        })
      ).rejects.toThrow('Item não encontrado na lista');
      expect(mockRepository.getById).toHaveBeenCalledWith('1');
      expect(mockRepository.updateItem).not.toHaveBeenCalled();
    });
  });

  describe('Propagação de erros', () => {
    it('deve propagar erro do repository.getById', async () => {
      const error = { message: 'Erro de rede', status: 500 };
      mockRepository.getById.mockRejectedValue(error);

      await expect(
        useCase.execute({
          listId: '1',
          itemId: 'item1',
          isPurchased: true,
        })
      ).rejects.toMatchObject(error);
      expect(mockRepository.updateItem).not.toHaveBeenCalled();
    });

    it('deve propagar erro do repository.updateItem', async () => {
      const mockList: ShoppingList = {
        id: '1',
        title: 'Minha Lista',
        items: [
          {
            id: 'item1',
            name: 'Arroz',
            quantity: 2,
            isPurchased: false,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
          },
        ],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      const error = { message: 'Item não encontrado', status: 404 };
      mockRepository.getById.mockResolvedValue(mockList);
      mockRepository.updateItem.mockRejectedValue(error);

      await expect(
        useCase.execute({
          listId: '1',
          itemId: 'item1',
          isPurchased: true,
        })
      ).rejects.toMatchObject(error);
    });

    it('deve propagar erro genérico do repository', async () => {
      const mockList: ShoppingList = {
        id: '1',
        title: 'Minha Lista',
        items: [
          {
            id: 'item1',
            name: 'Arroz',
            quantity: 2,
            isPurchased: false,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
          },
        ],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      const error = new Error('Erro desconhecido');
      mockRepository.getById.mockResolvedValue(mockList);
      mockRepository.updateItem.mockRejectedValue(error);

      await expect(
        useCase.execute({
          listId: '1',
          itemId: 'item1',
          isPurchased: true,
        })
      ).rejects.toThrow('Erro desconhecido');
    });
  });
});
