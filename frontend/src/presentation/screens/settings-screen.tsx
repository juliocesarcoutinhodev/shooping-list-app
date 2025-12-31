/**
 * Settings Screen
 * Exibe configurações de ambiente e informações da app
 */

import React from 'react';
import { View, Text, StyleSheet, ScrollView } from 'react-native';

import { Card, Divider, useAppTheme } from '@/src';
import { env, isDevelopment, isProduction } from '@/src/infrastructure/config/env';

export function SettingsScreen() {
  const theme = useAppTheme();

  const envItems = [
    { label: 'API URL', value: env.apiUrl },
    { label: 'API Timeout', value: `${env.apiTimeout}ms` },
    { label: 'App Name', value: env.appName },
    { label: 'Environment', value: env.appEnv },
    { label: 'Mock API', value: env.enableMockApi ? 'Enabled' : 'Disabled' },
    { label: 'Debug Logs', value: env.enableDebugLogs ? 'Enabled' : 'Disabled' },
  ];

  const statusItems = [
    { label: 'Development Mode', value: isDevelopment ? 'Yes' : 'No' },
    { label: 'Production Mode', value: isProduction ? 'Yes' : 'No' },
  ];

  return (
    <ScrollView style={[styles.container, { backgroundColor: theme.colors.background }]}>
      <View style={styles.content}>
        <Text style={[styles.title, { color: theme.colors.text }]}>⚙️ Configurações</Text>
        <Text style={[styles.subtitle, { color: theme.colors.textSecondary }]}>
          Variáveis de ambiente e configurações da aplicação
        </Text>

        <Card>
          <Text style={[styles.sectionTitle, { color: theme.colors.text }]}>
            🌍 Environment Variables
          </Text>
          {envItems.map((item, index) => (
            <View key={index}>
              <View style={styles.configRow}>
                <Text style={[styles.configLabel, { color: theme.colors.textSecondary }]}>
                  {item.label}:
                </Text>
                <Text
                  style={[styles.configValue, { color: theme.colors.text }]}
                  numberOfLines={1}
                  ellipsizeMode='middle'
                >
                  {item.value}
                </Text>
              </View>
              {index < envItems.length - 1 && <Divider margin={8} />}
            </View>
          ))}
        </Card>

        <Card>
          <Text style={[styles.sectionTitle, { color: theme.colors.text }]}>📊 Status</Text>
          {statusItems.map((item, index) => (
            <View key={index}>
              <View style={styles.configRow}>
                <Text style={[styles.configLabel, { color: theme.colors.textSecondary }]}>
                  {item.label}:
                </Text>
                <Text style={[styles.configValue, { color: theme.colors.text }]}>{item.value}</Text>
              </View>
              {index < statusItems.length - 1 && <Divider margin={8} />}
            </View>
          ))}
        </Card>

        <Card variant='filled'>
          <Text style={[styles.infoTitle, { color: theme.colors.text }]}>💡 Como configurar</Text>
          <Text style={[styles.infoText, { color: theme.colors.textTertiary }]}>
            1. Copie .env.example para .env{'\n'}
            2. Edite as variáveis no arquivo .env{'\n'}
            3. Reinicie o servidor com npm start{'\n'}
            4. As novas configurações serão carregadas
          </Text>
        </Card>

        <View style={styles.warning}>
          <Text style={[styles.warningText, { color: theme.colors.error }]}>
            ⚠️ O arquivo .env não deve ser commitado no git
          </Text>
        </View>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    padding: 16,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    textAlign: 'center',
    marginBottom: 24,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    marginBottom: 16,
  },
  configRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 4,
  },
  configLabel: {
    fontSize: 14,
    fontWeight: '500',
    flex: 1,
  },
  configValue: {
    fontSize: 14,
    flex: 2,
    textAlign: 'right',
  },
  infoTitle: {
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 8,
  },
  infoText: {
    fontSize: 14,
    lineHeight: 20,
  },
  warning: {
    marginTop: 16,
    padding: 12,
    borderRadius: 8,
    backgroundColor: '#FEE2E2',
  },
  warningText: {
    fontSize: 14,
    textAlign: 'center',
  },
});
