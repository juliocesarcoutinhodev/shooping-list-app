/**
 * Presentation Layer - Modal Screen
 *
 * Tela modal simples para demonstração do sistema de tema.
 */

import { useRouter } from 'expo-router';
import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

import { Button } from '../components';
import { useAppTheme } from '../hooks';

export function ModalScreen() {
  const router = useRouter();
  const theme = useAppTheme();

  const handleClose = () => {
    router.dismiss();
  };

  return (
    <View style={[styles.container, { backgroundColor: theme.colors.background }]}>
      <View style={styles.content}>
        <Text style={[styles.title, { color: theme.colors.text }]}>Modal de Exemplo</Text>
        <Text style={[styles.description, { color: theme.colors.textSecondary }]}>
          Esta é uma tela modal usando nosso sistema de Design Tokens.
        </Text>

        <View
          style={[
            styles.themeDemo,
            { backgroundColor: theme.colors.surface, borderColor: theme.colors.border },
          ]}
        >
          <Text style={[styles.themeDemoTitle, { color: theme.colors.text }]}>
            🎨 Design Tokens
          </Text>
          <Text style={[styles.themeDemoText, { color: theme.colors.textTertiary }]}>
            Cores, tipografia e espaçamento centralizados para consistência visual.
          </Text>
        </View>

        <Button title='Fechar' onPress={handleClose} variant='primary' />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  content: {
    alignItems: 'center',
    maxWidth: 320,
    width: '100%',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 16,
    textAlign: 'center',
  },
  description: {
    fontSize: 16,
    textAlign: 'center',
    marginBottom: 32,
    lineHeight: 24,
  },
  themeDemo: {
    padding: 16,
    borderRadius: 12,
    marginBottom: 32,
    width: '100%',
    borderWidth: 1,
  },
  themeDemoTitle: {
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 8,
    textAlign: 'center',
  },
  themeDemoText: {
    fontSize: 14,
    textAlign: 'center',
    lineHeight: 20,
  },
});
