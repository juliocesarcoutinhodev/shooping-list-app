// Testes unitários para CreateListUseCase

import { ShoppingList } from '../../entities';
import { ShoppingListRepository } from '../../repositories';
import { CreateListUseCase } from '../create-list-use-case';

describe('CreateListUseCase', () => {
  let mockRepository: jest.Mocked<ShoppingListRepository>;
  let useCase: CreateListUseCase;

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
    useCase = new CreateListUseCase(mockRepository);
  });

  it('deve criar uma lista com título válido', async () => {
    const mockList: ShoppingList = {
      id: '1',
      title: 'Minha Lista',
      items: [],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    mockRepository.create.mockResolvedValue(mockList);

    const result = await useCase.execute({ title: 'Minha Lista' });

    expect(result).toEqual(mockList);
    expect(mockRepository.create).toHaveBeenCalledWith({
      title: 'Minha Lista',
      description: undefined,
      items: [],
    });
  });

  it('deve criar uma lista com título e descrição', async () => {
    const mockList: ShoppingList = {
      id: '1',
      title: 'Compras do mês',
      description: 'Lista para o supermercado',
      items: [],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    mockRepository.create.mockResolvedValue(mockList);

    const result = await useCase.execute({
      title: 'Compras do mês',
      description: 'Lista para o supermercado',
    });

    expect(result).toEqual(mockList);
    expect(mockRepository.create).toHaveBeenCalledWith({
      title: 'Compras do mês',
      description: 'Lista para o supermercado',
      items: [],
    });
  });

  it('deve lançar erro se título estiver vazio', async () => {
    await expect(useCase.execute({ title: '' })).rejects.toThrow('Título é obrigatório');
  });

  it('deve lançar erro se título tiver menos de 3 caracteres', async () => {
    await expect(useCase.execute({ title: 'AB' })).rejects.toThrow(
      'Título deve ter no mínimo 3 caracteres'
    );
  });

  it('deve lançar erro se título tiver mais de 100 caracteres', async () => {
    const longTitle = 'A'.repeat(101);
    await expect(useCase.execute({ title: longTitle })).rejects.toThrow(
      'Título deve ter no máximo 100 caracteres'
    );
  });

  it('deve lançar erro se descrição tiver mais de 255 caracteres', async () => {
    const longDescription = 'A'.repeat(256);
    await expect(
      useCase.execute({ title: 'Título válido', description: longDescription })
    ).rejects.toThrow('Descrição deve ter no máximo 255 caracteres');
  });

  it('deve fazer trim do título e descrição', async () => {
    const mockList: ShoppingList = {
      id: '1',
      title: 'Título',
      description: 'Descrição',
      items: [],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    mockRepository.create.mockResolvedValue(mockList);

    await useCase.execute({
      title: '  Título  ',
      description: '  Descrição  ',
    });

    expect(mockRepository.create).toHaveBeenCalledWith({
      title: 'Título',
      description: 'Descrição',
      items: [],
    });
  });

  it('deve propagar erro do repositório', async () => {
    const error = new Error('Erro de rede');
    mockRepository.create.mockRejectedValue(error);

    await expect(useCase.execute({ title: 'Título válido' })).rejects.toThrow('Erro de rede');
  });
});
