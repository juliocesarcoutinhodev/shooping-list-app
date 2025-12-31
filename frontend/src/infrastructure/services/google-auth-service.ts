/**
 * Google Auth Service
 * Gerencia o fluxo de autenticação OAuth2 com Google usando Expo Auth Session
 */

import * as AuthSession from 'expo-auth-session';
import * as WebBrowser from 'expo-web-browser';

import { env } from '../config/env';

// Necessário para fechar o browser corretamente após autenticação
WebBrowser.maybeCompleteAuthSession();

export interface GoogleAuthResult {
  idToken: string;
  cancelled: boolean;
  error?: string;
}

export class GoogleAuthService {
  private readonly redirectUri: string;
  private readonly clientId: string;

  constructor() {
    // Deixo o Expo decidir o redirect URI automaticamente
    // Para Expo Go: usa auth.expo.io
    // Para standalone: usa o scheme configurado no app.config.js
    this.redirectUri = AuthSession.makeRedirectUri();

    // Obtenho o Google Client ID do ambiente
    this.clientId = env.googleClientId || '';

    if (!this.clientId) {
      console.warn('GOOGLE_CLIENT_ID não configurado no .env');
    }

    // Log para debug do redirect URI
    console.log('Google OAuth Redirect URI:', this.redirectUri);
    console.log('Google Client ID:', this.clientId ? 'Configurado' : 'Faltando');
  }

  /**
   * Inicia o fluxo de autenticação com Google
   * @returns Promise com idToken ou erro
   */
  async signIn(): Promise<GoogleAuthResult> {
    try {
      // Verifico se o clientId está configurado
      if (!this.clientId) {
        return {
          idToken: '',
          cancelled: false,
          error: 'Google Client ID não configurado. Configure GOOGLE_CLIENT_ID no .env',
        };
      }

      // Configuro o discovery document do Google
      const discovery = {
        authorizationEndpoint: 'https://accounts.google.com/o/oauth2/v2/auth',
        tokenEndpoint: 'https://oauth2.googleapis.com/token',
      };

      // Crio a request de autenticação
      const request = new AuthSession.AuthRequest({
        clientId: this.clientId,
        scopes: ['openid', 'profile', 'email'],
        redirectUri: this.redirectUri,
        responseType: AuthSession.ResponseType.IdToken,
        usePKCE: false, // Google não requer PKCE para id_token
      });

      // Executo o fluxo de autenticação
      const result = await request.promptAsync(discovery);

      // Verifico o resultado
      if (result.type === 'success') {
        const idToken = result.params.id_token;

        if (!idToken) {
          return {
            idToken: '',
            cancelled: false,
            error: 'ID Token não recebido do Google',
          };
        }

        return {
          idToken,
          cancelled: false,
        };
      }

      if (result.type === 'cancel') {
        return {
          idToken: '',
          cancelled: true,
        };
      }

      // Erro ou dismiss
      return {
        idToken: '',
        cancelled: false,
        error: result.type === 'error' ? result.error?.message : 'Autenticação cancelada',
      };
    } catch (error) {
      console.error('Erro no fluxo Google OAuth:', error);
      return {
        idToken: '',
        cancelled: false,
        error:
          error instanceof Error ? error.message : 'Erro desconhecido ao autenticar com Google',
      };
    }
  }

  /**
   * Retorna a URL de redirect configurada (útil para debug)
   */
  getRedirectUri(): string {
    return this.redirectUri;
  }
}

// Singleton instance
export const googleAuthService = new GoogleAuthService();
