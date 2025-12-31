import { useNavigation } from '@react-navigation/native';
import { ArrowLeft } from 'lucide-react-native';
import { useState } from 'react';
import {
    ScrollView,
    Text,
    TouchableOpacity,
    View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Button } from '../components/Button';
import { Input } from '../components/Input';
import { Label } from '../components/Label';
import { useListStore } from '../store/useListStore';
import { colors } from '../styles/colors';

export function CreateListScreen() {
  const navigation = useNavigation();
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const addList = useListStore((state) => state.addList);

  const handleSubmit = () => {
    if (!name.trim()) {
      alert('Por favor, digite o nome da lista');
      return;
    }

    const newList = {
      id: Date.now(),
      name: name.trim(),
      description: description.trim(),
      items: [],
      completed: 0,
      createdAt: new Date(),
    };

    addList(newList);
    navigation.goBack();
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
          flexDirection: 'row',
          alignItems: 'center',
          gap: 16,
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
        <Text
          style={{
            fontSize: 18,
            fontWeight: '600',
            color: colors.foreground,
          }}
        >
          Nova Lista
        </Text>
      </View>

      {/* Form */}
      <ScrollView
        style={{
          flex: 1,
          paddingHorizontal: 24,
          paddingVertical: 24,
        }}
      >
        {/* List Name */}
        <View style={{ marginBottom: 24 }}>
          <Label>Nome da Lista</Label>
          <Input
            placeholder="Digite o nome da lista"
            value={name}
            onChangeText={setName}
          />
        </View>

        {/* Description */}
        <View style={{ marginBottom: 24 }}>
          <Label>Descrição (opcional)</Label>
          <View
            style={{
              borderWidth: 1,
              borderColor: colors.border,
              borderRadius: 12,
              paddingHorizontal: 16,
              paddingVertical: 12,
              backgroundColor: colors.card,
              minHeight: 128,
            }}
          >
            <Text
              style={{
                fontSize: 16,
                color: colors.foreground,
              }}
            >
              {/* This would be a TextInput in a real implementation */}
            </Text>
          </View>
        </View>

        {/* Submit Button */}
        <Button onPress={handleSubmit}>Criar Lista</Button>
      </ScrollView>
    </SafeAreaView>
  );
}
