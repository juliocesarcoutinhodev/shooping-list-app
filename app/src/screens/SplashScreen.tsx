import { useNavigation } from '@react-navigation/native';
import { useEffect } from 'react';
import { Text, View } from 'react-native';
import LogoCart from '../components/LogoCart';
import { colors } from '../styles/colors';

export function SplashScreen() {
  const navigation = useNavigation();

  useEffect(() => {
    const timer = setTimeout(() => {
      // Substitui a rota atual pela tela de Login sem warnings
      navigation.replace('login' as never);
    }, 2500);

    return () => clearTimeout(timer);
  }, [navigation]);

  return (
    <View
      style={{
        flex: 1,
        backgroundColor: colors.primary,
        justifyContent: 'center',
        alignItems: 'center',
        paddingHorizontal: 24,
      }}
    >
      <View
        style={{
          width: 128,
          height: 128,
          borderRadius: 32,
          backgroundColor: 'rgba(255, 255, 255, 0.12)',
          justifyContent: 'center',
          alignItems: 'center',
          marginBottom: 32,
        }}
      >
        <LogoCart size={96} color={colors.primaryForeground} />
      </View>

      <Text
        style={{
          fontSize: 32,
          fontWeight: '600',
          color: colors.primaryForeground,
          marginBottom: 8,
          textAlign: 'center',
        }}
      >
        Lista de Compras
      </Text>

      <Text
        style={{
          fontSize: 16,
          color: 'rgba(255, 255, 255, 0.8)',
          textAlign: 'center',
        }}
      >
        Simples. Prático. Organizado.
      </Text>
    </View>
  );
}
