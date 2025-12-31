/**
 * Auth Context
 * Gerencia estado de autenticação do usuário com integração ao backend
 */

import React, { createContext, useContext, useEffect, useState } from 'react';

import { User } from '@/src/domain/entities';
import { authService, googleAuthService } from '@/src/infrastructure/services';

interface AuthContextData {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  signIn: (email: string, password: string) => Promise<void>;
  signUp: (name: string, email: string, password: string) => Promise<void>;
  signInWithGoogle: () => Promise<void>;
  signOut: () => Promise<void>;
}

const AuthContext = createContext<AuthContextData>({} as AuthContextData);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    restoreSession();
  }, []);

  async function restoreSession() {
    try {
      const restoredUser = await authService.restoreSession();
      if (restoredUser) {
        setUser(restoredUser);
        console.log('[AuthContext] Sessão restaurada com sucesso:', restoredUser.email);
      } else {
        console.log('[AuthContext] Nenhuma sessão válida encontrada');
      }
    } catch (error) {
      console.error('[AuthContext] Erro ao restaurar sessão:', error);
      // Limpo estado do usuário em caso de erro
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  }

  async function signIn(email: string, password: string) {
    try {
      const session = await authService.login(email, password);
      setUser(session.user);
      console.log('[AuthContext] Login realizado com sucesso:', session.user.email);
    } catch (error: any) {
      // O erro vem do normalizeError do apiClient, tem estrutura: { message, status, code, data }
      let errorMessage = 'Erro ao fazer login. Tente novamente.';

      if (typeof error === 'object' && error !== null) {
        // Se é um erro normalizado do ApiClient
        if (error.message && error.status !== undefined) {
          errorMessage = error.message;
        }
        // Se é um Error comum
        else if (error.message) {
          errorMessage = error.message;
        }
      } else if (typeof error === 'string') {
        errorMessage = error;
      }

      console.error('[AuthContext] Erro ao fazer login:', errorMessage);

      // Cria um erro com a mensagem apropriada para que a UI possa exibir
      const userError = new Error(errorMessage);
      userError.name = 'AuthenticationError';
      throw userError;
    }
  }

  async function signUp(name: string, email: string, password: string) {
    try {
      const session = await authService.register(name, email, password);
      setUser(session.user);
      console.log('[AuthContext] Registro realizado com sucesso:', session.user.email);
    } catch (error: any) {
      const errorMessage =
        error?.message || error?.data?.message || 'Erro ao fazer registro. Tente novamente.';
      console.error('[AuthContext] Erro ao fazer registro:', errorMessage);

      const userError = new Error(errorMessage);
      userError.name = 'RegistrationError';
      throw userError;
    }
  }

  async function signInWithGoogle() {
    try {
      // Inicio fluxo OAuth2 com Google
      const result = await googleAuthService.signIn();

      // Verifico se o usuário cancelou
      if (result.cancelled) {
        throw new Error('Login cancelado pelo usuário');
      }

      // Verifico se houve erro no OAuth
      if (result.error) {
        throw new Error(result.error);
      }

      // Envio o idToken para o backend
      const session = await authService.loginWithGoogle(result.idToken);
      setUser(session.user);
      console.log('[AuthContext] Login com Google realizado com sucesso:', session.user.email);
    } catch (error: any) {
      const errorMessage =
        error?.message ||
        error?.data?.message ||
        'Erro ao fazer login com Google. Tente novamente.';
      console.error('[AuthContext] Erro ao fazer login com Google:', errorMessage);

      const userError = new Error(errorMessage);
      userError.name = 'GoogleAuthError';
      throw userError;
    }
  }

  async function signOut() {
    try {
      await authService.logout();
      setUser(null);
      console.log('[AuthContext] Logout realizado com sucesso');
    } catch (error) {
      console.error('[AuthContext] Erro ao fazer logout:', error);
      // Limpo usuário mesmo com erro no backend
      setUser(null);
      throw error;
    }
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        isAuthenticated: !!user,
        signIn,
        signUp,
        signInWithGoogle,
        signOut,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth deve ser usado dentro de AuthProvider');
  }
  return context;
}
