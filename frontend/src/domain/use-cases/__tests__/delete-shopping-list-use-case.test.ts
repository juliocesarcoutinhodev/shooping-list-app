// Testes unitários para DeleteShoppingListUseCase
// Cobre: sucesso, validações de ID, erro do repository, códigos HTTP específicos

import { DeleteShoppingListUseCase } from '../delete-shopping-list-use-case';

describe('DeleteShoppingListUseCase', () => {
  let repository: { delete: jest.Mock };
  let useCase: DeleteShoppingListUseCase;

  beforeEach(() => {
    repository = { delete: jest.fn() };
    useCase = new DeleteShoppingListUseCase(repository as any);
  });

  describe('validações', () => {
    it('lança erro se ID for vazio', async () => {
      await expect(useCase.execute('')).rejects.toThrow('ID da lista é obrigatório');
    });

    it('lança erro se ID for apenas espaços', async () => {
      await expect(useCase.execute('   ')).rejects.toThrow('ID da lista é obrigatório');
    });

    it('lança erro se ID não for string', async () => {
      await expect(useCase.execute(null as any)).rejects.toThrow('ID da lista é obrigatório');
      await expect(useCase.execute(undefined as any)).rejects.toThrow('ID da lista é obrigatório');
    });
  });

  describe('operação de deleção', () => {
    it('chama repository.delete com ID válido', async () => {
      repository.delete.mockResolvedValueOnce(undefined);
      await useCase.execute('list-123');
      expect(repository.delete).toHaveBeenCalledWith('list-123');
      expect(repository.delete).toHaveBeenCalledTimes(1);
    });

    it('faz trim do ID antes de passar para o repository', async () => {
      repository.delete.mockResolvedValueOnce(undefined);
      await useCase.execute('  list-456  ');
      expect(repository.delete).toHaveBeenCalledWith('list-456');
    });

    it('sucesso: não retorna valor', async () => {
      repository.delete.mockResolvedValueOnce(undefined);
      const result = await useCase.execute('list-789');
      expect(result).toBeUndefined();
    });
  });

  describe('tratamento de erros do repository', () => {
    it('propaga erro 404 (lista não encontrada)', async () => {
      const error404 = { message: 'Lista não encontrada', status: 404 };
      repository.delete.mockRejectedValueOnce(error404);
      await expect(useCase.execute('list-not-found')).rejects.toMatchObject(error404);
    });

    it('propaga erro 403 (sem permissão)', async () => {
      const error403 = { message: 'Sem permissão para deletar esta lista', status: 403 };
      repository.delete.mockRejectedValueOnce(error403);
      await expect(useCase.execute('list-forbidden')).rejects.toMatchObject(error403);
    });

    it('propaga erro 401 (não autenticado)', async () => {
      const error401 = { message: 'Token expirado', status: 401 };
      repository.delete.mockRejectedValueOnce(error401);
      await expect(useCase.execute('list-unauth')).rejects.toMatchObject(error401);
    });

    it('propaga erro genérico do repository', async () => {
      const errorGeneric = { message: 'Erro interno do servidor', status: 500 };
      repository.delete.mockRejectedValueOnce(errorGeneric);
      await expect(useCase.execute('list-error')).rejects.toMatchObject(errorGeneric);
    });
  });
});
