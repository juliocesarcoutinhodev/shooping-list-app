import { useFocusEffect, useRouter } from 'expo-router';
import React, { useCallback, useState } from 'react';
import { FlatList, RefreshControl, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { ShoppingListRemoteDataSource } from '@/src/data/data-sources/shopping-list-remote-data-source';
import { ShoppingListRepositoryImpl } from '@/src/data/repositories/shopping-list-repository';
import { ShoppingList } from '@/src/domain/entities';
import { DeleteShoppingListUseCase } from '@/src/domain/use-cases/delete-shopping-list-use-case';
import { GetMyListsUseCase } from '@/src/domain/use-cases/get-my-lists-use-case';

import { Button, ConfirmModal, Toast } from '../../components';
import FloatingActionButton from '../../components/fab';
import ListCard from '../../components/list-card';
import EmptyListSvg from '../../components/list-card/EmptyListSvg';
import { useAuth } from '../../contexts/auth-context';
import { useAppTheme } from '../../hooks';

// Instancio use cases com repository real
const remoteDataSource = new ShoppingListRemoteDataSource();
const repository = new ShoppingListRepositoryImpl(remoteDataSource);
const getMyListsUseCase = new GetMyListsUseCase(repository);
const deleteListUseCase = new DeleteShoppingListUseCase(repository);

export const ListsDashboardScreen: React.FC = () => {
  const theme = useAppTheme();
  const router = useRouter();
  const insets = useSafeAreaInsets();
  const { user } = useAuth();
  const [lists, setLists] = useState<ShoppingList[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Estados para modal de confirmação e toast
  const [confirmModalVisible, setConfirmModalVisible] = useState(false);
  const [selectedList, setSelectedList] = useState<ShoppingList | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [toastVisible, setToastVisible] = useState(false);
  const [toastMessage, setToastMessage] = useState('');
  const [toastType, setToastType] = useState<'success' | 'error'>('success');

  // Função para obter iniciais do usuário
  const getUserInitials = () => {
    if (!user?.name) return '?';
    const names = user.name.trim().split(' ');
    if (names.length >= 2) {
      return `${names[0][0]}${names[names.length - 1][0]}`.toUpperCase();
    }
    return names[0][0].toUpperCase();
  };

  const fetchLists = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getMyListsUseCase.execute();
      setLists(data);
    } catch (err) {
      const error = err as Error;
      setError(error?.message || 'Erro ao carregar listas');
    } finally {
      setLoading(false);
    }
  }, []);

  const onRefresh = useCallback(async () => {
    setRefreshing(true);
    setError(null);
    try {
      const data = await getMyListsUseCase.execute();
      setLists(data);
    } catch (err) {
      const error = err as Error;
      setError(error?.message || 'Erro ao recarregar listas');
    } finally {
      setRefreshing(false);
    }
  }, []);

  // Recarrega listas automaticamente quando a tela ganha foco
  // Isso garante que após criar uma lista, o dashboard seja atualizado
  useFocusEffect(
    useCallback(() => {
      fetchLists();
    }, [fetchLists])
  );

  // Função para abrir modal de confirmação de exclusão
  const handleDeleteList = useCallback((list: ShoppingList) => {
    setSelectedList(list);
    setConfirmModalVisible(true);
  }, []);

  // Função para confirmar exclusão
  const confirmDelete = useCallback(async () => {
    if (!selectedList) return;

    setDeleting(true);
    try {
      await deleteListUseCase.execute(selectedList.id);
      // Remove da UI imediatamente
      setLists(prev => prev.filter(l => l.id !== selectedList.id));
      // Fecha modal
      setConfirmModalVisible(false);
      setSelectedList(null);
      // Exibe toast de sucesso
      setToastMessage('Lista excluída com sucesso');
      setToastType('success');
      setToastVisible(true);
    } catch (err: any) {
      // Tratamento de erros específicos
      setConfirmModalVisible(false);
      setSelectedList(null);

      if (err?.status === 404) {
        // Já foi deletada, remove da UI
        setLists(prev => prev.filter(l => l.id === selectedList.id));
        setToastMessage('Lista não encontrada (já foi removida)');
        setToastType('error');
        setToastVisible(true);
      } else if (err?.status === 403) {
        setToastMessage('Você não tem permissão para deletar esta lista');
        setToastType('error');
        setToastVisible(true);
      } else {
        const message = err?.message || 'Erro ao deletar lista';
        setToastMessage(message);
        setToastType('error');
        setToastVisible(true);
      }
    } finally {
      setDeleting(false);
    }
  }, [selectedList]);

  // Função para cancelar exclusão
  const cancelDelete = useCallback(() => {
    setConfirmModalVisible(false);
    setSelectedList(null);
  }, []);

  const renderItem = useCallback(
    ({ item }: { item: ShoppingList }) => {
      // Uso itemsCount da API se disponível, senão calculo de items
      const totalItems = item.itemsCount ?? item.items.length;
      // pendingItemsCount é quantidade de itens não comprados
      // purchasedItemsCount = total - pending
      const purchasedItems =
        item.pendingItemsCount !== undefined
          ? totalItems - item.pendingItemsCount
          : item.items.filter(i => i.isPurchased).length;

      return (
        <ListCard
          title={item.title}
          itemsCount={totalItems}
          purchasedItemsCount={purchasedItems}
          onPress={() => router.push(`/lists/${item.id}` as never)}
          onMenuPress={() => handleDeleteList(item)}
          testID={`list-card-${item.id}`}
        />
      );
    },
    [handleDeleteList, router]
  );

  if (loading) {
    return (
      <View style={[styles.container, { backgroundColor: theme.colors.background }]}>
        {[1, 2, 3].map(i => (
          <ListCard key={i} title='' itemsCount={0} purchasedItemsCount={0} loading />
        ))}
      </View>
    );
  }

  if (error) {
    return (
      <View
        style={[
          styles.container,
          {
            backgroundColor: theme.colors.background,
            justifyContent: 'center',
            alignItems: 'center',
          },
        ]}
      >
        <Text style={[styles.errorText, { color: theme.colors.error }]}>{error}</Text>
        <TouchableOpacity
          style={[styles.retryButton, { backgroundColor: theme.colors.primary }]}
          onPress={fetchLists}
        >
          <Text style={[styles.retryButtonText, { color: theme.colors.textInverted }]}>
            Tentar novamente
          </Text>
        </TouchableOpacity>
      </View>
    );
  }

  if (!lists.length) {
    return (
      <View style={[styles.emptyContainer, { backgroundColor: theme.colors.background }]}>
        <EmptyListSvg width={160} height={120} />
        <Text style={[styles.emptyTitle, { color: theme.colors.text }]}>Sua lista está vazia</Text>
        <Text style={[styles.emptySubtitle, { color: theme.colors.textSecondary }]}>
          Crie uma lista para organizar suas compras do dia a dia
        </Text>
        <Button
          title='Começar minha lista'
          onPress={() => router.push('/create-list' as never)}
          size='large'
          variant='primary'
        />
      </View>
    );
  }

  return (
    <View style={[styles.container, { backgroundColor: theme.colors.background }]}>
      <View style={[styles.header, { paddingTop: insets.top + 8 }]}>
        <View style={styles.headerContent}>
          <View>
            <Text style={[styles.headerTitle, { color: theme.colors.text }]}>Minhas Listas</Text>
            <Text style={[styles.headerSubtitle, { color: theme.colors.textSecondary }]}>
              Organize suas compras
            </Text>
          </View>
          <TouchableOpacity
            style={[styles.avatar, { backgroundColor: '#059669' }]}
            onPress={() => router.push('/(tabs)/account' as never)}
            accessibilityLabel='Ver perfil'
            accessibilityRole='button'
          >
            <Text style={[styles.avatarText, { color: '#FFFFFF' }]}>
              {getUserInitials()}
            </Text>
          </TouchableOpacity>
        </View>
      </View>
      <FlatList
        data={lists}
        keyExtractor={item => item.id}
        renderItem={renderItem}
        contentContainerStyle={{ paddingTop: 8, paddingBottom: 100, gap: 8 }}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={onRefresh}
            tintColor={theme.colors.primary}
          />
        }
        showsVerticalScrollIndicator={false}
        accessibilityRole='list'
        testID='lists-flatlist'
      />
      <FloatingActionButton
        onPress={() => router.push('/create-list' as never)}
        testID='fab-create-list'
        accessibilityLabel='Criar nova lista'
      />

      {/* Modal de Confirmação */}
      <ConfirmModal
        visible={confirmModalVisible}
        title='Excluir lista?'
        message={`Excluir "${selectedList?.title}"?\nEssa ação não pode ser desfeita.`}
        confirmText='Excluir'
        cancelText='Cancelar'
        confirmVariant='destructive'
        onConfirm={confirmDelete}
        onCancel={cancelDelete}
        loading={deleting}
      />

      {/* Toast de Feedback */}
      <Toast
        visible={toastVisible}
        message={toastMessage}
        type={toastType}
        duration={3000}
        onHide={() => setToastVisible(false)}
        position='bottom'
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  header: {
    paddingHorizontal: 20,
    paddingBottom: 16,
  },
  headerContent: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  headerTitle: {
    fontSize: 32,
    fontWeight: '700',
    letterSpacing: 0.3,
    marginBottom: 4,
  },
  headerSubtitle: {
    fontSize: 16,
    fontWeight: '400',
  },
  avatar: {
    width: 48,
    height: 48,
    borderRadius: 24,
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOpacity: 0.1,
    shadowRadius: 4,
    shadowOffset: { width: 0, height: 2 },
    elevation: 2,
  },
  avatarText: {
    fontSize: 18,
    fontWeight: '700',
    letterSpacing: 0.5,
  },
  errorText: {
    fontSize: 16,
    marginBottom: 16,
    textAlign: 'center',
    fontWeight: '500',
  },
  retryButton: {
    borderRadius: 8,
    paddingVertical: 12,
    paddingHorizontal: 32,
    marginTop: 8,
  },
  retryButtonText: {
    fontSize: 16,
    fontWeight: '600',
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 24,
    paddingBottom: 32,
  },
  emptyTitle: {
    fontSize: 22,
    fontWeight: '700',
    marginTop: 28,
    marginBottom: 6,
    textAlign: 'center',
    letterSpacing: 0.2,
  },
  emptySubtitle: {
    fontSize: 15,
    color: '#888',
    marginBottom: 22,
    textAlign: 'center',
    fontWeight: '400',
    lineHeight: 22,
  },
});

export default ListsDashboardScreen;
