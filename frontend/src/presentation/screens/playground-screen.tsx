/**
 * Presentation Layer - Playground Screen
 *
 * Tela de demonstração de todos os componentes disponíveis.
 */

import React, { useState } from 'react';
import { View, Text, StyleSheet, ScrollView, Alert } from 'react-native';

import { Button, TextField, Card, Divider, Loader } from '../components';
import { useAppTheme } from '../hooks';

export function PlaygroundScreen() {
  const theme = useAppTheme();

  // States para demonstrações interativas
  const [textValue, setTextValue] = useState('');
  const [textWithError, setTextWithError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [showLoader, setShowLoader] = useState(false);

  // Simulação de loading
  const handleLoadingDemo = async () => {
    setIsLoading(true);
    setTimeout(() => {
      setIsLoading(false);
      Alert.alert('Sucesso!', 'Operação concluída');
    }, 2000);
  };

  const handleLoaderToggle = () => {
    setShowLoader(!showLoader);
  };

  return (
    <ScrollView style={[styles.container, { backgroundColor: theme.colors.background }]}>
      <View style={styles.content}>
        <Text style={[styles.title, { color: theme.colors.text }]}>
          🎮 Playground de Componentes
        </Text>
        <Text style={[styles.subtitle, { color: theme.colors.textSecondary }]}>
          Demonstração interativa dos componentes disponíveis
        </Text>

        {/* Buttons Section */}
        <Card>
          <Text style={[styles.sectionTitle, { color: theme.colors.text }]}>🔘 Buttons</Text>

          <View style={styles.buttonRow}>
            <Button
              title='Primary'
              onPress={() => Alert.alert('Button', 'Primary pressed!')}
              size='small'
            />
            <Button
              title='Secondary'
              onPress={() => Alert.alert('Button', 'Secondary pressed!')}
              variant='secondary'
              size='small'
            />
          </View>

          <View style={styles.buttonRow}>
            <Button
              title='Medium'
              onPress={() => Alert.alert('Button', 'Medium pressed!')}
              size='medium'
            />
            <Button
              title='Large'
              onPress={() => Alert.alert('Button', 'Large pressed!')}
              size='large'
            />
          </View>

          <View style={styles.buttonRow}>
            <Button title='Disabled' onPress={() => {}} disabled={true} />
            <Button
              title={isLoading ? 'Loading...' : 'Test Loading'}
              onPress={handleLoadingDemo}
              loading={isLoading}
              variant='primary'
            />
          </View>
        </Card>

        {/* TextFields Section */}
        <Card>
          <Text style={[styles.sectionTitle, { color: theme.colors.text }]}>📝 Text Fields</Text>

          <TextField
            label='Campo Normal'
            placeholder='Digite algo aqui...'
            value={textValue}
            onChangeText={setTextValue}
          />

          <TextField label='Campo Preenchido' placeholder='Variante filled' variant='filled' />

          <TextField
            label='Campo com Erro'
            placeholder='Este campo tem erro'
            value={textWithError}
            onChangeText={setTextWithError}
            error={
              textWithError.length > 0 && textWithError.length < 5
                ? 'Mínimo 5 caracteres'
                : undefined
            }
          />

          <TextField
            label='Campo Desabilitado'
            placeholder='Campo desabilitado'
            value='Texto não editável'
            disabled={true}
          />
        </Card>

        {/* Cards Section */}
        <Card>
          <Text style={[styles.sectionTitle, { color: theme.colors.text }]}>🃏 Cards</Text>

          <Card variant='elevated' onPress={() => Alert.alert('Card', 'Elevated card pressed!')}>
            <Text style={[styles.cardContent, { color: theme.colors.text }]}>
              Card Elevado (com sombra) - Toque para testar
            </Text>
          </Card>

          <Card variant='outlined'>
            <Text style={[styles.cardContent, { color: theme.colors.text }]}>Card com Borda</Text>
          </Card>

          <Card variant='filled'>
            <Text style={[styles.cardContent, { color: theme.colors.text }]}>Card Preenchido</Text>
          </Card>
        </Card>

        {/* Dividers Section */}
        <Card>
          <Text style={[styles.sectionTitle, { color: theme.colors.text }]}>📏 Dividers</Text>

          <Text style={[styles.cardContent, { color: theme.colors.text }]}>
            Texto acima do divider
          </Text>

          <Divider />

          <Text style={[styles.cardContent, { color: theme.colors.text }]}>
            Texto abaixo do divider padrão
          </Text>

          <Divider thickness={2} color={theme.colors.primary} />

          <Text style={[styles.cardContent, { color: theme.colors.text }]}>
            Divider customizado (grosso e colorido)
          </Text>

          <View style={styles.verticalDividerDemo}>
            <Text style={[styles.cardContent, { color: theme.colors.text }]}>Esquerda</Text>
            <Divider orientation='vertical' margin={theme.spacing[2]} />
            <Text style={[styles.cardContent, { color: theme.colors.text }]}>Direita</Text>
          </View>
        </Card>

        {/* Loaders Section */}
        <Card>
          <Text style={[styles.sectionTitle, { color: theme.colors.text }]}>⏳ Loaders</Text>

          <View style={styles.loaderRow}>
            <View style={styles.loaderDemo}>
              <Text style={[styles.loaderLabel, { color: theme.colors.textSecondary }]}>
                Spinner
              </Text>
              <Loader variant='spinner' size='medium' />
            </View>

            <View style={styles.loaderDemo}>
              <Text style={[styles.loaderLabel, { color: theme.colors.textSecondary }]}>Dots</Text>
              <Loader variant='dots' size='medium' />
            </View>

            <View style={styles.loaderDemo}>
              <Text style={[styles.loaderLabel, { color: theme.colors.textSecondary }]}>Pulse</Text>
              <Loader variant='pulse' size='medium' />
            </View>
          </View>

          <View style={styles.loaderSizes}>
            <Loader variant='spinner' size='small' text='Small' />
            <Loader variant='spinner' size='medium' text='Medium' />
            <Loader variant='spinner' size='large' text='Large' />
          </View>

          <Button
            title={showLoader ? 'Ocultar Loader' : 'Mostrar Loader'}
            onPress={handleLoaderToggle}
            variant='secondary'
          />

          {showLoader && <Loader variant='spinner' size='large' text='Carregando dados...' />}
        </Card>

        {/* Theme Demo */}
        <Card>
          <Text style={[styles.sectionTitle, { color: theme.colors.text }]}>🎨 Design Tokens</Text>

          <Text style={[styles.cardContent, { color: theme.colors.text }]}>
            Todos os componentes usam o sistema de Design Tokens:
          </Text>

          <Text style={[styles.tokenList, { color: theme.colors.textTertiary }]}>
            • Cores: {Object.keys(theme.colors).length} tokens{'\n'}• Espaçamento:{' '}
            {Object.keys(theme.spacing).length} tokens{'\n'}• Tipografia:{' '}
            {Object.keys(theme.typography).length} presets{'\n'}• Bordas:{' '}
            {Object.keys(theme.radius).length} variações{'\n'}• Sombras:{' '}
            {Object.keys(theme.shadows).length} níveis
          </Text>
        </Card>

        <View style={styles.footer}>
          <Text style={[styles.footerText, { color: theme.colors.textTertiary }]}>
            Todos os componentes são reutilizáveis e seguem a identidade visual centralizada! 🎯
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
  cardContent: {
    fontSize: 14,
    lineHeight: 20,
  },
  buttonRow: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    marginBottom: 12,
  },
  verticalDividerDemo: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 16,
  },
  loaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    marginBottom: 20,
  },
  loaderDemo: {
    alignItems: 'center',
    flex: 1,
  },
  loaderLabel: {
    fontSize: 12,
    marginBottom: 8,
  },
  loaderSizes: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    marginVertical: 16,
  },
  tokenList: {
    fontSize: 14,
    lineHeight: 20,
    marginTop: 8,
  },
  footer: {
    marginTop: 32,
    padding: 16,
    borderTopWidth: 1,
    borderTopColor: '#E5E5E7',
  },
  footerText: {
    fontSize: 14,
    textAlign: 'center',
    fontStyle: 'italic',
  },
});
