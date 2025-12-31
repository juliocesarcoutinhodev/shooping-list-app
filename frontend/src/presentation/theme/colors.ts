/**
 * Design Tokens - Colors
 *
 * Paleta Fresh Market - Verde suave e minimalista
 * Ideal para aplicações de lista de compras e marketplace
 */

// Cores Base (Neutras)
export const baseColors = {
  // Brancos e Pretos
  white: '#FFFFFF',
  black: '#000000',

  // Fresh Market Background
  backgroundLight: '#F9FAF7',

  // Text Colors - Verde bem escuro
  textDark: '#064E3B', // Verde bem escuro para textos principais
  textMuted: '#0F766E', // Verde escuro um pouco mais claro para textos secundários

  // Escala de Cinzas (mantida para compatibilidade)
  gray50: '#F9FAFB',
  gray100: '#F3F4F6',
  gray200: '#E5E7EB',
  gray300: '#D1D5DB',
  gray400: '#9CA3AF',
  gray500: '#6B7280',
  gray600: '#4B5563',
  gray700: '#374151',
  gray800: '#1F2937',
  gray900: '#111827',
};

// Cores Semânticas - Fresh Market
export const semanticColors = {
  // Primary (Verde suave - Fresh Market)
  primary50: '#E8F8F0',
  primary100: '#D1F2E1',
  primary200: '#A3E4C3',
  primary300: '#75D7A5',
  primary400: '#47C987',
  primary500: '#2ECC71', // Primary - Verde suave
  primary600: '#27AE60', // Secondary - Verde mais forte
  primary700: '#229954',
  primary800: '#1D8348',
  primary900: '#186A3B',

  // Success (Verde - usa o primary)
  success50: '#E8F8F0',
  success100: '#D1F2E1',
  success500: '#2ECC71',
  success600: '#27AE60',

  // Warning (Amarelo)
  warning50: '#FFF8E1',
  warning100: '#FFECB3',
  warning500: '#F39C12',
  warning600: '#E67E22',

  // Error (Vermelho Fresh Market)
  error50: '#FDECEA',
  error100: '#FAD9D5',
  error500: '#E74C3C',
  error600: '#C0392B',
};

// Tema Light - Fresh Market
export const lightColors = {
  // Background - Quase branco confortável
  background: baseColors.backgroundLight, // #F9FAF7
  backgroundSecondary: baseColors.white,
  backgroundTertiary: baseColors.gray50,

  // Text - Verde bem escuro
  text: baseColors.textDark, // #064E3B - Verde bem escuro para textos principais
  textSecondary: baseColors.textMuted, // #0F766E - Verde escuro para textos secundários
  textTertiary: baseColors.gray400,
  textInverted: baseColors.white,

  // Primary - Verde Fresh Market
  primary: semanticColors.primary500, // #2ECC71
  primaryHover: semanticColors.primary600, // #27AE60
  primaryActive: semanticColors.primary700,

  // Surface - Branco para cards
  surface: baseColors.white, // #FFFFFF
  surfaceSecondary: baseColors.backgroundLight,

  // Borders
  border: baseColors.gray200,
  borderFocus: semanticColors.primary500,

  // States
  success: semanticColors.success500, // #2ECC71
  warning: semanticColors.warning500,
  error: semanticColors.error500, // #E74C3C

  // Overlay
  overlay: 'rgba(44, 62, 80, 0.5)',
};

// Tema Dark - Fresh Market adaptado
export const darkColors = {
  // Background - Escuro
  background: baseColors.gray900,
  backgroundSecondary: baseColors.gray800,
  backgroundTertiary: baseColors.gray700,

  // Text
  text: baseColors.white,
  textSecondary: baseColors.gray300,
  textTertiary: baseColors.gray400,
  textInverted: baseColors.textDark,

  // Primary - Verde Fresh Market (mais claro no dark)
  primary: semanticColors.primary400,
  primaryHover: semanticColors.primary300,
  primaryActive: semanticColors.primary200,

  // Surface
  surface: baseColors.gray800,
  surfaceSecondary: baseColors.gray700,

  // Borders
  border: baseColors.gray600,
  borderFocus: semanticColors.primary400,

  // States
  success: semanticColors.success500,
  warning: semanticColors.warning500,
  error: semanticColors.error500,

  // Overlay
  overlay: 'rgba(0, 0, 0, 0.7)',
};

export type ColorTokens = typeof lightColors;
