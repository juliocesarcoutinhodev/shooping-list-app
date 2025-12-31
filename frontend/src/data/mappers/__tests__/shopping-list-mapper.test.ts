// Testes unitários para o mapper ShoppingListDto -> ShoppingList
// Sigo o padrão do projeto, cobrindo casos de entrada válida e campos obrigatórios.

import { ShoppingListDto } from '../../models';
import { mapShoppingListDtoToDomain } from '../shopping-list-mapper';

describe('mapShoppingListDtoToDomain', () => {
  it('deve mapear corretamente um ShoppingListDto com snake_case', () => {
    const dto: ShoppingListDto = {
      id: '1',
      title: 'Supermercado',
      description: 'Lista mensal',
      items: [
        {
          id: 'item1',
          name: 'Arroz',
          quantity: 2,
          is_completed: false,
          created_at: '2025-12-29T10:00:00Z',
          updated_at: '2025-12-29T10:00:00Z',
        },
      ],
      created_at: '2025-12-29T09:00:00Z',
      updated_at: '2025-12-29T09:30:00Z',
    };

    const domain = mapShoppingListDtoToDomain(dto);
    expect(domain).toEqual({
      id: '1',
      title: 'Supermercado',
      description: 'Lista mensal',
      items: [
        {
          id: 'item1',
          name: 'Arroz',
          quantity: 2,
          isPurchased: false,
          createdAt: '2025-12-29T10:00:00Z',
          updatedAt: '2025-12-29T10:00:00Z',
        },
      ],
      createdAt: '2025-12-29T09:00:00Z',
      updatedAt: '2025-12-29T09:30:00Z',
    });
  });

  it('deve mapear corretamente um ShoppingListDto com camelCase (formato real da API)', () => {
    const dto: ShoppingListDto = {
      id: 5,
      title: 'Lista da Miriã',
      itemsCount: 0,
      pendingItemsCount: 0,
      createdAt: '2025-12-29T14:40:21.299Z',
      updatedAt: '2025-12-29T14:40:21.299Z',
    };

    const domain = mapShoppingListDtoToDomain(dto);
    expect(domain).toEqual({
      id: '5',
      title: 'Lista da Miriã',
      description: undefined,
      items: [],
      itemsCount: 0,
      pendingItemsCount: 0,
      createdAt: '2025-12-29T14:40:21.299Z',
      updatedAt: '2025-12-29T14:40:21.299Z',
    });
  });

  it('deve mapear corretamente quando items é null ou undefined', () => {
    const dto: ShoppingListDto = {
      id: '2',
      title: 'Lista vazia',
      items: undefined,
      created_at: '2025-12-29T09:00:00Z',
      updated_at: '2025-12-29T09:30:00Z',
    };

    const domain = mapShoppingListDtoToDomain(dto);
    expect(domain.items).toEqual([]);
  });

  it('deve lançar erro se campos obrigatórios estiverem ausentes', () => {
    // Aqui simulo um DTO incompleto para garantir robustez
    const dtoInvalido = {
      id: '2',
      title: 'Padaria',
      items: [],
      // created_at/createdAt e updated_at/updatedAt ausentes
    } as any;
    expect(() => mapShoppingListDtoToDomain(dtoInvalido)).toThrow(
      'Campos obrigatórios ausentes em ShoppingListDto'
    );
  });
});
