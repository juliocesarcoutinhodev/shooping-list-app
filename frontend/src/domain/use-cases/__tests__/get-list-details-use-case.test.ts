/**
 * Testes unitários para GetListDetailsUseCase
 *
 * Cobre:
 * - Busca bem-sucedida de lista com itens ordenados
 * - Ordenação: não comprados primeiro, depois comprados, por updatedAt desc
 * - Lista não encontrada (404 -> null)
 * - Validação de ID obrigatório
 * - Propagação de erros do repository
 */

import { ShoppingList } from '../../entities';
import { ShoppingListRepository } from '../../repositories';
import { GetListDetailsUseCase } from '../get-list-details-use-case';

describe('GetListDetailsUseCase', () => {
  let mockRepository: jest.Mocked<ShoppingListRepository>;
  let useCase: GetListDetailsUseCase;

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
    useCase = new GetListDetailsUseCase(mockRepository);
  });

  describe('Ordenação de itens', () => {
    it('deve ordenar itens não comprados antes dos comprados', async () => {
      const mockList: ShoppingList = {
        id: '1',
        title: 'Compras',
        items: [
          {
            id: 'item1',
            name: 'Pão (comprado)',
            quantity: 1,
            isPurchased: true,
            createdAt: '2025-12-30T10:00:00Z',
            updatedAt: '2025-12-30T12:00:00Z',
          },
          {
            id: 'item2',
            name: 'Leite (não comprado)',
            quantity: 2,
            isPurchased: false,
            createdAt: '2025-12-30T10:00:00Z',
            updatedAt: '2025-12-30T11:00:00Z',
          },
        ],
        createdAt: '2025-12-30T09:00:00Z',
        updatedAt: '2025-12-30T09:30:00Z',
      };

      mockRepository.getById.mockResolvedValueOnce(mockList);

      const result = await useCase.execute('1');

      expect(result?.items[0].isPurchased).toBe(false);
      expect(result?.items[0].name).toBe('Leite (não comprado)');
      expect(result?.items[1].isPurchased).toBe(true);
      expect(result?.items[1].name).toBe('Pão (comprado)');
    });

    it('deve ordenar itens não comprados por updatedAt desc (mais recente primeiro)', async () => {
      const mockList: ShoppingList = {
        id: '1',
        title: 'Compras',
        items: [
          {
            id: 'item1',
            name: 'Item antigo',
            quantity: 1,
            isPurchased: false,
            createdAt: '2025-12-30T08:00:00Z',
            updatedAt: '2025-12-30T09:00:00Z',
          },
          {
            id: 'item2',
            name: 'Item recente',
            quantity: 1,
            isPurchased: false,
            createdAt: '2025-12-30T10:00:00Z',
            updatedAt: '2025-12-30T12:00:00Z',
          },
          {
            id: 'item3',
            name: 'Item médio',
            quantity: 1,
            isPurchased: false,
            createdAt: '2025-12-30T09:00:00Z',
            updatedAt: '2025-12-30T10:00:00Z',
          },
        ],
        createdAt: '2025-12-30T08:00:00Z',
        updatedAt: '2025-12-30T12:00:00Z',
      };

      mockRepository.getById.mockResolvedValueOnce(mockList);

      const result = await useCase.execute('1');

      expect(result?.items[0].name).toBe('Item recente');
      expect(result?.items[1].name).toBe('Item médio');
      expect(result?.items[2].name).toBe('Item antigo');
    });

    it('deve ordenar itens comprados por updatedAt desc dentro do grupo', async () => {
      const mockList: ShoppingList = {
        id: '1',
        title: 'Compras',
        items: [
          {
            id: 'item1',
            name: 'Comprado antigo',
            quantity: 1,
            isPurchased: true,
            createdAt: '2025-12-30T08:00:00Z',
            updatedAt: '2025-12-30T09:00:00Z',
          },
          {
            id: 'item2',
            name: 'Comprado recente',
            quantity: 1,
            isPurchased: true,
            createdAt: '2025-12-30T10:00:00Z',
            updatedAt: '2025-12-30T12:00:00Z',
          },
        ],
        createdAt: '2025-12-30T08:00:00Z',
        updatedAt: '2025-12-30T12:00:00Z',
      };

      mockRepository.getById.mockResolvedValueOnce(mockList);

      const result = await useCase.execute('1');

      expect(result?.items[0].name).toBe('Comprado recente');
      expect(result?.items[1].name).toBe('Comprado antigo');
    });

    it('deve ordenar corretamente misturando comprados e não comprados', async () => {
      const mockList: ShoppingList = {
        id: '1',
        title: 'Compras Completa',
        items: [
          {
            id: 'item1',
            name: 'Comprado 1',
            quantity: 1,
            isPurchased: true,
            createdAt: '2025-12-30T08:00:00Z',
            updatedAt: '2025-12-30T13:00:00Z', // mais recente dos comprados
          },
          {
            id: 'item2',
            name: 'Não comprado 1',
            quantity: 1,
            isPurchased: false,
            createdAt: '2025-12-30T08:00:00Z',
            updatedAt: '2025-12-30T10:00:00Z',
          },
          {
            id: 'item3',
            name: 'Comprado 2',
            quantity: 1,
            isPurchased: true,
            createdAt: '2025-12-30T08:00:00Z',
            updatedAt: '2025-12-30T09:00:00Z', // mais antigo dos comprados
          },
          {
            id: 'item4',
            name: 'Não comprado 2',
            quantity: 1,
            isPurchased: false,
            createdAt: '2025-12-30T08:00:00Z',
            updatedAt: '2025-12-30T14:00:00Z', // mais recente dos não comprados
          },
        ],
        createdAt: '2025-12-30T08:00:00Z',
        updatedAt: '2025-12-30T14:00:00Z',
      };

      mockRepository.getById.mockResolvedValueOnce(mockList);

      const result = await useCase.execute('1');

      // Não comprados primeiro (por updatedAt desc)
      expect(result?.items[0].name).toBe('Não comprado 2');
      expect(result?.items[0].isPurchased).toBe(false);
      expect(result?.items[1].name).toBe('Não comprado 1');
      expect(result?.items[1].isPurchased).toBe(false);
      // Depois comprados (por updatedAt desc)
      expect(result?.items[2].name).toBe('Comprado 1');
      expect(result?.items[2].isPurchased).toBe(true);
      expect(result?.items[3].name).toBe('Comprado 2');
      expect(result?.items[3].isPurchased).toBe(true);
    });
  });

  it('deve retornar lista com itens ordenados quando encontrada', async () => {
    const mockList: ShoppingList = {
      id: '1',
      title: 'Compras do Mercado',
      description: 'Lista semanal',
      items: [
        {
          id: 'item1',
          name: 'Leite',
          quantity: 2,
          unitPrice: 4.5,
          isPurchased: false,
          createdAt: '2025-12-30T10:00:00Z',
          updatedAt: '2025-12-30T10:00:00Z',
        },
        {
          id: 'item2',
          name: 'Pão',
          quantity: 1,
          unitPrice: 6.0,
          isPurchased: true,
          createdAt: '2025-12-30T10:00:00Z',
          updatedAt: '2025-12-30T10:00:00Z',
        },
      ],
      createdAt: '2025-12-30T09:00:00Z',
      updatedAt: '2025-12-30T09:30:00Z',
    };

    mockRepository.getById.mockResolvedValueOnce(mockList);

    const result = await useCase.execute('1');

    expect(result).toBeDefined();
    expect(result?.items).toHaveLength(2);
    // Leite (não comprado) deve vir antes de Pão (comprado)
    expect(result?.items[0].name).toBe('Leite');
    expect(result?.items[1].name).toBe('Pão');
    expect(mockRepository.getById).toHaveBeenCalledWith('1');
  });

  it('deve retornar lista vazia quando não há itens', async () => {
    const mockList: ShoppingList = {
      id: '1',
      title: 'Lista Vazia',
      items: [],
      createdAt: '2025-12-30T09:00:00Z',
      updatedAt: '2025-12-30T09:30:00Z',
    };

    mockRepository.getById.mockResolvedValueOnce(mockList);

    const result = await useCase.execute('1');

    expect(result).toBeDefined();
    expect(result?.items).toEqual([]);
    expect(mockRepository.getById).toHaveBeenCalledWith('1');
  });

  it('deve retornar null quando lista não é encontrada', async () => {
    mockRepository.getById.mockResolvedValueOnce(null);

    const result = await useCase.execute('999');

    expect(result).toBeNull();
    expect(mockRepository.getById).toHaveBeenCalledWith('999');
  });

  it('deve fazer trim do listId antes de buscar', async () => {
    mockRepository.getById.mockResolvedValueOnce(null);

    await useCase.execute('  123  ');

    expect(mockRepository.getById).toHaveBeenCalledWith('123');
  });

  it('deve lançar erro se listId estiver vazio', async () => {
    await expect(useCase.execute('')).rejects.toThrow('ID da lista é obrigatório');
    expect(mockRepository.getById).not.toHaveBeenCalled();
  });

  it('deve lançar erro se listId for apenas espaços', async () => {
    await expect(useCase.execute('   ')).rejects.toThrow('ID da lista é obrigatório');
    expect(mockRepository.getById).not.toHaveBeenCalled();
  });

  it('deve propagar erro do repository (401)', async () => {
    mockRepository.getById.mockRejectedValueOnce({
      message: 'Não autorizado',
      status: 401,
    });

    await expect(useCase.execute('1')).rejects.toMatchObject({
      message: 'Não autorizado',
      status: 401,
    });
  });

  it('deve propagar erro do repository (500)', async () => {
    mockRepository.getById.mockRejectedValueOnce({
      message: 'Erro interno do servidor',
      status: 500,
    });

    await expect(useCase.execute('1')).rejects.toMatchObject({
      message: 'Erro interno do servidor',
      status: 500,
    });
  });

  it('deve retornar lista com items vazio quando não há itens', async () => {
    const mockListNoItems: ShoppingList = {
      id: '2',
      title: 'Lista Vazia',
      items: [],
      createdAt: '2025-12-30T09:00:00Z',
      updatedAt: '2025-12-30T09:30:00Z',
    };

    mockRepository.getById.mockResolvedValueOnce(mockListNoItems);

    const result = await useCase.execute('2');

    expect(result).toEqual(mockListNoItems);
    expect(result?.items).toEqual([]);
  });
});
