// Testes unitários para GetMyListsUseCase
// Cobre: ordenação, retorno vazio, erro do repository

import { ShoppingList } from '../../entities';
import { GetMyListsUseCase } from '../get-my-lists-use-case';

const makeList = (id: string, updatedAt: string): ShoppingList => ({
  id,
  title: `Lista ${id}`,
  items: [],
  createdAt: updatedAt,
  updatedAt,
});

describe('GetMyListsUseCase', () => {
  let repository: { getMyLists: jest.Mock };
  let useCase: GetMyListsUseCase;

  beforeEach(() => {
    repository = { getMyLists: jest.fn() };
    useCase = new GetMyListsUseCase(repository as any);
  });

  it('retorna listas ordenadas por updatedAt desc', async () => {
    const lists = [
      makeList('1', '2025-12-01T10:00:00Z'),
      makeList('2', '2025-12-03T09:00:00Z'),
      makeList('3', '2025-11-30T08:00:00Z'),
    ];
    repository.getMyLists.mockResolvedValueOnce(lists);
    const result = await useCase.execute();
    expect(result.map(l => l.id)).toEqual(['2', '1', '3']);
  });

  it('retorna array vazio se não houver listas', async () => {
    repository.getMyLists.mockResolvedValueOnce([]);
    const result = await useCase.execute();
    expect(result).toEqual([]);
  });

  it('propaga erro do repository', async () => {
    repository.getMyLists.mockRejectedValueOnce({ message: 'Erro', status: 500 });
    await expect(useCase.execute()).rejects.toMatchObject({ message: 'Erro', status: 500 });
  });
});
