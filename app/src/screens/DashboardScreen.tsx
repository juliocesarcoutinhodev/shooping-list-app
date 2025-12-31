import { useNavigation } from '@react-navigation/native';
import {
    CheckCircle2,
    LogOut,
    MoreVertical,
    Plus,
    ShoppingBag,
} from 'lucide-react-native';
import {
    FlatList,
    Text,
    TouchableOpacity,
    View
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Card } from '../components/Card';
import { useAuthStore } from '../store/useAuthStore';
import { useListStore } from '../store/useListStore';
import { colors } from '../styles/colors';
import { ShoppingList } from '../types';

export function DashboardScreen() {
  const navigation = useNavigation();
  const lists = useListStore((state) => state.lists);
  const setCurrentList = useListStore((state) => state.setCurrentList);
  const user = useAuthStore((state) => state.user);
  const logout = useAuthStore((state) => state.logout);

  const handleLogout = () => {
    logout();
    navigation.reset({
      index: 0,
      routes: [{ name: 'login' as never }],
    });
  };

  const handleListPress = (list: ShoppingList) => {
    setCurrentList(list);
    navigation.navigate('list-details' as never, { id: list.id } as never);
  };

  const renderListCard = ({ item }: { item: ShoppingList }) => {
    const progress = (item.completed / item.items.length) * 100;
    const isCompleted = item.completed === item.items.length;

    return (
      <TouchableOpacity onPress={() => handleListPress(item)}>
        <Card style={{ marginBottom: 16 }}>
          <View
            style={{
              flexDirection: 'row',
              justifyContent: 'space-between',
              alignItems: 'flex-start',
              marginBottom: 16,
            }}
          >
            <View style={{ flexDirection: 'row', gap: 12, flex: 1 }}>
              <View
                style={{
                  width: 48,
                  height: 48,
                  borderRadius: 12,
                  backgroundColor: isCompleted
                    ? colors.secondary + '20'
                    : colors.primary + '20',
                  justifyContent: 'center',
                  alignItems: 'center',
                }}
              >
                {isCompleted ? (
                  <CheckCircle2
                    size={24}
                    color={colors.secondary}
                  />
                ) : (
                  <ShoppingBag
                    size={24}
                    color={colors.primary}
                  />
                )}
              </View>
              <View style={{ flex: 1 }}>
                <Text
                  style={{
                    fontSize: 16,
                    fontWeight: '600',
                    color: colors.foreground,
                    marginBottom: 4,
                  }}
                >
                  {item.name}
                </Text>
                <Text
                  style={{
                    fontSize: 12,
                    color: colors.mutedForeground,
                  }}
                >
                  {item.completed} de {item.items.length} itens
                </Text>
              </View>
            </View>
            <TouchableOpacity>
              <MoreVertical
                size={20}
                color={colors.mutedForeground}
              />
            </TouchableOpacity>
          </View>

          {/* Progress */}
          <View style={{ gap: 8 }}>
            <View
              style={{
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
                Progresso
              </Text>
              <Text
                style={{
                  fontSize: 14,
                  fontWeight: '600',
                  color: isCompleted ? colors.secondary : colors.primary,
                }}
              >
                {Math.round(progress)}%
              </Text>
            </View>
            <View
              style={{
                height: 8,
                backgroundColor: colors.muted,
                borderRadius: 4,
                overflow: 'hidden',
              }}
            >
              <View
                style={{
                  height: '100%',
                  width: `${progress}%`,
                  backgroundColor: isCompleted
                    ? colors.secondary
                    : colors.primary,
                }}
              />
            </View>
          </View>
        </Card>
      </TouchableOpacity>
    );
  };

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
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: 16,
          }}
        >
          <View style={{ flex: 1 }}>
            <Text
              style={{
                fontSize: 20,
                fontWeight: '600',
                color: colors.foreground,
                marginBottom: 4,
              }}
            >
              Minhas Listas
            </Text>
            <Text
              style={{
                fontSize: 14,
                color: colors.mutedForeground,
              }}
            >
              Organize suas compras
            </Text>
          </View>
          <TouchableOpacity
            onPress={handleLogout}
            style={{
              width: 48,
              height: 48,
              borderRadius: 24,
              backgroundColor: colors.primary,
              justifyContent: 'center',
              alignItems: 'center',
            }}
          >
            <LogOut size={24} color={colors.primaryForeground} />
          </TouchableOpacity>
        </View>
      </View>

      {/* Lists */}
      <FlatList
        data={lists}
        renderItem={renderListCard}
        keyExtractor={(item) => item.id.toString()}
        contentContainerStyle={{
          paddingHorizontal: 24,
          paddingVertical: 24,
          flexGrow: 1,
        }}
        scrollEnabled={true}
      />

      {/* Floating Action Button */}
      <TouchableOpacity
        onPress={() => navigation.navigate('create-list' as never)}
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
    </SafeAreaView>
  );
}
