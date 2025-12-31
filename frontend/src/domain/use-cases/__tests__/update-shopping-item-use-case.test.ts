/**
 * Testes unitários para UpdateShoppingItemUseCase
 *
 * Cobre:
 * - Atualização bem-sucedida de item
 * - Validações de entrada (listId, itemId)
 * - Validações de negócio (nome, quantidade, preço)
 * - Validação de pelo menos um campo fornecido
 * - Lista não encontrada
 * - Item não encontrado na lista
 * - Propagação de erros do repository
 */

import { ShoppingItem, ShoppingList } from '../../entities';
import { ShoppingListRepository } from '../../repositories';
import { UpdateShoppingItemUseCase } from '../update-shopping-item-use-case';

describe('UpdateShoppingItemUseCase', () => {
  let mockRepository: jest.Mocked<ShoppingListRepository>;
  let useCase: UpdateShoppingItemUseCase;

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
    useCase = new UpdateShoppingItemUseCase(mockRepository);
  });

  describe('Atualização bem-sucedida', () => {
    it('deve atualizar item com nome válido', async () => {
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
        name: 'Arroz Integral',
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
        name: 'Arroz Integral',
      });

      expect(result).toEqual(mockUpdatedItem);
      expect(mockRepository.getById).toHaveBeenCalledWith('1');
      expect(mockRepository.updateItem).toHaveBeenCalledWith('1', 'item1', {
        name: 'Arroz Integral',
      });
    });

    it('deve atualizar item com quantidade válida', async () => {
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
        quantity: 5,
        isPurchased: false,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      mockRepository.getById.mockResolvedValue(mockList);
      mockRepository.updateItem.mockResolvedValue(mockUpdatedItem);

      const result = await useCase.execute({
        listId: '1',
        itemId: 'item1',
        quantity: 5,
      });

      expect(result).toEqual(mockUpdatedItem);
      expect(mockRepository.updateItem).toHaveBeenCalledWith('1', 'item1', {
        quantity: 5,
      });
    });

    it('deve atualizar item com preço unitário válido', async () => {
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
        unitPrice: 4.99,
        isPurchased: false,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      mockRepository.getById.mockResolvedValue(mockList);
      mockRepository.updateItem.mockResolvedValue(mockUpdatedItem);

      const result = await useCase.execute({
        listId: '1',
        itemId: 'item1',
        unitPrice: 4.99,
      });

      expect(result).toEqual(mockUpdatedItem);
      expect(mockRepository.updateItem).toHaveBeenCalledWith('1', 'item1', {
        unitPrice: 4.99,
      });
    });

    it('deve atualizar item com múltiplos campos', async () => {
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
        name: 'Arroz Integral',
        quantity: 5,
        unitPrice: 4.99,
        isPurchased: false,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      mockRepository.getById.mockResolvedValue(mockList);
      mockRepository.updateItem.mockResolvedValue(mockUpdatedItem);

      const result = await useCase.execute({
        listId: '1',
        itemId: 'item1',
        name: 'Arroz Integral',
        quantity: 5,
        unitPrice: 4.99,
      });

      expect(result).toEqual(mockUpdatedItem);
      expect(mockRepository.updateItem).toHaveBeenCalledWith('1', 'item1', {
        name: 'Arroz Integral',
        quantity: 5,
        unitPrice: 4.99,
      });
    });
  });

  describe('Validações de entrada', () => {
    it('deve lançar erro se listId estiver vazio', async () => {
      await expect(
        useCase.execute({
          listId: '',
          itemId: 'item1',
          name: 'Arroz',
        })
      ).rejects.toThrow('ID da lista é obrigatório');
      expect(mockRepository.getById).not.toHaveBeenCalled();
    });

    it('deve lançar erro se listId for apenas espaços', async () => {
      await expect(
        useCase.execute({
          listId: '   ',
          itemId: 'item1',
          name: 'Arroz',
        })
      ).rejects.toThrow('ID da lista é obrigatório');
      expect(mockRepository.getById).not.toHaveBeenCalled();
    });

    it('deve lançar erro se itemId estiver vazio', async () => {
      await expect(
        useCase.execute({
          listId: '1',
          itemId: '',
          name: 'Arroz',
        })
      ).rejects.toThrow('ID do item é obrigatório');
      expect(mockRepository.getById).not.toHaveBeenCalled();
    });

    it('deve lançar erro se itemId for apenas espaços', async () => {
      await expect(
        useCase.execute({
          listId: '1',
          itemId: '   ',
          name: 'Arroz',
        })
      ).rejects.toThrow('ID do item é obrigatório');
      expect(mockRepository.getById).not.toHaveBeenCalled();
    });

    it('deve lançar erro se nenhum campo for fornecido', async () => {
      await expect(
        useCase.execute({
          listId: '1',
          itemId: 'item1',
        })
      ).rejects.toThrow('Pelo menos um campo deve ser fornecido para atualização');
      expect(mockRepository.getById).not.toHaveBeenCalled();
    });
  });

  describe('Validações de negócio', () => {
    it('deve lançar erro se nome tiver menos de 2 caracteres', async () => {
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
          itemId: 'item1',
          name: 'A',
        })
      ).rejects.toThrow('Nome do item deve ter entre 2 e 80 caracteres');
      expect(mockRepository.updateItem).not.toHaveBeenCalled();
    });

    it('deve lançar erro se nome tiver mais de 80 caracteres', async () => {
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
          itemId: 'item1',
          name: 'A'.repeat(81),
        })
      ).rejects.toThrow('Nome do item deve ter entre 2 e 80 caracteres');
      expect(mockRepository.updateItem).not.toHaveBeenCalled();
    });

    it('deve lançar erro se quantidade for menor que 1', async () => {
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
          itemId: 'item1',
          quantity: 0,
        })
      ).rejects.toThrow('Quantidade deve ser maior ou igual a 1');
      expect(mockRepository.updateItem).not.toHaveBeenCalled();
    });

    it('deve lançar erro se preço unitário for negativo', async () => {
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
          itemId: 'item1',
          unitPrice: -1,
        })
      ).rejects.toThrow('Preço unitário não pode ser negativo');
      expect(mockRepository.updateItem).not.toHaveBeenCalled();
    });
  });

  describe('Validação de lista e item', () => {
    it('deve lançar erro se lista não for encontrada', async () => {
      mockRepository.getById.mockResolvedValue(null);

      await expect(
        useCase.execute({
          listId: '999',
          itemId: 'item1',
          name: 'Arroz',
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
          name: 'Arroz',
        })
      ).rejects.toThrow('Item não encontrado na lista');
      expect(mockRepository.getById).toHaveBeenCalledWith('1');
      expect(mockRepository.updateItem).not.toHaveBeenCalled();
    });
  });

  describe('Trim de campos', () => {
    it('deve fazer trim do nome antes de atualizar', async () => {
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
        name: 'Arroz Integral',
        quantity: 2,
        isPurchased: false,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      mockRepository.getById.mockResolvedValue(mockList);
      mockRepository.updateItem.mockResolvedValue(mockUpdatedItem);

      await useCase.execute({
        listId: '1',
        itemId: 'item1',
        name: '  Arroz Integral  ',
      });

      expect(mockRepository.updateItem).toHaveBeenCalledWith('1', 'item1', {
        name: 'Arroz Integral',
      });
    });

    it('deve fazer trim dos IDs antes de buscar', async () => {
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
        name: 'Arroz Integral',
        quantity: 2,
        isPurchased: false,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      mockRepository.getById.mockResolvedValue(mockList);
      mockRepository.updateItem.mockResolvedValue(mockUpdatedItem);

      await useCase.execute({
        listId: '  1  ',
        itemId: '  item1  ',
        name: 'Arroz Integral',
      });

      expect(mockRepository.getById).toHaveBeenCalledWith('1');
      expect(mockRepository.updateItem).toHaveBeenCalledWith('1', 'item1', {
        name: 'Arroz Integral',
      });
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
          name: 'Arroz',
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
          name: 'Arroz',
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
          name: 'Arroz',
        })
      ).rejects.toThrow('Erro desconhecido');
    });
  });
});
