/**
 * Design Tokens - Spacing & Layout
 *
 * Sistema de espaçamento baseado em múltiplos de 4px para consistência.
 * Escala: 4px, 8px, 12px, 16px, 20px, 24px, 32px, 40px, 48px, 64px, 80px, 96px
 */

// Base unit: 4px
const BASE_UNIT = 4;

// Spacing Scale (múltiplos de 4px)
export const spacing = {
  0: 0,
  1: BASE_UNIT * 1, // 4px
  2: BASE_UNIT * 2, // 8px
  3: BASE_UNIT * 3, // 12px
  4: BASE_UNIT * 4, // 16px (base)
  5: BASE_UNIT * 5, // 20px
  6: BASE_UNIT * 6, // 24px
  8: BASE_UNIT * 8, // 32px
  10: BASE_UNIT * 10, // 40px
  12: BASE_UNIT * 12, // 48px
  16: BASE_UNIT * 16, // 64px
  20: BASE_UNIT * 20, // 80px
  24: BASE_UNIT * 24, // 96px
  32: BASE_UNIT * 32, // 128px
};

// Border Radius (cantos arredondados)
export const radius = {
  none: 0,
  sm: 4,
  base: 8, // Padrão
  md: 12,
  lg: 16,
  xl: 20,
  '2xl': 24,
  '3xl': 32,
  full: 9999, // Círculo
};

// Shadows (sombras para depth)
export const shadows = {
  none: {
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0,
    shadowRadius: 0,
    elevation: 0,
  },
  sm: {
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 1,
  },
  base: {
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  md: {
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15,
    shadowRadius: 8,
    elevation: 4,
  },
  lg: {
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.15,
    shadowRadius: 16,
    elevation: 8,
  },
  xl: {
    shadowOffset: { width: 0, height: 12 },
    shadowOpacity: 0.2,
    shadowRadius: 24,
    elevation: 12,
  },
};

// Opacity Scale
export const opacity = {
  0: 0,
  5: 0.05,
  10: 0.1,
  20: 0.2,
  25: 0.25,
  30: 0.3,
  40: 0.4,
  50: 0.5,
  60: 0.6,
  70: 0.7,
  75: 0.75,
  80: 0.8,
  90: 0.9,
  95: 0.95,
  100: 1,
};

// Z-Index Scale (para camadas de elementos)
export const zIndex = {
  hide: -1,
  base: 0,
  raised: 10,
  dropdown: 1000,
  modal: 1300,
  popover: 1400,
  tooltip: 1500,
  toast: 1600,
  max: 9999,
};

// Layout Breakpoints (para responsividade futura)
export const breakpoints = {
  sm: 640,
  md: 768,
  lg: 1024,
  xl: 1280,
  '2xl': 1536,
};

export type SpacingTokens = typeof spacing;
export type RadiusTokens = typeof radius;
export type ShadowTokens = typeof shadows;
