import { Ionicons } from '@expo/vector-icons';
import React from 'react';
import { StyleSheet, Text, TextStyle, TouchableOpacity, View, ViewStyle } from 'react-native';

import { useAppTheme } from '../../hooks';

export interface ListCardProps {
  title: string;
  itemsCount: number;
  purchasedItemsCount: number;
  loading?: boolean;
  onPress?: () => void;
  onMenuPress?: () => void;
  testID?: string;
}

export const ListCard: React.FC<ListCardProps> = ({
  title,
  itemsCount,
  purchasedItemsCount,
  loading = false,
  onPress,
  onMenuPress,
  testID,
}) => {
  const theme = useAppTheme();

  // Calcula progresso
  const progress = itemsCount > 0 ? (purchasedItemsCount / itemsCount) * 100 : 0;
  const isCompleted = progress === 100;

  if (loading) {
    return (
      <View
        style={[styles.card, { backgroundColor: theme.colors.surface }]}
        accessibilityRole='none'
        testID={testID ? `${testID}-skeleton` : 'list-card-skeleton'}
      >
        <View style={styles.contentRow}>
          <View style={[styles.skeletonIcon, { backgroundColor: theme.colors.border }]} />
          <View style={styles.skeletonContent}>
            <View style={[styles.skeletonTitle, { backgroundColor: theme.colors.border }]} />
            <View style={[styles.skeletonSubtitle, { backgroundColor: theme.colors.border }]} />
          </View>
        </View>
        <View style={[styles.skeletonProgress, { backgroundColor: theme.colors.border }]} />
      </View>
    );
  }

  return (
    <TouchableOpacity
      style={[
        styles.card,
        {
          backgroundColor: theme.colors.surface,
          borderColor: isCompleted ? theme.colors.success : theme.colors.border,
        },
      ]}
      onPress={onPress}
      activeOpacity={0.7}
      accessibilityRole='button'
      accessibilityLabel={`Lista: ${title}, ${purchasedItemsCount} de ${itemsCount} itens`}
      testID={testID || 'list-card'}
    >
      {/* Header: Ícone + Título + Menu */}
      <View style={styles.header}>
        <View style={styles.headerLeft}>
          {/* Ícone */}
          <View
            style={[
              styles.iconContainer,
              {
                backgroundColor: isCompleted
                  ? theme.colors.success + '20'
                  : theme.colors.primary + '20',
              },
            ]}
          >
            <Ionicons
              name={isCompleted ? 'checkmark-circle' : 'bag-outline'}
              size={24}
              color={isCompleted ? theme.colors.success : theme.colors.primary}
            />
          </View>

          {/* Título + Contador */}
          <View style={styles.titleContainer}>
            <Text style={[styles.title, { color: theme.colors.text }]} numberOfLines={1}>
              {title}
            </Text>
            <Text style={[styles.itemCount, { color: theme.colors.textSecondary }]}>
              {purchasedItemsCount} de {itemsCount} items
            </Text>
          </View>
        </View>

        {/* Botão de deletar */}
        {onMenuPress && (
          <TouchableOpacity
            style={styles.menuButton}
            onPress={onMenuPress}
            hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
            accessibilityLabel='Excluir lista'
            testID={testID ? `${testID}-delete` : 'list-card-delete'}
          >
            <Ionicons name='trash-outline' size={20} color={theme.colors.error} />
          </TouchableOpacity>
        )}
      </View>

      {/* Barra de Progresso */}
      <View style={styles.progressSection}>
        <Text style={[styles.progressLabel, { color: theme.colors.textSecondary }]}>Progress</Text>
        <View style={styles.progressRow}>
          <View style={[styles.progressBarContainer, { backgroundColor: theme.colors.border }]}>
            <View
              style={[
                styles.progressBarFill,
                {
                  width: `${progress}%`,
                  backgroundColor: isCompleted ? theme.colors.success : theme.colors.primary,
                },
              ]}
            />
          </View>
          <Text style={[styles.progressPercentage, { color: theme.colors.text }]}>
            {Math.round(progress)}%
          </Text>
        </View>
      </View>
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  card: {
    borderRadius: 16,
    borderWidth: 1,
    padding: 16,
    marginVertical: 8,
    marginHorizontal: 20,
    shadowColor: '#000',
    shadowOpacity: 0.08,
    shadowRadius: 12,
    shadowOffset: { width: 0, height: 4 },
    elevation: 3,
  } as ViewStyle,
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 16,
  } as ViewStyle,
  headerLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
    gap: 12,
  } as ViewStyle,
  iconContainer: {
    width: 48,
    height: 48,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
  } as ViewStyle,
  titleContainer: {
    flex: 1,
    justifyContent: 'center',
    gap: 4,
  } as ViewStyle,
  title: {
    fontSize: 17,
    fontWeight: '600',
    letterSpacing: 0.2,
  } as TextStyle,
  itemCount: {
    fontSize: 14,
    fontWeight: '400',
  } as TextStyle,
  menuButton: {
    padding: 4,
    marginTop: -4,
  } as ViewStyle,
  progressSection: {
    gap: 8,
  } as ViewStyle,
  progressLabel: {
    fontSize: 12,
    fontWeight: '600',
    textTransform: 'capitalize',
  } as TextStyle,
  progressRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  } as ViewStyle,
  progressBarContainer: {
    flex: 1,
    height: 8,
    borderRadius: 4,
    overflow: 'hidden',
  } as ViewStyle,
  progressBarFill: {
    height: '100%',
    borderRadius: 4,
  } as ViewStyle,
  progressPercentage: {
    fontSize: 14,
    fontWeight: '600',
    minWidth: 42,
    textAlign: 'right',
  } as TextStyle,
  // Skeleton styles
  contentRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    marginBottom: 16,
  } as ViewStyle,
  skeletonIcon: {
    width: 48,
    height: 48,
    borderRadius: 12,
  } as ViewStyle,
  skeletonContent: {
    flex: 1,
    gap: 8,
  } as ViewStyle,
  skeletonTitle: {
    width: '70%',
    height: 18,
    borderRadius: 4,
  } as ViewStyle,
  skeletonSubtitle: {
    width: '50%',
    height: 14,
    borderRadius: 4,
  } as ViewStyle,
  skeletonProgress: {
    width: '100%',
    height: 8,
    borderRadius: 4,
  } as ViewStyle,
});

export default ListCard;
