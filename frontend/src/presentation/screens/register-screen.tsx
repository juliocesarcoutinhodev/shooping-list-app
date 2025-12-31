/**
 * Register Screen
 * Tela de cadastro com validação forte e UX consistente
 */

import { zodResolver } from '@hookform/resolvers/zod';
import { useRouter } from 'expo-router';
import React, { useState } from 'react';
import { useForm, Controller } from 'react-hook-form';
import {
  View,
  Text,
  StyleSheet,
  KeyboardAvoidingView,
  Platform,
  TouchableOpacity,
  ScrollView,
} from 'react-native';
import { z } from 'zod';

import { Button, TextField } from '../components';
import { useAuth } from '../contexts/auth-context';
import { useAppTheme } from '../hooks';

// Validação apenas para UX básica (campos vazios)
const registerSchema = z.object({
  name: z.string().min(1, 'Campo obrigatório'),
  email: z.string().min(1, 'Campo obrigatório'),
  password: z.string().min(1, 'Campo obrigatório'),
  confirmPassword: z.string().min(1, 'Campo obrigatório'),
});

type RegisterFormData = z.infer<typeof registerSchema>;

export function RegisterScreen() {
  const theme = useAppTheme();
  const router = useRouter();
  const { signUp } = useAuth();

  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      name: '',
      email: '',
      password: '',
      confirmPassword: '',
    },
  });

  const onSubmit = async (data: RegisterFormData) => {
    setIsLoading(true);
    setErrorMessage('');
    setSuccessMessage('');

    // Validação de UX: senhas devem conferir (antes de enviar para API)
    if (data.password !== data.confirmPassword) {
      setErrorMessage('As senhas não conferem');
      setIsLoading(false);
      return;
    }

    try {
      await signUp(data.name, data.email, data.password);
      setSuccessMessage('Conta criada com sucesso! Redirecionando...');

      // Pequeno delay para mostrar mensagem de sucesso antes de navegar
      setTimeout(() => {
        // Navegação automática será feita pelo AuthContext
      }, 1500);
    } catch (error: any) {
      // Captura mensagem de erro da API
      const apiMessage = error?.message || error?.data?.message;
      setErrorMessage(apiMessage || 'Erro ao criar conta. Tente novamente.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleGoToLogin = () => {
    router.back();
  };

  return (
    <KeyboardAvoidingView
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      style={[styles.container, { backgroundColor: theme.colors.background }]}
    >
      <ScrollView contentContainerStyle={styles.scrollContent} keyboardShouldPersistTaps='handled'>
        <View style={styles.content}>
          {/* Header com ícone */}
          <View style={styles.header}>
            <View style={[styles.iconContainer, { backgroundColor: theme.colors.primary + '15' }]}>
              <Text style={styles.icon}>🛒</Text>
            </View>
            <Text style={[styles.title, { color: theme.colors.text }]}>Criar Conta</Text>
            <Text style={[styles.subtitle, { color: theme.colors.textSecondary }]}>
              Comece a organizar suas compras hoje
            </Text>
          </View>

          {/* Banner de erro */}
          {errorMessage ? (
            <View style={[styles.errorBanner, { backgroundColor: theme.colors.error + '15' }]}>
              <Text style={[styles.bannerText, { color: theme.colors.error }]}>{errorMessage}</Text>
            </View>
          ) : null}

          {/* Banner de sucesso */}
          {successMessage ? (
            <View style={[styles.successBanner, { backgroundColor: theme.colors.success + '15' }]}>
              <Text style={[styles.bannerText, { color: theme.colors.success }]}>
                ✓ {successMessage}
              </Text>
            </View>
          ) : null}

          {/* Formulário */}
          <View style={styles.form}>
            <Controller
              control={control}
              name='name'
              render={({ field: { onChange, onBlur, value } }) => (
                <TextField
                  label='Nome completo'
                  placeholder='João Silva'
                  value={value}
                  onChangeText={onChange}
                  onBlur={onBlur}
                  error={errors.name?.message}
                  autoCapitalize='words'
                  disabled={isLoading}
                />
              )}
            />

            <Controller
              control={control}
              name='email'
              render={({ field: { onChange, onBlur, value } }) => (
                <TextField
                  label='Email'
                  placeholder='seu@email.com'
                  value={value}
                  onChangeText={onChange}
                  onBlur={onBlur}
                  error={errors.email?.message}
                  keyboardType='email-address'
                  autoCapitalize='none'
                  autoCorrect={false}
                  disabled={isLoading}
                />
              )}
            />

            <Controller
              control={control}
              name='password'
              render={({ field: { onChange, onBlur, value } }) => (
                <TextField
                  label='Senha'
                  placeholder='••••••••'
                  value={value}
                  onChangeText={onChange}
                  onBlur={onBlur}
                  error={errors.password?.message}
                  secureTextEntry
                  disabled={isLoading}
                />
              )}
            />

            <Controller
              control={control}
              name='confirmPassword'
              render={({ field: { onChange, onBlur, value } }) => (
                <TextField
                  label='Confirmar senha'
                  placeholder='••••••••'
                  value={value}
                  onChangeText={onChange}
                  onBlur={onBlur}
                  error={errors.confirmPassword?.message}
                  secureTextEntry
                  disabled={isLoading}
                />
              )}
            />

            {/* Dicas de senha forte */}
            <View style={styles.passwordHints}>
              <Text style={[styles.passwordHintsTitle, { color: theme.colors.textSecondary }]}>
                Sua senha deve conter:
              </Text>
              <Text style={[styles.passwordHint, { color: theme.colors.textTertiary }]}>
                • Mínimo 8 caracteres
              </Text>
              <Text style={[styles.passwordHint, { color: theme.colors.textTertiary }]}>
                • Pelo menos uma letra maiúscula
              </Text>
              <Text style={[styles.passwordHint, { color: theme.colors.textTertiary }]}>
                • Pelo menos um número
              </Text>
              <Text style={[styles.passwordHint, { color: theme.colors.textTertiary }]}>
                • Pelo menos um caractere especial (!@#$%)
              </Text>
            </View>

            <Button
              title='Criar Conta'
              onPress={handleSubmit(onSubmit)}
              loading={isLoading}
              disabled={isLoading}
              size='large'
            />
          </View>

          {/* Footer */}
          <View style={styles.footer}>
            <Text style={[styles.footerText, { color: theme.colors.textSecondary }]}>
              Já tem uma conta?{' '}
            </Text>
            <TouchableOpacity onPress={handleGoToLogin} disabled={isLoading}>
              <Text style={[styles.footerLink, { color: theme.colors.primary }]}>Fazer login</Text>
            </TouchableOpacity>
          </View>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  scrollContent: {
    flexGrow: 1,
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    padding: 24,
    paddingTop: 60,
  },
  header: {
    alignItems: 'center',
    marginBottom: 32,
  },
  iconContainer: {
    width: 80,
    height: 80,
    borderRadius: 40,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 16,
  },
  icon: {
    fontSize: 40,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 15,
    textAlign: 'center',
    lineHeight: 22,
  },
  errorBanner: {
    padding: 12,
    borderRadius: 8,
    marginBottom: 20,
  },
  successBanner: {
    padding: 12,
    borderRadius: 8,
    marginBottom: 20,
  },
  bannerText: {
    fontSize: 14,
    textAlign: 'center',
    fontWeight: '500',
  },
  form: {
    gap: 16,
  },
  passwordHints: {
    backgroundColor: 'rgba(46, 204, 113, 0.08)',
    padding: 12,
    borderRadius: 8,
    marginTop: -8,
  },
  passwordHintsTitle: {
    fontSize: 13,
    fontWeight: '600',
    marginBottom: 6,
  },
  passwordHint: {
    fontSize: 12,
    lineHeight: 18,
  },
  footer: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 24,
  },
  footerText: {
    fontSize: 14,
  },
  footerLink: {
    fontSize: 14,
    fontWeight: '600',
  },
});
