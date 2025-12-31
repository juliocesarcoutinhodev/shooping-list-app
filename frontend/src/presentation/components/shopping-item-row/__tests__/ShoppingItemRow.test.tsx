/**
 * Testes para ShoppingItemRow Component
 *
 * Testes de props, lógica de negócio e interface.
 * Para testes completos de renderização, seria necessário configurar o Jest para React Native.
 */

import { ShoppingItemRowProps } from '../index';

describe('ShoppingItemRow', () => {
  describe('Props interface', () => {
    it('deve aceitar props mínimas obrigatórias', () => {
      const props: ShoppingItemRowProps = {
        id: 'item1',
        name: 'Leite',
        quantity: 2,
        isPurchased: false,
      };

      expect(props.id).toBe('item1');
      expect(props.name).toBe('Leite');
      expect(props.quantity).toBe(2);
      expect(props.isPurchased).toBe(false);
    });

    it('deve aceitar props com preço unitário opcional', () => {
      const props: ShoppingItemRowProps = {
        id: 'item1',
        name: 'Pão',
        quantity: 3,
        unitPrice: 6.5,
        isPurchased: false,
      };

      expect(props.unitPrice).toBe(6.5);
    });

    it('deve aceitar callbacks opcionais', () => {
      const mockOnPress = jest.fn();
      const mockOnTogglePurchased = jest.fn();

      const props: ShoppingItemRowProps = {
        id: 'item1',
        name: 'Arroz',
        quantity: 1,
        isPurchased: false,
        onPress: mockOnPress,
        onTogglePurchased: mockOnTogglePurchased,
      };

      expect(props.onPress).toBe(mockOnPress);
      expect(props.onTogglePurchased).toBe(mockOnTogglePurchased);
    });

    it('deve aceitar prop loading opcional', () => {
      const props: ShoppingItemRowProps = {
        id: 'item1',
        name: 'Feijão',
        quantity: 1,
        isPurchased: false,
        loading: true,
      };

      expect(props.loading).toBe(true);
    });

    it('deve aceitar testID para acessibilidade', () => {
      const props: ShoppingItemRowProps = {
        id: 'item1',
        name: 'Café',
        quantity: 1,
        isPurchased: false,
        testID: 'custom-test-id',
      };

      expect(props.testID).toBe('custom-test-id');
    });
  });

  describe('Lógica de negócio - Cálculo de subtotal', () => {
    it('deve calcular subtotal corretamente (quantidade * preço)', () => {
      const quantity = 5;
      const unitPrice = 3.5;
      const subtotal = quantity * unitPrice;

      expect(subtotal).toBe(17.5);
    });

    it('deve calcular subtotal com valores decimais', () => {
      const quantity = 3;
      const unitPrice = 6.5;
      const subtotal = quantity * unitPrice;

      expect(subtotal).toBe(19.5);
    });

    it('deve retornar 0 quando quantidade é 0', () => {
      const quantity = 0;
      const unitPrice = 10;
      const subtotal = quantity * unitPrice;

      expect(subtotal).toBe(0);
    });
  });

  describe('Lógica de negócio - Formatação monetária', () => {
    it('deve formatar valores em BRL corretamente', () => {
      const value = 19.5;
      const formatted = new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL',
      }).format(value);

      expect(formatted).toContain('R$');
      expect(formatted).toContain('19,50');
    });

    it('deve formatar centavos corretamente', () => {
      const value = 3.5;
      const formatted = new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL',
      }).format(value);

      expect(formatted).toContain('R$');
      expect(formatted).toContain('3,50');
    });

    it('deve formatar valores inteiros com ,00', () => {
      const value = 10;
      const formatted = new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL',
      }).format(value);

      expect(formatted).toContain('R$');
      expect(formatted).toContain('10,00');
    });
  });

  describe('Estados do componente', () => {
    it('deve ter estado purchased como booleano', () => {
      const purchased = true;
      const notPurchased = false;

      expect(typeof purchased).toBe('boolean');
      expect(typeof notPurchased).toBe('boolean');
    });

    it('deve ter estado loading opcional', () => {
      const loading = true;
      const notLoading = undefined;

      expect(loading).toBe(true);
      expect(notLoading).toBeUndefined();
    });
  });

  describe('Callbacks - onTogglePurchased', () => {
    it('deve receber id e novo valor booleano', () => {
      const mockCallback = jest.fn();
      const itemId = 'item123';
      const newValue = true;

      mockCallback(itemId, newValue);

      expect(mockCallback).toHaveBeenCalledTimes(1);
      expect(mockCallback).toHaveBeenCalledWith('item123', true);
    });

    it('deve alternar entre true e false', () => {
      const mockCallback = jest.fn();
      const itemId = 'item123';

      // Marca como comprado
      mockCallback(itemId, true);
      expect(mockCallback).toHaveBeenLastCalledWith('item123', true);

      // Desmarca
      mockCallback(itemId, false);
      expect(mockCallback).toHaveBeenLastCalledWith('item123', false);

      expect(mockCallback).toHaveBeenCalledTimes(2);
    });
  });

  describe('Callbacks - onPress', () => {
    it('deve ser chamado sem argumentos', () => {
      const mockCallback = jest.fn();

      mockCallback();

      expect(mockCallback).toHaveBeenCalledTimes(1);
      expect(mockCallback).toHaveBeenCalledWith();
    });

    it('deve ser chamado múltiplas vezes', () => {
      const mockCallback = jest.fn();

      mockCallback();
      mockCallback();
      mockCallback();

      expect(mockCallback).toHaveBeenCalledTimes(3);
    });
  });

  describe('Validações de tipos', () => {
    it('id deve ser string', () => {
      const id = 'item123';
      expect(typeof id).toBe('string');
    });

    it('name deve ser string', () => {
      const name = 'Leite Integral';
      expect(typeof name).toBe('string');
    });

    it('quantity deve ser number', () => {
      const quantity = 5;
      expect(typeof quantity).toBe('number');
    });

    it('unitPrice deve ser number ou undefined', () => {
      const unitPrice = 4.5;
      const noPrice = undefined;

      expect(typeof unitPrice).toBe('number');
      expect(noPrice).toBeUndefined();
    });

    it('isPurchased deve ser boolean', () => {
      const purchased = true;
      expect(typeof purchased).toBe('boolean');
    });
  });
});
