/**
 * Presentation Layer - Custom Hooks
 *
 * Hooks personalizados para lógica de UI.
 */

import { useColorScheme } from 'react-native';

import { lightTheme, darkTheme, type Theme } from '../theme';

// Hook para tema da aplicação (atualizado para usar design tokens)
export function useAppTheme(): Theme {
  const colorScheme = useColorScheme();
  const isDark = colorScheme === 'dark';

  return isDark ? darkTheme : lightTheme;
}

// Legacy interface para compatibilidade (deprecated)
/** @deprecated Use useAppTheme() directly for full theme access */
export interface UseThemeResult {
  isDark: boolean;
  colors: {
    background: string;
    text: string;
    primary: string;
    secondary: string;
    card: string;
    border: string;
  };
}

// Legacy hook para compatibilidade (deprecated)
/** @deprecated Use useAppTheme() instead */
export function useLegacyTheme(): UseThemeResult {
  const theme = useAppTheme();

  return {
    isDark: theme === darkTheme,
    colors: {
      background: theme.colors.background,
      text: theme.colors.text,
      primary: theme.colors.primary,
      secondary: theme.colors.textSecondary,
      card: theme.colors.surface,
      border: theme.colors.border,
    },
  };
}
