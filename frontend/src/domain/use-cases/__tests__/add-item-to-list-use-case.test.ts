/**
 * Testes unitários para AddItemToListUseCase
 *
 * Cobre:
 * - Criação bem-sucedida de item
 * - Validações de negócio (nome, quantidade, preço)
 * - Trim de campos (nome, unit)
 * - Lista não encontrada
 * - Propagação de erros do repository
 */

import { ShoppingItem, ShoppingList } from '../../entities';
import { ShoppingListRepository } from '../../repositories';
import { AddItemToListUseCase } from '../add-item-to-list-use-case';

describe('AddItemToListUseCase', () => {
  let mockRepository: jest.Mocked<ShoppingListRepository>;
  let useCase: AddItemToListUseCase;

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
    useCase = new AddItemToListUseCase(mockRepository);
  });

  describe('Criação bem-sucedida', () => {
    it('deve criar item com dados válidos', async () => {
      const mockList: ShoppingList = {
        id: '1',
        title: 'Minha Lista',
        items: [],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      const mockItem: ShoppingItem = {
        id: 'item1',
        name: 'Arroz',
        quantity: 2,
        isPurchased: false,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      mockRepository.getById.mockResolvedValue(mockList);
      mockRepository.addItem.mockResolvedValue(mockItem);

      const result = await useCase.execute({
        listId: '1',
        name: 'Arroz',
        quantity: 2,
      });

      expect(result).toEqual(mockItem);
      expect(mockRepository.getById).toHaveBeenCalledWith('1');
      expect(mockRepository.addItem).toHaveBeenCalledWith('1', {
        name: 'Arroz',
        quantity: 2,
        unit: undefined,
        unitPrice: undefined,
      });
    });

    it('deve criar item com todos os campos (nome, quantidade, unidade, preço)', async () => {
      const mockList: ShoppingList = {
        id: '1',
        title: 'Minha Lista',
        items: [],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      const mockItem: ShoppingItem = {
        id: 'item1',
        name: 'Leite',
        quantity: 3,
        unitPrice: 4.99,
        isPurchased: false,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      mockRepository.getById.mockResolvedValue(mockList);
      mockRepository.addItem.mockResolvedValue(mockItem);

      const result = await useCase.execute({
        listId: '1',
        name: 'Leite',
        quantity: 3,
        unit: 'L',
        unitPrice: 4.99,
      });

      expect(result).toEqual(mockItem);
      expect(mockRepository.addItem).toHaveBeenCalledWith('1', {
        name: 'Leite',
        quantity: 3,
        unit: 'L',
        unitPrice: 4.99,
      });
    });

    it('deve criar item com quantidade = 1', async () => {
      const mockList: ShoppingList = {
        id: '1',
        title: 'Minha Lista',
        items: [],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      const mockItem: ShoppingItem = {
        id: 'item1',
        name: 'Pão',
        quantity: 1,
        isPurchased: false,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      mockRepository.getById.mockResolvedValue(mockList);
      mockRepository.addItem.mockResolvedValue(mockItem);

      const result = await useCase.execute({
        listId: '1',
        name: 'Pão',
        quantity: 1,
      });

      expect(result).toEqual(mockItem);
    });

    it('deve criar item com preço unitário = 0', async () => {
      const mockList: ShoppingList = {
        id: '1',
        title: 'Minha Lista',
        items: [],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      const mockItem: ShoppingItem = {
        id: 'item1',
        name: 'Item Grátis',
        quantity: 1,
        unitPrice: 0,
        isPurchased: false,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      mockRepository.getById.mockResolvedValue(mockList);
      mockRepository.addItem.mockResolvedValue(mockItem);

      const result = await useCase.execute({
        listId: '1',
        name: 'Item Grátis',
        quantity: 1,
        unitPrice: 0,
      });

      expect(result).toEqual(mockItem);
    });
  });

  describe('Validações de nome', () => {
    it('deve lançar erro se nome estiver vazio', async () => {
      await expect(
        useCase.execute({
          listId: '1',
          name: '',
          quantity: 1,
        })
      ).rejects.toThrow('Nome do item é obrigatório');
      expect(mockRepository.getById).not.toHaveBeenCalled();
    });

    it('deve lançar erro se nome for apenas espaços', async () => {
      await expect(
        useCase.execute({
          listId: '1',
          name: '   ',
          quantity: 1,
        })
      ).rejects.toThrow('Nome do item é obrigatório');
      expect(mockRepository.getById).not.toHaveBeenCalled();
    });

    it('deve lançar erro se nome tiver menos de 2 caracteres', async () => {
      await expect(
        useCase.execute({
          listId: '1',
          name: 'A',
          quantity: 1,
        })
      ).rejects.toThrow('Nome do item deve ter entre 2 e 80 caracteres');
      expect(mockRepository.getById).not.toHaveBeenCalled();
    });

    it('deve lançar erro se nome tiver mais de 80 caracteres', async () => {
      const longName = 'A'.repeat(81);
      await expect(
        useCase.execute({
          listId: '1',
          name: longName,
          quantity: 1,
        })
      ).rejects.toThrow('Nome do item deve ter entre 2 e 80 caracteres');
      expect(mockRepository.getById).not.toHaveBeenCalled();
    });

    it('deve aceitar nome com exatamente 2 caracteres', async () => {
      const mockList: ShoppingList = {
        id: '1',
        title: 'Minha Lista',
        items: [],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      const mockItem: ShoppingItem = {
        id: 'item1',
        name: 'AB',
        quantity: 1,
        isPurchased: false,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      mockRepository.getById.mockResolvedValue(mockList);
      mockRepository.addItem.mockResolvedValue(mockItem);

      const result = await useCase.execute({
        listId: '1',
        name: 'AB',
        quantity: 1,
      });

      expect(result).toEqual(mockItem);
    });

    it('deve aceitar nome com exatamente 80 caracteres', async () => {
      const mockList: ShoppingList = {
        id: '1',
        title: 'Minha Lista',
        items: [],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      const name80 = 'A'.repeat(80);
      const mockItem: ShoppingItem = {
        id: 'item1',
        name: name80,
        quantity: 1,
        isPurchased: false,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      mockRepository.getById.mockResolvedValue(mockList);
      mockRepository.addItem.mockResolvedValue(mockItem);

      const result = await useCase.execute({
        listId: '1',
        name: name80,
        quantity: 1,
      });

      expect(result).toEqual(mockItem);
    });
  });

  describe('Validações de quantidade', () => {
    it('deve lançar erro se quantidade for menor que 1', async () => {
      await expect(
        useCase.execute({
          listId: '1',
          name: 'Arroz',
          quantity: 0,
        })
      ).rejects.toThrow('Quantidade deve ser maior ou igual a 1');
      expect(mockRepository.getById).not.toHaveBeenCalled();
    });

    it('deve lançar erro se quantidade for negativa', async () => {
      await expect(
        useCase.execute({
          listId: '1',
          name: 'Arroz',
          quantity: -1,
        })
      ).rejects.toThrow('Quantidade deve ser maior ou igual a 1');
      expect(mockRepository.getById).not.toHaveBeenCalled();
    });

    it('deve lançar erro se quantidade for undefined', async () => {
      await expect(
        useCase.execute({
          listId: '1',
          name: 'Arroz',
          quantity: undefined as any,
        })
      ).rejects.toThrow('Quantidade deve ser maior ou igual a 1');
      expect(mockRepository.getById).not.toHaveBeenCalled();
    });
  });

  describe('Validações de preço unitário', () => {
    it('deve lançar erro se preço unitário for negativo', async () => {
      await expect(
        useCase.execute({
          listId: '1',
          name: 'Arroz',
          quantity: 1,
          unitPrice: -1,
        })
      ).rejects.toThrow('Preço unitário não pode ser negativo');
      expect(mockRepository.getById).not.toHaveBeenCalled();
    });

    it('deve aceitar preço unitário como undefined', async () => {
      const mockList: ShoppingList = {
        id: '1',
        title: 'Minha Lista',
        items: [],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      const mockItem: ShoppingItem = {
        id: 'item1',
        name: 'Arroz',
        quantity: 1,
        isPurchased: false,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      mockRepository.getById.mockResolvedValue(mockList);
      mockRepository.addItem.mockResolvedValue(mockItem);

      const result = await useCase.execute({
        listId: '1',
        name: 'Arroz',
        quantity: 1,
        unitPrice: undefined,
      });

      expect(result).toEqual(mockItem);
    });

    it('deve aceitar preço unitário como null (converte para undefined)', async () => {
      const mockList: ShoppingList = {
        id: '1',
        title: 'Minha Lista',
        items: [],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      const mockItem: ShoppingItem = {
        id: 'item1',
        name: 'Arroz',
        quantity: 1,
        isPurchased: false,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      mockRepository.getById.mockResolvedValue(mockList);
      mockRepository.addItem.mockResolvedValue(mockItem);

      const result = await useCase.execute({
        listId: '1',
        name: 'Arroz',
        quantity: 1,
        unitPrice: null as any,
      });

      expect(result).toEqual(mockItem);
      expect(mockRepository.addItem).toHaveBeenCalledWith('1', {
        name: 'Arroz',
        quantity: 1,
        unit: undefined,
        unitPrice: undefined,
      });
    });
  });

  describe('Validação de lista', () => {
    it('deve lançar erro se lista não for encontrada', async () => {
      mockRepository.getById.mockResolvedValue(null);

      await expect(
        useCase.execute({
          listId: '999',
          name: 'Arroz',
          quantity: 1,
        })
      ).rejects.toThrow('Lista não encontrada');
      expect(mockRepository.getById).toHaveBeenCalledWith('999');
      expect(mockRepository.addItem).not.toHaveBeenCalled();
    });
  });

  describe('Trim de campos', () => {
    it('deve fazer trim do nome antes de enviar ao repository', async () => {
      const mockList: ShoppingList = {
        id: '1',
        title: 'Minha Lista',
        items: [],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      const mockItem: ShoppingItem = {
        id: 'item1',
        name: 'Arroz',
        quantity: 1,
        isPurchased: false,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      mockRepository.getById.mockResolvedValue(mockList);
      mockRepository.addItem.mockResolvedValue(mockItem);

      await useCase.execute({
        listId: '1',
        name: '  Arroz  ',
        quantity: 1,
      });

      expect(mockRepository.addItem).toHaveBeenCalledWith('1', {
        name: 'Arroz', // Trim aplicado
        quantity: 1,
        unit: undefined,
        unitPrice: undefined,
      });
    });

    it('deve fazer trim da unidade antes de enviar ao repository', async () => {
      const mockList: ShoppingList = {
        id: '1',
        title: 'Minha Lista',
        items: [],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      const mockItem: ShoppingItem = {
        id: 'item1',
        name: 'Leite',
        quantity: 1,
        isPurchased: false,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      mockRepository.getById.mockResolvedValue(mockList);
      mockRepository.addItem.mockResolvedValue(mockItem);

      await useCase.execute({
        listId: '1',
        name: 'Leite',
        quantity: 1,
        unit: '  L  ',
      });

      expect(mockRepository.addItem).toHaveBeenCalledWith('1', {
        name: 'Leite',
        quantity: 1,
        unit: 'L', // Trim aplicado
        unitPrice: undefined,
      });
    });

    it('deve converter unidade vazia (após trim) para undefined', async () => {
      const mockList: ShoppingList = {
        id: '1',
        title: 'Minha Lista',
        items: [],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      const mockItem: ShoppingItem = {
        id: 'item1',
        name: 'Arroz',
        quantity: 1,
        isPurchased: false,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      mockRepository.getById.mockResolvedValue(mockList);
      mockRepository.addItem.mockResolvedValue(mockItem);

      await useCase.execute({
        listId: '1',
        name: 'Arroz',
        quantity: 1,
        unit: '   ',
      });

      expect(mockRepository.addItem).toHaveBeenCalledWith('1', {
        name: 'Arroz',
        quantity: 1,
        unit: undefined, // Convertido para undefined após trim
        unitPrice: undefined,
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
          name: 'Arroz',
          quantity: 1,
        })
      ).rejects.toMatchObject(error);
      expect(mockRepository.addItem).not.toHaveBeenCalled();
    });

    it('deve propagar erro do repository.addItem', async () => {
      const mockList: ShoppingList = {
        id: '1',
        title: 'Minha Lista',
        items: [],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      const error = { message: 'Item duplicado', status: 400 };
      mockRepository.getById.mockResolvedValue(mockList);
      mockRepository.addItem.mockRejectedValue(error);

      await expect(
        useCase.execute({
          listId: '1',
          name: 'Arroz',
          quantity: 1,
        })
      ).rejects.toMatchObject(error);
    });

    it('deve propagar erro genérico do repository', async () => {
      const mockList: ShoppingList = {
        id: '1',
        title: 'Minha Lista',
        items: [],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      const error = new Error('Erro desconhecido');
      mockRepository.getById.mockResolvedValue(mockList);
      mockRepository.addItem.mockRejectedValue(error);

      await expect(
        useCase.execute({
          listId: '1',
          name: 'Arroz',
          quantity: 1,
        })
      ).rejects.toThrow('Erro desconhecido');
    });
  });
});
