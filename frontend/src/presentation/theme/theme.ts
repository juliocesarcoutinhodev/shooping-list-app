/**
 * Design Tokens - Theme System
 *
 * Sistema centralizado que combina todos os tokens de design.
 * Ponto único de verdade para cores, tipografia, espaçamento, etc.
 */

import { lightColors, darkColors, type ColorTokens } from './colors';
import {
  spacing,
  radius,
  shadows,
  opacity,
  zIndex,
  type SpacingTokens,
  type RadiusTokens,
  type ShadowTokens,
} from './layout';
import { typography, type TypographyTokens } from './typography';

// Theme Interface
export interface Theme {
  colors: ColorTokens;
  typography: TypographyTokens;
  spacing: SpacingTokens;
  radius: RadiusTokens;
  shadows: ShadowTokens;
  opacity: typeof opacity;
  zIndex: typeof zIndex;
}

// Light Theme
export const lightTheme: Theme = {
  colors: lightColors,
  typography,
  spacing,
  radius,
  shadows,
  opacity,
  zIndex,
};

// Dark Theme
export const darkTheme: Theme = {
  colors: darkColors,
  typography,
  spacing,
  radius,
  shadows,
  opacity,
  zIndex,
};

// Tema padrão (será usado como fallback)
export const defaultTheme = lightTheme;

// Type exports para uso em componentes
export type { ColorTokens, TypographyTokens, SpacingTokens, RadiusTokens, ShadowTokens };

// Utility function para acessar valores aninhados do tema
export const getThemeValue = <T>(theme: Theme, path: string): T => {
  return path.split('.').reduce((obj: unknown, key: string) => {
    return obj && typeof obj === 'object' && key in obj
      ? (obj as Record<string, unknown>)[key]
      : undefined;
  }, theme) as T;
};

// Helper para criar estilos com theme
export const createStyles = <T>(stylesFn: (theme: Theme) => T) => stylesFn;
