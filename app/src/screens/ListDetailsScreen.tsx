import { useNavigation, useRoute } from '@react-navigation/native';
import {
    ArrowLeft,
    DollarSign,
    Edit2,
    Hash,
    MoreVertical,
    Plus,
    Trash2,
} from 'lucide-react-native';
import { useState } from 'react';
import {
    FlatList,
    Modal,
    Text,
    TextInput,
    TouchableOpacity,
    View
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Button } from '../components/Button';
import { Input } from '../components/Input';
import { Label } from '../components/Label';
import { useListStore } from '../store/useListStore';
import { colors } from '../styles/colors';
import { ListItem } from '../types';

export function ListDetailsScreen() {
  const route = useRoute();
  const navigation = useNavigation();
  const currentList = useListStore((state) => state.currentList);
  const toggleItemComplete = useListStore(
    (state) => state.toggleItemComplete
  );
  const deleteItem = useListStore((state) => state.deleteItem);
  const addItem = useListStore((state) => state.addItem);
  const updateItem = useListStore((state) => state.updateItem);

  const [isAddItemOpen, setIsAddItemOpen] = useState(false);
  const [isEditItemOpen, setIsEditItemOpen] = useState(false);
  const [newItemName, setNewItemName] = useState('');
  const [editingItem, setEditingItem] = useState<ListItem | null>(null);

  if (!currentList) {
    return (
      <SafeAreaView
        style={{
          flex: 1,
          backgroundColor: colors.background,
          justifyContent: 'center',
          alignItems: 'center',
        }}
      >
        <Text>Lista não encontrada</Text>
      </SafeAreaView>
    );
  }

  const handleAddItem = () => {
    if (!newItemName.trim()) {
      alert('Digite o nome do item');
      return;
    }

    const newItem: ListItem = {
      id: Date.now(),
      name: newItemName.trim(),
      completed: false,
    };

    addItem(currentList.id, newItem);
    setNewItemName('');
    setIsAddItemOpen(false);
  };

  const handleEditClick = (item: ListItem) => {
    setEditingItem({ ...item });
    setIsEditItemOpen(true);
  };

  const handleSaveEdit = () => {
    if (editingItem) {
      updateItem(currentList.id, editingItem.id, editingItem);
      setIsEditItemOpen(false);
      setEditingItem(null);
    }
  };

  const totalPrice = currentList.items.reduce((sum, item) => {
    if (item.price && item.quantity) {
      return sum + item.price * item.quantity;
    } else if (item.price) {
      return sum + item.price;
    }
    return sum;
  }, 0);

  const renderItemCard = ({ item }: { item: ListItem }) => (
    <TouchableOpacity
      onPress={() => handleEditClick(item)}
      style={{
        backgroundColor: colors.card,
        borderWidth: 1,
        borderColor: colors.border,
        borderRadius: 12,
        padding: 16,
        marginBottom: 12,
        flexDirection: 'row',
        alignItems: 'flex-start',
        gap: 12,
      }}
    >
      {/* Checkbox */}
      <TouchableOpacity
        onPress={() => toggleItemComplete(currentList.id, item.id)}
        style={{
          width: 24,
          height: 24,
          borderRadius: 8,
          borderWidth: 2,
          borderColor: colors.border,
          backgroundColor: item.completed ? colors.primary : 'transparent',
          justifyContent: 'center',
          alignItems: 'center',
          marginTop: 4,
        }}
      >
        {item.completed && (
          <Text style={{ color: colors.primaryForeground, fontWeight: '600' }}>
            ✓
          </Text>
        )}
      </TouchableOpacity>

      {/* Item Content */}
      <View style={{ flex: 1 }}>
        <View
          style={{
            flexDirection: 'row',
            justifyContent: 'space-between',
            alignItems: 'flex-start',
            gap: 8,
            marginBottom: 8,
          }}
        >
          <Text
            style={{
              fontSize: 16,
              fontWeight: item.completed ? '400' : '600',
              color: item.completed
                ? colors.mutedForeground
                : colors.foreground,
              textDecorationLine: item.completed ? 'line-through' : 'none',
              flex: 1,
            }}
          >
            {item.name}
          </Text>
          <TouchableOpacity
            onPress={() => deleteItem(currentList.id, item.id)}
            style={{
              width: 32,
              height: 32,
              borderRadius: 8,
              backgroundColor: colors.destructive + '10',
              justifyContent: 'center',
              alignItems: 'center',
            }}
          >
            <Trash2 size={16} color={colors.destructive} />
          </TouchableOpacity>
        </View>

        {/* Quantity and Price */}
        <View style={{ flexDirection: 'row', gap: 16, alignItems: 'center' }}>
          {item.quantity && (
            <View style={{ flexDirection: 'row', alignItems: 'center', gap: 4 }}>
              <Hash size={14} color={colors.mutedForeground} />
              <Text
                style={{
                  fontSize: 12,
                  color: colors.mutedForeground,
                }}
              >
                {item.quantity}x
              </Text>
            </View>
          )}
          {item.price && (
            <View style={{ flexDirection: 'row', alignItems: 'center', gap: 4 }}>
              <DollarSign size={14} color={colors.secondary} />
              <Text
                style={{
                  fontSize: 12,
                  fontWeight: '600',
                  color: colors.secondary,
                }}
              >
                R$ {item.price.toFixed(2)}
              </Text>
              {item.quantity && item.quantity > 1 && (
                <Text
                  style={{
                    fontSize: 10,
                    color: colors.mutedForeground,
                    marginLeft: 4,
                  }}
                >
                  (total: R$ {(item.price * item.quantity).toFixed(2)})
                </Text>
              )}
            </View>
          )}
        </View>
      </View>
    </TouchableOpacity>
  );

  return (
    <SafeAreaView
      style={{
        flex: 1,
        backgroundColor: colors.background,
      }}
    >
      {/* Header */}
      <View
        style={{
          backgroundColor: colors.card,
          borderBottomWidth: 1,
          borderBottomColor: colors.border,
          paddingHorizontal: 24,
          paddingVertical: 16,
        }}
      >
        <View
          style={{
            flexDirection: 'row',
            alignItems: 'flex-start',
            gap: 16,
            marginBottom: 16,
          }}
        >
          <TouchableOpacity
            onPress={() => navigation.goBack()}
            style={{
              width: 40,
              height: 40,
              borderRadius: 8,
              backgroundColor: colors.muted,
              justifyContent: 'center',
              alignItems: 'center',
            }}
          >
            <ArrowLeft size={20} color={colors.foreground} />
          </TouchableOpacity>
          <View style={{ flex: 1 }}>
            <Text
              style={{
                fontSize: 18,
                fontWeight: '600',
                color: colors.foreground,
                marginBottom: 4,
              }}
            >
              {currentList.name}
            </Text>
            <Text
              style={{
                fontSize: 12,
                color: colors.mutedForeground,
              }}
            >
              {currentList.completed} de {currentList.items.length} itens
            </Text>
          </View>
          <TouchableOpacity>
            <MoreVertical size={20} color={colors.foreground} />
          </TouchableOpacity>
        </View>

        {/* Total */}
        {totalPrice > 0 && (
          <View
            style={{
              backgroundColor: colors.accent + '50',
              borderWidth: 1,
              borderColor: colors.primary + '30',
              borderRadius: 12,
              paddingHorizontal: 16,
              paddingVertical: 12,
              flexDirection: 'row',
              justifyContent: 'space-between',
              alignItems: 'center',
            }}
          >
            <Text
              style={{
                fontSize: 12,
                color: colors.mutedForeground,
              }}
            >
              Total estimado:
            </Text>
            <Text
              style={{
                fontSize: 18,
                fontWeight: '600',
                color: colors.primary,
              }}
            >
              R$ {totalPrice.toFixed(2)}
            </Text>
          </View>
        )}
      </View>

      {/* Items List */}
      <FlatList
        data={currentList.items}
        renderItem={renderItemCard}
        keyExtractor={(item) => item.id.toString()}
        contentContainerStyle={{
          paddingHorizontal: 24,
          paddingVertical: 24,
        }}
      />

      {/* Add Item FAB */}
      <TouchableOpacity
        onPress={() => setIsAddItemOpen(true)}
        style={{
          position: 'absolute',
          bottom: 32,
          right: 24,
          width: 64,
          height: 64,
          borderRadius: 32,
          backgroundColor: colors.primary,
          justifyContent: 'center',
          alignItems: 'center',
          shadowColor: colors.primary,
          shadowOffset: { width: 0, height: 8 },
          shadowOpacity: 0.3,
          shadowRadius: 16,
          elevation: 8,
        }}
      >
        <Plus size={28} color={colors.primaryForeground} />
      </TouchableOpacity>

      {/* Add Item Modal */}
      <Modal
        visible={isAddItemOpen}
        transparent
        animationType="slide"
        onRequestClose={() => setIsAddItemOpen(false)}
      >
        <View
          style={{
            flex: 1,
            backgroundColor: 'rgba(0, 0, 0, 0.5)',
            justifyContent: 'flex-end',
          }}
        >
          <View
            style={{
              backgroundColor: colors.background,
              borderTopLeftRadius: 24,
              borderTopRightRadius: 24,
              paddingHorizontal: 24,
              paddingTop: 24,
              paddingBottom: 32,
            }}
          >
            <Text
              style={{
                fontSize: 18,
                fontWeight: '600',
                color: colors.foreground,
                marginBottom: 24,
              }}
            >
              Adicionar Item
            </Text>

            <View style={{ marginBottom: 20 }}>
              <Label>Nome do Item</Label>
              <Input
                placeholder="Ex: Arroz, Feijão, Leite..."
                value={newItemName}
                onChangeText={setNewItemName}
                autoFocus
              />
            </View>

            <Button onPress={handleAddItem}>Adicionar Item</Button>
          </View>
        </View>
      </Modal>

      {/* Edit Item Modal */}
      <Modal
        visible={isEditItemOpen}
        transparent
        animationType="slide"
        onRequestClose={() => setIsEditItemOpen(false)}
      >
        <View
          style={{
            flex: 1,
            backgroundColor: 'rgba(0, 0, 0, 0.5)',
            justifyContent: 'flex-end',
          }}
        >
          <View
            style={{
              backgroundColor: colors.background,
              borderTopLeftRadius: 24,
              borderTopRightRadius: 24,
              paddingHorizontal: 24,
              paddingTop: 24,
              paddingBottom: 32,
            }}
          >
            <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8, marginBottom: 24 }}>
              <Edit2 size={20} color={colors.foreground} />
              <Text
                style={{
                  fontSize: 18,
                  fontWeight: '600',
                  color: colors.foreground,
                }}
              >
                Editar Item
              </Text>
            </View>

            {editingItem && (
              <>
                {/* Item Name */}
                <View style={{ marginBottom: 20 }}>
                  <Label>Nome do Item</Label>
                  <Input
                    value={editingItem.name}
                    onChangeText={(text) =>
                      setEditingItem({ ...editingItem, name: text })
                    }
                  />
                </View>

                {/* Quantity and Price in Grid */}
                <View
                  style={{
                    flexDirection: 'row',
                    gap: 16,
                    marginBottom: 20,
                  }}
                >
                  {/* Quantity */}
                  <View style={{ flex: 1 }}>
                    <Label>Quantidade</Label>
                    <TextInput
                      keyboardType="number-pad"
                      placeholder="0"
                      value={editingItem.quantity?.toString() ?? ''}
                      onChangeText={(text) =>
                        setEditingItem({
                          ...editingItem,
                          quantity: text ? parseInt(text) : undefined,
                        })
                      }
                      style={{
                        height: 56,
                        paddingHorizontal: 16,
                        borderRadius: 12,
                        fontSize: 16,
                        color: colors.foreground,
                        backgroundColor: colors.card,
                        borderWidth: 1,
                        borderColor: colors.border,
                      }}
                    />
                  </View>

                  {/* Price */}
                  <View style={{ flex: 1 }}>
                    <Label>Preço (R$)</Label>
                    <TextInput
                      keyboardType="decimal-pad"
                      placeholder="0,00"
                      value={editingItem.price?.toString() ?? ''}
                      onChangeText={(text) =>
                        setEditingItem({
                          ...editingItem,
                          price: text ? parseFloat(text) : undefined,
                        })
                      }
                      style={{
                        height: 56,
                        paddingHorizontal: 16,
                        borderRadius: 12,
                        fontSize: 16,
                        color: colors.foreground,
                        backgroundColor: colors.card,
                        borderWidth: 1,
                        borderColor: colors.border,
                      }}
                    />
                  </View>
                </View>

                {/* Subtotal */}
                {editingItem.quantity && editingItem.price && (
                  <View
                    style={{
                      backgroundColor: colors.accent + '50',
                      borderWidth: 1,
                      borderColor: colors.primary + '30',
                      borderRadius: 12,
                      paddingHorizontal: 16,
                      paddingVertical: 12,
                      marginBottom: 20,
                      flexDirection: 'row',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                    }}
                  >
                    <Text
                      style={{
                        fontSize: 12,
                        color: colors.mutedForeground,
                      }}
                    >
                      Subtotal:
                    </Text>
                    <Text
                      style={{
                        fontSize: 16,
                        fontWeight: '600',
                        color: colors.primary,
                      }}
                    >
                      R$ {(editingItem.quantity * editingItem.price).toFixed(2)}
                    </Text>
                  </View>
                )}

                <Button onPress={handleSaveEdit}>Salvar</Button>
              </>
            )}
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
}
