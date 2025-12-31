/**
 * Create List Screen
 * Tela de criação de nova lista de compras com validação RHF + Zod
 */

import { zodResolver } from '@hookform/resolvers/zod';
import { Ionicons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import React, { useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { ScrollView, StyleSheet, Text, TouchableOpacity, View, ViewStyle } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { z } from 'zod';

import { ShoppingListRemoteDataSource } from '@/src/data/data-sources/shopping-list-remote-data-source';
import { ShoppingListRepositoryImpl } from '@/src/data/repositories/shopping-list-repository';
import { CreateListUseCase } from '@/src/domain/use-cases';

import { Button, TextField } from '../components';
import { useAppTheme } from '../hooks';

// Schema de validação Zod
const createListSchema = z.object({
  title: z
    .string()
    .min(3, 'Título deve ter no mínimo 3 caracteres')
    .max(100, 'Título deve ter no máximo 100 caracteres')
    .trim(),
  description: z
    .string()
    .max(255, 'Descrição deve ter no máximo 255 caracteres')
    .optional()
    .or(z.literal('')),
});

type CreateListFormData = z.infer<typeof createListSchema>;

// Instancio use case - idealmente viria de DI/contexto
const remoteDataSource = new ShoppingListRemoteDataSource();
const repository = new ShoppingListRepositoryImpl(remoteDataSource);
const createListUseCase = new CreateListUseCase(repository);

export function CreateListScreen() {
  const theme = useAppTheme();
  const router = useRouter();
  const insets = useSafeAreaInsets();

  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<CreateListFormData>({
    resolver: zodResolver(createListSchema),
    defaultValues: {
      title: '',
      description: '',
    },
  });

  const onSubmit = async (data: CreateListFormData) => {
    setIsLoading(true);
    setErrorMessage('');

    try {
      await createListUseCase.execute({
        title: data.title,
        description: data.description || undefined,
      });

      // Sucesso: volto para o dashboard
      router.back();
    } catch (error: unknown) {
      // Capturo mensagem de erro da API ou do use case
      const apiError = error as { message?: string; data?: { message?: string } };
      const apiMessage = apiError?.message || apiError?.data?.message;
      setErrorMessage(apiMessage || 'Erro ao criar lista. Tente novamente.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <View style={[styles.container, { backgroundColor: theme.colors.background }]}>
      {/* Header Customizado - Responsivo para Android/iOS e diferentes tamanhos */}
      <View
        style={[
          styles.header,
          {
            paddingTop: Math.max(insets.top, 8) + 8, // Garante padding mínimo mesmo sem notch
            backgroundColor: theme.colors.background, // Usa a cor de fundo do tema (adapta automaticamente)
            minHeight: 56, // Altura mínima padrão do Material Design
          },
        ]}
      >
        <View style={styles.headerContent}>
          <TouchableOpacity
            onPress={() => router.back()}
            hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
            accessibilityLabel='Voltar'
            accessibilityRole='button'
          >
            <Ionicons name='arrow-back' size={24} color={theme.colors.text} />
          </TouchableOpacity>

          <View style={styles.headerCenter}>
            <Text
              style={[styles.headerTitle, { color: theme.colors.text }]}
              numberOfLines={1}
              adjustsFontSizeToFit
              minimumFontScale={0.8}
            >
              Nova Lista
            </Text>
          </View>

          <View style={styles.headerSpacer} />
        </View>
      </View>

      <ScrollView
        contentContainerStyle={styles.scrollContent}
        keyboardShouldPersistTaps='handled'
        showsVerticalScrollIndicator={false}
        style={styles.scrollView}
      >
        {errorMessage ? (
          <View style={[styles.errorBanner, { backgroundColor: theme.colors.error }]}>
            <Text style={[styles.errorText, { color: theme.colors.textInverted }]}>
              {errorMessage}
            </Text>
          </View>
        ) : null}

        <View style={styles.form}>
          <Controller
            control={control}
            name='title'
            render={({ field: { onChange, onBlur, value } }) => (
              <TextField
                label='Nome da Lista'
                placeholder='Digite o nome da lista'
                value={value}
                onChangeText={onChange}
                onBlur={onBlur}
                error={errors.title?.message}
                autoCapitalize='sentences'
                returnKeyType='next'
              />
            )}
          />

          <Controller
            control={control}
            name='description'
            render={({ field: { onChange, onBlur, value } }) => (
              <TextField
                label='Descrição (optional)'
                placeholder='Adicione uma descrição para sua lista'
                value={value}
                onChangeText={onChange}
                onBlur={onBlur}
                error={errors.description?.message}
                autoCapitalize='sentences'
                returnKeyType='done'
                multiline
                numberOfLines={5}
              />
            )}
          />
        </View>

        <Button
          title='Criar Lista'
          onPress={handleSubmit(onSubmit)}
          loading={isLoading}
          disabled={isLoading}
          variant='primary'
          size='large'
        />
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  } as ViewStyle,
  header: {
    paddingHorizontal: 20,
    paddingBottom: 16,
    // Divisor bem sutil
    borderBottomWidth: 1,
    borderBottomColor: 'rgba(0,0,0,0.05)',
    // Garante que o header não seja comprimido
    justifyContent: 'flex-end',
  } as ViewStyle,
  headerContent: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 16,
    // Garante alinhamento consistente
    minHeight: 40,
  } as ViewStyle,
  headerCenter: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    // Permite que o texto seja truncado em telas muito pequenas
    minWidth: 0,
  } as ViewStyle,
  headerTitle: {
    fontSize: 20,
    fontWeight: '700',
    letterSpacing: 0.2,
    // Garante que o texto não quebre em telas pequenas
    textAlign: 'center',
  },
  headerSpacer: {
    width: 24,
    // Garante espaço igual ao botão de voltar para centralização perfeita
  } as ViewStyle,
  scrollView: {
    flex: 1,
  },
  scrollContent: {
    padding: 24,
    gap: 24,
  },
  form: {
    gap: 20,
  },
  errorBanner: {
    padding: 12,
    borderRadius: 8,
  },
  errorText: {
    fontSize: 14,
    textAlign: 'center',
    fontWeight: '500',
  },
});
