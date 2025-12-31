/**
 * Login Screen
 * Tela de autenticação do usuário com validação React Hook Form + Zod
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
const loginSchema = z.object({
  email: z.string().min(1, 'Campo obrigatório'),
  password: z.string().min(1, 'Campo obrigatório'),
});

type LoginFormData = z.infer<typeof loginSchema>;

export function LoginScreen() {
  const theme = useAppTheme();
  const router = useRouter();
  const { signIn, signInWithGoogle } = useAuth();

  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  });

  const onSubmit = async (data: LoginFormData) => {
    setIsLoading(true);
    setErrorMessage('');

    try {
      await signIn(data.email, data.password);
    } catch (error: unknown) {
      // Captura mensagem de erro da API
      const apiError = error as { message?: string; data?: { message?: string } };
      const apiMessage = apiError?.message || apiError?.data?.message;
      setErrorMessage(apiMessage || 'Erro ao fazer login. Tente novamente.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleGoogleLogin = async () => {
    setIsLoading(true);
    setErrorMessage('');

    try {
      await signInWithGoogle();
      // Navegação é feita automaticamente pelo _layout.tsx
    } catch (error: any) {
      // Captura mensagem de erro (Google OAuth ou API)
      const apiMessage = error?.message || error?.data?.message;
      setErrorMessage(apiMessage || 'Erro ao fazer login com Google. Tente novamente.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleGoToRegister = () => {
    router.push('/register' as never);
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
            <Text style={[styles.title, { color: theme.colors.text }]}>Shopping List</Text>
            <Text style={[styles.subtitle, { color: theme.colors.textSecondary }]}>
              Organize suas compras de forma simples
            </Text>
          </View>

          {/* Mensagem de erro global */}
          {errorMessage ? (
            <View style={[styles.errorBanner, { backgroundColor: theme.colors.error + '15' }]}>
              <Text style={[styles.errorBannerText, { color: theme.colors.error }]}>
                {errorMessage}
              </Text>
            </View>
          ) : null}

          {/* Formulário */}
          <View style={styles.form}>
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

            <TouchableOpacity style={styles.forgotPassword} onPress={() => {}} disabled={isLoading}>
              <Text style={[styles.forgotPasswordText, { color: theme.colors.primary }]}>
                Esqueceu a senha?
              </Text>
            </TouchableOpacity>

            <Button
              title='Entrar'
              onPress={handleSubmit(onSubmit)}
              loading={isLoading}
              disabled={isLoading}
              size='large'
            />
          </View>

          {/* Divider com texto */}
          <View style={styles.dividerContainer}>
            <View style={[styles.dividerLine, { backgroundColor: theme.colors.border }]} />
            <Text style={[styles.dividerText, { color: theme.colors.textSecondary }]}>
              ou continue com
            </Text>
            <View style={[styles.dividerLine, { backgroundColor: theme.colors.border }]} />
          </View>

          {/* Botão Google */}
          <TouchableOpacity
            style={[
              styles.googleButton,
              {
                backgroundColor: theme.colors.surface,
                borderColor: theme.colors.border,
              },
            ]}
            onPress={handleGoogleLogin}
            disabled={isLoading}
          >
            <Text style={styles.googleIcon}>G</Text>
            <Text style={[styles.googleButtonText, { color: theme.colors.text }]}>
              {isLoading ? 'Autenticando...' : 'Entrar com Google'}
            </Text>
          </TouchableOpacity>

          {/* Link para registro */}
          <View style={styles.footer}>
            <Text style={[styles.footerText, { color: theme.colors.textSecondary }]}>
              Não tem uma conta?{' '}
            </Text>
            <TouchableOpacity onPress={handleGoToRegister} disabled={isLoading}>
              <Text style={[styles.footerLink, { color: theme.colors.primary }]}>Criar conta</Text>
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
    marginBottom: 40,
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
  errorBannerText: {
    fontSize: 14,
    textAlign: 'center',
    fontWeight: '500',
  },
  form: {
    gap: 16,
  },
  forgotPassword: {
    alignSelf: 'flex-end',
    marginTop: -8,
  },
  forgotPasswordText: {
    fontSize: 14,
    fontWeight: '500',
  },
  dividerContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginVertical: 32,
    gap: 16,
  },
  dividerLine: {
    flex: 1,
    height: 1,
  },
  dividerText: {
    fontSize: 13,
    textTransform: 'lowercase',
  },
  googleButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 14,
    paddingHorizontal: 20,
    borderRadius: 8,
    borderWidth: 1,
    gap: 12,
  },
  googleIcon: {
    fontSize: 20,
    fontWeight: 'bold',
  },
  googleButtonText: {
    fontSize: 16,
    fontWeight: '600',
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
