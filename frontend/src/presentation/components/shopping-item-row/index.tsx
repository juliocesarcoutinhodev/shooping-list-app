import { Ionicons } from '@expo/vector-icons';
import React from 'react';
import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';

import { useAppTheme } from '../../hooks';

export interface ShoppingItemRowProps {
  id: string;
  name: string;
  quantity: number;
  unitPrice?: number;
  isPurchased: boolean;
  loading?: boolean;
  onPress?: () => void;
  onTogglePurchased?: (id: string, newValue: boolean) => void;
  onDelete?: (id: string) => void;
  testID?: string;
}

/**
 * ShoppingItemRow Component
 *
 * Exibe um item da lista de compras com:
 * - Checkbox (checked/unchecked)
 * - Nome (strike-through se comprado)
 * - Quantidade (ex: "2x")
 * - Preço unitário (opcional)
 * - Subtotal (qty * unitPrice, se aplicável)
 *
 * Acessibilidade:
 * - role="checkbox" para o checkbox
 * - accessibilityLabel descritivo
 * - testID para testes automatizados
 */
export const ShoppingItemRow: React.FC<ShoppingItemRowProps> = ({
  id,
  name,
  quantity,
  unitPrice,
  isPurchased,
  loading = false,
  onPress,
  onTogglePurchased,
  onDelete,
  testID,
}) => {
  const theme = useAppTheme();

  // Calcula subtotal se houver preço válido
  const subtotal =
    unitPrice !== undefined && unitPrice !== null && unitPrice > 0
      ? quantity * unitPrice
      : undefined;

  // Formata valores monetários
  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(value);
  };

  // Handler do checkbox
  const handleToggleCheckbox = () => {
    if (!loading && onTogglePurchased) {
      onTogglePurchased(id, !isPurchased);
    }
  };

  // Handler do press na row (para editar no futuro)
  const handlePress = () => {
    if (!loading && onPress) {
      onPress();
    }
  };

  // Estado loading (placeholder simples)
  if (loading) {
    return (
      <View
        style={[styles.container, { backgroundColor: theme.colors.surface }]}
        accessibilityRole='none'
        testID={testID ? `${testID}-skeleton` : 'shopping-item-row-skeleton'}
      >
        <View style={[styles.skeletonCheckbox, { backgroundColor: theme.colors.border }]} />
        <View style={styles.skeletonContent}>
          <View style={[styles.skeletonName, { backgroundColor: theme.colors.border }]} />
          <View style={[styles.skeletonQuantity, { backgroundColor: theme.colors.border }]} />
        </View>
        {unitPrice && (
          <View style={[styles.skeletonPrice, { backgroundColor: theme.colors.border }]} />
        )}
      </View>
    );
  }

  return (
    <TouchableOpacity
      style={[
        styles.container,
        {
          backgroundColor: theme.colors.surface,
          borderColor: theme.colors.border,
        },
        isPurchased && { opacity: 0.85 },
      ]}
      onPress={handlePress}
      activeOpacity={0.7}
      accessibilityRole='button'
      accessibilityLabel={`${name}, ${quantity} ${quantity > 1 ? 'unidades' : 'unidade'}${
        isPurchased ? ', comprado' : ', não comprado'
      }${subtotal ? `, ${formatCurrency(subtotal)}` : ''}`}
      testID={testID}
    >
      {/* Checkbox */}
      <TouchableOpacity
        style={[
          styles.checkbox,
          {
            borderColor: isPurchased ? '#059669' : '#A7F3D0',
            backgroundColor: isPurchased ? '#059669' : 'transparent',
          },
        ]}
        onPress={handleToggleCheckbox}
        activeOpacity={0.7}
        accessibilityRole='checkbox'
        accessibilityState={{ checked: isPurchased }}
        accessibilityLabel={isPurchased ? 'Marcar como não comprado' : 'Marcar como comprado'}
        testID={testID ? `${testID}-checkbox` : undefined}
      >
        {isPurchased && (
          <Ionicons name='checkmark' size={18} color={theme.colors.surface} testID='check-icon' />
        )}
      </TouchableOpacity>

      {/* Content */}
      <View style={styles.content}>
        {/* Nome */}
        <Text
          style={[
            styles.name,
            { color: isPurchased ? theme.colors.textSecondary : theme.colors.text },
            isPurchased && styles.strikethrough,
          ]}
          numberOfLines={2}
          testID={testID ? `${testID}-name` : undefined}
        >
          {name}
        </Text>

        {/* Quantidade, Preço Unitário e Subtotal */}
        <View style={styles.detailsRow}>
          <Text
            style={[styles.quantity, { color: theme.colors.textSecondary }]}
            testID={testID ? `${testID}-quantity` : undefined}
          >
            # {quantity}x
          </Text>
          {unitPrice !== undefined && unitPrice !== null && unitPrice > 0 && (
            <>
              <Text
                style={[styles.unitPrice, { color: '#10B981' }]}
                testID={testID ? `${testID}-unit-price` : undefined}
              >
                $ {formatCurrency(unitPrice)}
              </Text>
              {subtotal && subtotal > 0 && quantity > 1 && (
                <Text
                  style={[styles.subtotal, { color: theme.colors.textSecondary }]}
                  testID={testID ? `${testID}-subtotal` : undefined}
                >
                  (total: {formatCurrency(subtotal)})
                </Text>
              )}
            </>
          )}
        </View>
      </View>

      {/* Menu Button (3 pontinhos) */}
      {onDelete && (
        <TouchableOpacity
          onPress={() => onDelete(id)}
          hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
          accessibilityLabel='Excluir item'
          accessibilityRole='button'
          testID={testID ? `${testID}-menu` : undefined}
          style={styles.menuButton}
        >
          <Ionicons name='ellipsis-vertical' size={20} color={theme.colors.textSecondary} />
        </TouchableOpacity>
      )}
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    paddingVertical: 16,
    paddingHorizontal: 16,
    borderRadius: 12,
    borderWidth: 1,
    marginBottom: 12,
    minHeight: 70,
  },

  // Checkbox
  checkbox: {
    width: 24,
    height: 24,
    borderRadius: 12,
    borderWidth: 2,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
    marginTop: 2,
  },

  // Content
  content: {
    flex: 1,
  },
  name: {
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 6,
  },
  strikethrough: {
    textDecorationLine: 'line-through',
    opacity: 0.85,
  },
  detailsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    flexWrap: 'wrap',
    gap: 8,
  },
  quantity: {
    fontSize: 14,
    fontWeight: '500',
  },
  unitPrice: {
    fontSize: 14,
    fontWeight: '500',
  },
  subtotal: {
    fontSize: 14,
    fontWeight: '400',
  },

  // Loading Skeleton
  skeletonCheckbox: {
    width: 24,
    height: 24,
    borderRadius: 6,
    marginRight: 12,
  },
  skeletonContent: {
    flex: 1,
  },
  skeletonName: {
    height: 16,
    width: '70%',
    borderRadius: 4,
    marginBottom: 6,
  },
  skeletonQuantity: {
    height: 12,
    width: '30%',
    borderRadius: 4,
  },
  skeletonPrice: {
    height: 14,
    width: 60,
    borderRadius: 4,
    marginLeft: 8,
  },
  menuButton: {
    padding: 8,
    marginLeft: 8,
    justifyContent: 'center',
    alignItems: 'center',
  },
});
