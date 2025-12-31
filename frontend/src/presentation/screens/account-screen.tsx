/**
 * Account Screen
 * Exibe dados do usuário autenticado com design melhorado
 */

import React, { useEffect, useState } from 'react';
import { ActivityIndicator, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useColorScheme } from 'react-native';

import { User } from '@/src/domain/entities';
import { userService } from '@/src/infrastructure/services';

import { Button } from '../components';
import { useAuth } from '../contexts/auth-context';
import { useAppTheme } from '../hooks';

export function AccountScreen() {
  const theme = useAppTheme();
  const { signOut } = useAuth();
  const colorScheme = useColorScheme();
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    loadUserData();
  }, []);

  const loadUserData = async () => {
    try {
      setIsLoading(true);
      setErrorMessage('');
      const userData = await userService.getMe();
      setUser(userData);
    } catch (error: any) {
      const errorMsg = error?.message || 'Erro ao carregar dados do usuário';
      setErrorMessage(errorMsg);
      console.error('[AccountScreen] Erro ao carregar usuário:', errorMsg);
    } finally {
      setIsLoading(false);
    }
  };

  const _getProviderIcon = (provider: 'LOCAL' | 'GOOGLE'): string => {
    return provider === 'GOOGLE' ? '🔐' : '📧';
  };

  const getProviderLabel = (provider: 'LOCAL' | 'GOOGLE'): string => {
    return provider === 'GOOGLE' ? 'Google' : 'Email/Senha';
  };

  if (isLoading) {
    return (
      <View style={[styles.container, { backgroundColor: theme.colors.background }]}>
        <View style={styles.centerContainer}>
          <ActivityIndicator size='large' color={theme.colors.primary} />
          <Text style={[styles.loadingText, { color: theme.colors.textSecondary }]}>
            Carregando dados...
          </Text>
        </View>
      </View>
    );
  }

  if (errorMessage) {
    return (
      <View style={[styles.container, { backgroundColor: theme.colors.background }]}>
        <View style={styles.centerContainer}>
          <View
            style={[
              styles.errorCard,
              { backgroundColor: theme.colors.error + '15', borderColor: theme.colors.error },
            ]}
          >
            <Text style={styles.errorIcon}>⚠️</Text>
            <Text style={[styles.errorText, { color: theme.colors.error }]}>{errorMessage}</Text>
            <Button
              title='Tentar Novamente'
              onPress={loadUserData}
              variant='primary'
              size='medium'
            />
          </View>
        </View>
      </View>
    );
  }

  if (!user) return null;

  return (
    <View style={[styles.container, { backgroundColor: theme.colors.background }]}>
      <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={styles.scrollContent}>
        {/* Hero Section - Avatar + Nome */}
        <View style={styles.heroSection}>
          <View
            style={[
              styles.avatar,
              {
                backgroundColor: theme.colors.primary + '20',
                borderColor: theme.colors.primary,
              },
            ]}
          >
            <Text style={styles.avatarText}>
              {user.name
                .split(' ')
                .map(n => n[0])
                .slice(0, 2)
                .join('')
                .toUpperCase()}
            </Text>
          </View>
          <Text style={[styles.userName, { color: theme.colors.text }]}>{user.name}</Text>
        </View>

        {/* Informações do Perfil - Layout Clean */}
        <View style={styles.infoSection}>
          {/* Email */}
          <View style={styles.infoRow}>
            <Text style={[styles.infoLabel, { color: theme.colors.textSecondary }]}>Email</Text>
            <Text style={[styles.infoValue, { color: theme.colors.text }]}>{user.email}</Text>
          </View>

          {/* Autenticação */}
          <View style={styles.infoRow}>
            <Text style={[styles.infoLabel, { color: theme.colors.textSecondary }]}>
              Autenticação
            </Text>
            <Text style={[styles.infoValue, { color: theme.colors.text }]}>
              {getProviderLabel(user.provider)}
            </Text>
          </View>

          {/* Status */}
          {user.status && (
            <View style={styles.infoRow}>
              <Text style={[styles.infoLabel, { color: theme.colors.textSecondary }]}>Status</Text>
              <Text style={[styles.infoValue, { color: theme.colors.success }]}>
                {user.status === 'ACTIVE' ? 'Ativo' : 'Inativo'}
              </Text>
            </View>
          )}

          {/* Membro desde */}
          {user.createdAt && (
            <View style={styles.infoRow}>
              <Text style={[styles.infoLabel, { color: theme.colors.textSecondary }]}>
                Membro desde
              </Text>
              <Text style={[styles.infoValue, { color: theme.colors.text }]}>
                {new Date(user.createdAt).toLocaleDateString('pt-BR', {
                  day: 'numeric',
                  month: 'long',
                  year: 'numeric',
                })}
              </Text>
            </View>
          )}
        </View>

        {/* Actions - Botões mais discretos */}
        <View style={styles.actions}>
          <Button
            title='Recarregar Dados'
            onPress={loadUserData}
            variant='secondary'
            size='medium'
          />
          <Button
            title='Sair'
            onPress={async () => {
              try {
                await signOut();
              } catch (error) {
                console.error('Erro ao sair:', error);
              }
            }}
            variant='secondary'
            size='medium'
          />
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  scrollContent: {
    paddingHorizontal: 24,
    paddingTop: 72,
    paddingBottom: 48,
  },
  centerContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  heroSection: {
    alignItems: 'center',
    marginBottom: 48,
  },
  avatar: {
    width: 96,
    height: 96,
    borderRadius: 48,
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 3,
    marginBottom: 16,
  },
  avatarText: {
    fontSize: 36,
    fontWeight: '700',
    color: '#2ECC71', // Verde como estava antes
  },
  userName: {
    fontSize: 24,
    fontWeight: '600',
    textAlign: 'center',
    letterSpacing: 0.2,
  },
  infoSection: {
    gap: 24,
    marginBottom: 48,
  },
  infoRow: {
    gap: 6,
  },
  infoLabel: {
    fontSize: 13,
    fontWeight: '500',
    letterSpacing: 0.2,
  },
  infoValue: {
    fontSize: 17,
    fontWeight: '500',
    lineHeight: 24,
  },
  errorCard: {
    borderRadius: 16,
    padding: 24,
    borderWidth: 1,
    alignItems: 'center',
    gap: 16,
  },
  errorIcon: {
    fontSize: 48,
  },
  errorText: {
    fontSize: 16,
    fontWeight: '500',
    textAlign: 'center',
    lineHeight: 22,
  },
  loadingText: {
    fontSize: 15,
    marginTop: 16,
    textAlign: 'center',
    fontWeight: '500',
  },
  actions: {
    gap: 12,
  },
});
