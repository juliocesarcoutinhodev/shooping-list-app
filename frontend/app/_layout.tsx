import { DarkTheme, DefaultTheme, ThemeProvider } from '@react-navigation/native';
import { Stack, useRouter, useSegments } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { useEffect } from 'react';
import { useColorScheme } from 'react-native';
import 'react-native-reanimated';

import { Loader } from '@/src/presentation/components';
import { AuthProvider, useAuth } from '@/src/presentation/contexts/auth-context';

function NavigationContent() {
  const { isAuthenticated, isLoading } = useAuth();
  const segments = useSegments();
  const router = useRouter();
  const colorScheme = useColorScheme();

  useEffect(() => {
    // Aguardo o carregamento da sessão antes de decidir navegação
    if (isLoading) return;

    // Verifico se estou na área protegida (tabs) ou na área pública (login/register)
    const inProtectedArea = segments[0] === '(tabs)';
    const isPublicRoute = segments[0] === 'login' || segments[0] === 'register';
    const _isModalRoute =
      segments[0] === 'create-list' || segments[0] === 'modal' || segments[0] === 'settings';

    // Se não estou autenticado mas tentando acessar área protegida, redireciono para login
    if (!isAuthenticated && inProtectedArea) {
      router.replace('/login' as never);
    }

    // Se estou autenticado mas na área de login/register, redireciono para home
    // Não redireciono se estiver em rotas modais
    if (isAuthenticated && isPublicRoute) {
      router.replace('/(tabs)' as never);
    }
  }, [isAuthenticated, isLoading, router, segments]);

  // Exibo loader enquanto verifico se existe sessão salva
  if (isLoading) {
    return <Loader variant='spinner' size='large' text='Carregando...' />;
  }

  return (
    <Stack
      screenOptions={{
        headerShadowVisible: false, // Remove sombra de todos os headers
        headerStyle: {
          elevation: 0,
          shadowOpacity: 0,
          shadowOffset: { width: 0, height: 0 },
          shadowRadius: 0,
          borderBottomWidth: 0,
        },
      }}
    >
      <Stack.Screen name='login' options={{ headerShown: false }} />
      <Stack.Screen name='register' options={{ headerShown: false }} />
      <Stack.Screen name='(tabs)' options={{ headerShown: false }} />
      <Stack.Screen name='modal' options={{ presentation: 'modal', title: 'Modal' }} />
      <Stack.Screen name='settings' options={{ title: 'Configurações' }} />
      <Stack.Screen
        name='create-list'
        options={{
          presentation: 'modal',
          headerShown: false, // Esconde header do React Navigation - vamos criar customizado na tela
        }}
      />
      <Stack.Screen
        name='lists/[id]'
        options={{
          headerShown: false,
        }}
      />
    </Stack>
  );
}

export default function RootLayout() {
  const colorScheme = useColorScheme();

  // Tema customizado sem borda no header
  const customTheme = {
    ...(colorScheme === 'dark' ? DarkTheme : DefaultTheme),
    colors: {
      ...(colorScheme === 'dark' ? DarkTheme.colors : DefaultTheme.colors),
      border: 'transparent', // Remove bordas padrão
    },
    // Remove sombras e bordas de todos os headers por padrão
    headerShadowVisible: false,
  };

  return (
    <ThemeProvider value={customTheme}>
      <AuthProvider>
        <NavigationContent />
        <StatusBar style='auto' />
      </AuthProvider>
    </ThemeProvider>
  );
}
