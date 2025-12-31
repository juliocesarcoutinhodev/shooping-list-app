// Testes unitários para ShoppingListRepositoryImpl
// Mocka o data source remoto e cobre sucesso, erro 401 e erro 500

import { ShoppingListRemoteDataSource } from '../../data-sources/shopping-list-remote-data-source';
import { ShoppingListDto } from '../../models';
import { ShoppingListRepositoryImpl } from '../shopping-list-repository';

jest.mock('expo-constants', () => ({
  default: {
    expoConfig: {
      extra: {
        apiUrl: 'http://localhost:3000',
      },
    },
  },
}));

jest.mock('../../data-sources/shopping-list-remote-data-source');

const mockLists: ShoppingListDto[] = [
  {
    id: '1',
    title: 'Supermercado',
    items: [],
    created_at: '2025-12-29T09:00:00Z',
    updated_at: '2025-12-29T09:30:00Z',
  },
];

describe('ShoppingListRepositoryImpl', () => {
  let remote: jest.Mocked<ShoppingListRemoteDataSource>;
  let repo: ShoppingListRepositoryImpl;

  beforeEach(() => {
    remote = new ShoppingListRemoteDataSource() as jest.Mocked<ShoppingListRemoteDataSource>;
    repo = new ShoppingListRepositoryImpl(remote);
  });

  it('deve retornar listas de compras no formato de domínio', async () => {
    remote.getMyLists.mockResolvedValueOnce(mockLists);
    const result = await repo.getMyLists();
    expect(result[0]).toHaveProperty('id', '1');
    expect(result[0]).toHaveProperty('title', 'Supermercado');
    expect(result[0]).toHaveProperty('createdAt', '2025-12-29T09:00:00Z');
    expect(result[0]).toHaveProperty('updatedAt', '2025-12-29T09:30:00Z');
  });

  it('deve lançar erro normalizado 401', async () => {
    remote.getMyLists.mockRejectedValueOnce({ message: 'Não autorizado', status: 401 });
    await expect(repo.getMyLists()).rejects.toMatchObject({
      message: 'Não autorizado',
      status: 401,
    });
  });

  it('deve lançar erro normalizado 500', async () => {
    remote.getMyLists.mockRejectedValueOnce({ message: 'Erro interno', status: 500 });
    await expect(repo.getMyLists()).rejects.toMatchObject({ message: 'Erro interno', status: 500 });
  });

  describe('getById', () => {
    it('deve retornar lista com itens mapeados quando encontrada', async () => {
      const mockListWithItems: ShoppingListDto = {
        id: '1',
        title: 'Compras do Mercado',
        description: 'Lista semanal',
        items: [
          {
            id: 'item1',
            name: 'Leite',
            quantity: 2,
            unit_price: 4.5,
            is_purchased: false,
            created_at: '2025-12-30T10:00:00Z',
            updated_at: '2025-12-30T10:00:00Z',
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
        created_at: '2025-12-30T09:00:00Z',
        updated_at: '2025-12-30T09:30:00Z',
      };

      remote.getListById.mockResolvedValueOnce(mockListWithItems);

      const result = await repo.getById('1');

      expect(result).not.toBeNull();
      expect(result?.id).toBe('1');
      expect(result?.title).toBe('Compras do Mercado');
      expect(result?.description).toBe('Lista semanal');
      expect(result?.items).toHaveLength(2);
      expect(result?.items[0]).toEqual({
        id: 'item1',
        name: 'Leite',
        quantity: 2,
        unitPrice: 4.5,
        isPurchased: false,
        createdAt: '2025-12-30T10:00:00Z',
        updatedAt: '2025-12-30T10:00:00Z',
      });
      expect(result?.items[1]).toEqual({
        id: 'item2',
        name: 'Pão',
        quantity: 1,
        unitPrice: 6.0,
        isPurchased: true,
        createdAt: '2025-12-30T10:00:00Z',
        updatedAt: '2025-12-30T10:00:00Z',
      });
    });

    it('deve retornar null quando lista não é encontrada (404)', async () => {
      remote.getListById.mockRejectedValueOnce({
        message: 'Lista não encontrada',
        status: 404,
      });

      const result = await repo.getById('999');

      expect(result).toBeNull();
    });

    it('deve propagar erro quando ocorre erro diferente de 404', async () => {
      remote.getListById.mockRejectedValueOnce({
        message: 'Erro interno do servidor',
        status: 500,
      });

      await expect(repo.getById('1')).rejects.toMatchObject({
        message: 'Erro interno do servidor',
        status: 500,
      });
    });

    it('deve propagar erro quando ocorre erro 401 (não autorizado)', async () => {
      remote.getListById.mockRejectedValueOnce({
        message: 'Não autorizado',
        status: 401,
      });

      await expect(repo.getById('1')).rejects.toMatchObject({
        message: 'Não autorizado',
        status: 401,
      });
    });

    it('deve mapear lista com items vazio quando API não retorna items', async () => {
      const mockListNoItems: ShoppingListDto = {
        id: '2',
        title: 'Lista Vazia',
        createdAt: '2025-12-30T09:00:00Z',
        updatedAt: '2025-12-30T09:30:00Z',
      };

      remote.getListById.mockResolvedValueOnce(mockListNoItems);

      const result = await repo.getById('2');

      expect(result).not.toBeNull();
      expect(result?.items).toEqual([]);
    });
  });
});
