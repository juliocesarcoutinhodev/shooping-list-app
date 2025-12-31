import { create } from 'zustand';
import { User } from '../types';

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  signup: (name: string, email: string, password: string) => Promise<void>;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: false,
  login: async (email: string, password: string) => {
    // Simular login
    set({
      user: {
        id: '1',
        name: 'João Developer',
        email: email,
      },
      isAuthenticated: true,
    });
  },
  signup: async (name: string, email: string, password: string) => {
    // Simular signup
    set({
      user: {
        id: '1',
        name: name,
        email: email,
      },
      isAuthenticated: true,
    });
  },
  logout: () => {
    set({
      user: null,
      isAuthenticated: false,
    });
  },
}));
