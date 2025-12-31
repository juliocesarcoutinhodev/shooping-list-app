/**
 * Testes unitários para o mapper ShoppingItemDto -> ShoppingItem
 *
 * Cobre casos:
 * - Mapeamento válido com todos os campos
 * - Mapeamento com campos opcionais
 * - Suporte a snake_case e camelCase
 * - Validação de campos obrigatórios
 * - Validação de tipos
 * - Mensagens de erro claras
 */

import { ShoppingItemDto } from '../../models';
import { mapShoppingItemDtoToDomain } from '../shopping-item-mapper';

describe('mapShoppingItemDtoToDomain', () => {
  describe('Mapeamento válido', () => {
    it('deve mapear corretamente um ShoppingItemDto completo com snake_case', () => {
      const dto: ShoppingItemDto = {
        id: '1',
        name: 'Leite Integral',
        quantity: 2,
        unit_price: 4.5,
        is_purchased: false,
        created_at: '2025-12-30T10:00:00Z',
        updated_at: '2025-12-30T10:00:00Z',
      };

      const domain = mapShoppingItemDtoToDomain(dto);

      expect(domain).toEqual({
        id: '1',
        name: 'Leite Integral',
        quantity: 2,
        unitPrice: 4.5,
        isPurchased: false,
        createdAt: '2025-12-30T10:00:00Z',
        updatedAt: '2025-12-30T10:00:00Z',
      });
    });

    it('deve mapear corretamente um ShoppingItemDto com camelCase', () => {
      const dto: ShoppingItemDto = {
        id: 42,
        name: 'Pão Francês',
        quantity: 10,
        unitPrice: 0.5,
        isPurchased: true,
        createdAt: '2025-12-30T09:00:00Z',
        updatedAt: '2025-12-30T11:00:00Z',
      };

      const domain = mapShoppingItemDtoToDomain(dto);

      expect(domain).toEqual({
        id: '42',
        name: 'Pão Francês',
        quantity: 10,
        unitPrice: 0.5,
        isPurchased: true,
        createdAt: '2025-12-30T09:00:00Z',
        updatedAt: '2025-12-30T11:00:00Z',
      });
    });

    it('deve mapear corretamente quando unitPrice é undefined', () => {
      const dto: ShoppingItemDto = {
        id: '3',
        name: 'Banana',
        quantity: 6,
        is_purchased: false,
        created_at: '2025-12-30T10:00:00Z',
        updated_at: '2025-12-30T10:00:00Z',
      };

      const domain = mapShoppingItemDtoToDomain(dto);

      expect(domain).toEqual({
        id: '3',
        name: 'Banana',
        quantity: 6,
        unitPrice: undefined,
        isPurchased: false,
        createdAt: '2025-12-30T10:00:00Z',
        updatedAt: '2025-12-30T10:00:00Z',
      });
    });

    it('deve mapear corretamente quando unitPrice é 0', () => {
      const dto: ShoppingItemDto = {
        id: '4',
        name: 'Item sem preço',
        quantity: 1,
        unit_price: 0,
        is_completed: true,
        created_at: '2025-12-30T10:00:00Z',
        updated_at: '2025-12-30T10:00:00Z',
      };

      const domain = mapShoppingItemDtoToDomain(dto);

      expect(domain.unitPrice).toBe(0);
    });

    it('deve suportar is_completed como sinônimo de is_purchased', () => {
      const dto: ShoppingItemDto = {
        id: '5',
        name: 'Arroz',
        quantity: 5,
        is_completed: true,
        created_at: '2025-12-30T10:00:00Z',
        updated_at: '2025-12-30T10:00:00Z',
      };

      const domain = mapShoppingItemDtoToDomain(dto);

      expect(domain.isPurchased).toBe(true);
    });

    it('deve fazer trim do nome do item', () => {
      const dto: ShoppingItemDto = {
        id: '6',
        name: '  Feijão Preto  ',
        quantity: 1,
        is_purchased: false,
        created_at: '2025-12-30T10:00:00Z',
        updated_at: '2025-12-30T10:00:00Z',
      };

      const domain = mapShoppingItemDtoToDomain(dto);

      expect(domain.name).toBe('Feijão Preto');
    });

    it('deve usar isPurchased como false quando nenhum campo de status é fornecido', () => {
      const dto: ShoppingItemDto = {
        id: '7',
        name: 'Item novo',
        quantity: 1,
        created_at: '2025-12-30T10:00:00Z',
        updated_at: '2025-12-30T10:00:00Z',
      };

      const domain = mapShoppingItemDtoToDomain(dto);

      expect(domain.isPurchased).toBe(false);
    });
  });

  describe('Validação de campos obrigatórios', () => {
    it('deve lançar erro se id estiver ausente', () => {
      const dto = {
        name: 'Item',
        quantity: 1,
        created_at: '2025-12-30T10:00:00Z',
        updated_at: '2025-12-30T10:00:00Z',
      } as any;

      expect(() => mapShoppingItemDtoToDomain(dto)).toThrow(
        'Campos obrigatórios ausentes em ShoppingItemDto: id'
      );
    });

    it('deve lançar erro se name estiver ausente', () => {
      const dto = {
        id: '1',
        quantity: 1,
        created_at: '2025-12-30T10:00:00Z',
        updated_at: '2025-12-30T10:00:00Z',
      } as any;

      expect(() => mapShoppingItemDtoToDomain(dto)).toThrow(
        'Campos obrigatórios ausentes em ShoppingItemDto: name'
      );
    });

    it('deve lançar erro se quantity estiver ausente', () => {
      const dto = {
        id: '1',
        name: 'Item',
        created_at: '2025-12-30T10:00:00Z',
        updated_at: '2025-12-30T10:00:00Z',
      } as any;

      expect(() => mapShoppingItemDtoToDomain(dto)).toThrow(
        'Campos obrigatórios ausentes em ShoppingItemDto: quantity'
      );
    });

    it('deve lançar erro se createdAt e created_at estiverem ausentes', () => {
      const dto = {
        id: '1',
        name: 'Item',
        quantity: 1,
        updated_at: '2025-12-30T10:00:00Z',
      } as any;

      expect(() => mapShoppingItemDtoToDomain(dto)).toThrow(
        'Campos obrigatórios ausentes em ShoppingItemDto: createdAt/created_at'
      );
    });

    it('deve lançar erro se updatedAt e updated_at estiverem ausentes', () => {
      const dto = {
        id: '1',
        name: 'Item',
        quantity: 1,
        created_at: '2025-12-30T10:00:00Z',
      } as any;

      expect(() => mapShoppingItemDtoToDomain(dto)).toThrow(
        'Campos obrigatórios ausentes em ShoppingItemDto: updatedAt/updated_at'
      );
    });

    it('deve lançar erro com múltiplos campos ausentes', () => {
      const dto = {
        id: '1',
      } as any;

      expect(() => mapShoppingItemDtoToDomain(dto)).toThrow(
        'Campos obrigatórios ausentes em ShoppingItemDto'
      );
      expect(() => mapShoppingItemDtoToDomain(dto)).toThrow('name');
      expect(() => mapShoppingItemDtoToDomain(dto)).toThrow('quantity');
      expect(() => mapShoppingItemDtoToDomain(dto)).toThrow('createdAt/created_at');
      expect(() => mapShoppingItemDtoToDomain(dto)).toThrow('updatedAt/updated_at');
    });
  });

  describe('Validação de tipos', () => {
    it('deve lançar erro se name não for uma string', () => {
      const dto = {
        id: '1',
        name: 123,
        quantity: 1,
        created_at: '2025-12-30T10:00:00Z',
        updated_at: '2025-12-30T10:00:00Z',
      } as any;

      expect(() => mapShoppingItemDtoToDomain(dto)).toThrow('Campo name deve ser uma string');
    });

    it('deve lançar erro se quantity não for um número', () => {
      const dto = {
        id: '1',
        name: 'Item',
        quantity: '5',
        created_at: '2025-12-30T10:00:00Z',
        updated_at: '2025-12-30T10:00:00Z',
      } as any;

      expect(() => mapShoppingItemDtoToDomain(dto)).toThrow(
        'Campo quantity deve ser um número positivo'
      );
    });

    it('deve lançar erro se quantity for negativo', () => {
      const dto = {
        id: '1',
        name: 'Item',
        quantity: -1,
        created_at: '2025-12-30T10:00:00Z',
        updated_at: '2025-12-30T10:00:00Z',
      } as any;

      expect(() => mapShoppingItemDtoToDomain(dto)).toThrow(
        'Campo quantity deve ser um número positivo'
      );
    });

    it('deve lançar erro se unitPrice não for um número quando fornecido', () => {
      const dto = {
        id: '1',
        name: 'Item',
        quantity: 1,
        unit_price: '10.50',
        created_at: '2025-12-30T10:00:00Z',
        updated_at: '2025-12-30T10:00:00Z',
      } as any;

      expect(() => mapShoppingItemDtoToDomain(dto)).toThrow(
        'Campo unitPrice deve ser um número positivo quando fornecido'
      );
    });

    it('deve lançar erro se unitPrice for negativo', () => {
      const dto = {
        id: '1',
        name: 'Item',
        quantity: 1,
        unit_price: -5.0,
        created_at: '2025-12-30T10:00:00Z',
        updated_at: '2025-12-30T10:00:00Z',
      } as any;

      expect(() => mapShoppingItemDtoToDomain(dto)).toThrow(
        'Campo unitPrice deve ser um número positivo quando fornecido'
      );
    });
  });
});
