// Testes unitários para DeleteShoppingItemUseCase
// Cobre: sucesso, validações de ID, erro do repository, códigos HTTP específicos

import { DeleteShoppingItemUseCase } from '../delete-shopping-item-use-case';

describe('DeleteShoppingItemUseCase', () => {
  let repository: { deleteItem: jest.Mock };
  let useCase: DeleteShoppingItemUseCase;

  beforeEach(() => {
    repository = { deleteItem: jest.fn() };
    useCase = new DeleteShoppingItemUseCase(repository as any);
  });

  describe('validações', () => {
    it('lança erro se listId for vazio', async () => {
      await expect(useCase.execute('', 'item-1')).rejects.toThrow('ID da lista é obrigatório');
    });

    it('lança erro se listId for apenas espaços', async () => {
      await expect(useCase.execute('   ', 'item-1')).rejects.toThrow('ID da lista é obrigatório');
    });

    it('lança erro se listId não for string', async () => {
      await expect(useCase.execute(null as any, 'item-1')).rejects.toThrow(
        'ID da lista é obrigatório'
      );
      await expect(useCase.execute(undefined as any, 'item-1')).rejects.toThrow(
        'ID da lista é obrigatório'
      );
    });

    it('lança erro se itemId for vazio', async () => {
      await expect(useCase.execute('list-1', '')).rejects.toThrow('ID do item é obrigatório');
    });

    it('lança erro se itemId for apenas espaços', async () => {
      await expect(useCase.execute('list-1', '   ')).rejects.toThrow('ID do item é obrigatório');
    });

    it('lança erro se itemId não for string', async () => {
      await expect(useCase.execute('list-1', null as any)).rejects.toThrow(
        'ID do item é obrigatório'
      );
      await expect(useCase.execute('list-1', undefined as any)).rejects.toThrow(
        'ID do item é obrigatório'
      );
    });
  });

  describe('operação de deleção', () => {
    it('chama repository.deleteItem com IDs válidos', async () => {
      repository.deleteItem.mockResolvedValueOnce(undefined);
      await useCase.execute('list-123', 'item-456');
      expect(repository.deleteItem).toHaveBeenCalledWith('list-123', 'item-456');
      expect(repository.deleteItem).toHaveBeenCalledTimes(1);
    });

    it('faz trim dos IDs antes de passar para o repository', async () => {
      repository.deleteItem.mockResolvedValueOnce(undefined);
      await useCase.execute('  list-456  ', '  item-789  ');
      expect(repository.deleteItem).toHaveBeenCalledWith('list-456', 'item-789');
    });

    it('sucesso: não retorna valor', async () => {
      repository.deleteItem.mockResolvedValueOnce(undefined);
      const result = await useCase.execute('list-789', 'item-123');
      expect(result).toBeUndefined();
    });
  });

  describe('tratamento de erros do repository', () => {
    it('propaga erro 404 do repository', async () => {
      const error = { message: 'Item não encontrado', status: 404 };
      repository.deleteItem.mockRejectedValueOnce(error);
      await expect(useCase.execute('list-1', 'item-1')).rejects.toMatchObject(error);
    });

    it('propaga erro 403 do repository', async () => {
      const error = { message: 'Sem permissão', status: 403 };
      repository.deleteItem.mockRejectedValueOnce(error);
      await expect(useCase.execute('list-1', 'item-1')).rejects.toMatchObject(error);
    });

    it('propaga erro genérico do repository', async () => {
      const error = new Error('Erro de rede');
      repository.deleteItem.mockRejectedValueOnce(error);
      await expect(useCase.execute('list-1', 'item-1')).rejects.toThrow('Erro de rede');
    });
  });
});
