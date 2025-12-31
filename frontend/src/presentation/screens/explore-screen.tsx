/**
 * Presentation Layer - Explore Screen
 *
 * Tela para explorar funcionalidades da aplicação.
 */

import React from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';

export function ExploreScreen() {
  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Explorar</Text>
        <Text style={styles.subtitle}>Funcionalidades e configurações</Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>📱 Próximas Funcionalidades</Text>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>🛒 Gerenciar Listas</Text>
          <Text style={styles.cardDescription}>Criar, editar e excluir listas de compras</Text>
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>📝 Itens</Text>
          <Text style={styles.cardDescription}>Adicionar, marcar e remover itens das listas</Text>
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>🔄 Sincronização</Text>
          <Text style={styles.cardDescription}>Sincronizar dados entre dispositivos</Text>
        </View>
      </View>

      <View style={styles.footer}>
        <Text style={styles.footerText}>
          Comece a desenvolver na pasta src/ seguindo a Clean Architecture!
        </Text>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  header: {
    padding: 20,
    paddingTop: 40,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#333333',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: '#666666',
  },
  section: {
    padding: 20,
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: '600',
    color: '#333333',
    marginBottom: 20,
  },
  card: {
    backgroundColor: '#F5F5F5',
    padding: 16,
    borderRadius: 12,
    marginBottom: 12,
  },
  cardTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333333',
    marginBottom: 8,
  },
  cardDescription: {
    fontSize: 14,
    color: '#666666',
    lineHeight: 20,
  },
  footer: {
    padding: 20,
    borderTopWidth: 1,
    borderTopColor: '#E5E5E7',
  },
  footerText: {
    fontSize: 14,
    color: '#666666',
    textAlign: 'center',
    fontStyle: 'italic',
  },
});
