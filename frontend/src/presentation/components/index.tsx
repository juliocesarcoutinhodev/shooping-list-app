/**
 * Presentation Layer - UI Components
 *
 * Componentes básicos reutilizáveis para a aplicação.
 */

import React, { useState } from 'react';
import {
  ActivityIndicator,
  StyleSheet,
  Text,
  TextInput,
  TextInputProps,
  TouchableOpacity,
  View,
} from 'react-native';

import { useAppTheme } from '../hooks';

// Export custom components
export { AddItemModal } from './add-item-modal';
export type { AddItemModalProps } from './add-item-modal';
export { ConfirmModal } from './confirm-modal';
export { EditItemModal } from './edit-item-modal';
export type { EditItemModalProps } from './edit-item-modal';
export { FloatingActionButton } from './fab';
export type { FloatingActionButtonProps } from './fab';
export { ShoppingItemRow } from './shopping-item-row';
export type { ShoppingItemRowProps } from './shopping-item-row';
export { Toast } from './toast';

// Button Component (melhorado com loading state)
export interface ButtonProps {
  title: string;
  onPress: () => void;
  variant?: 'primary' | 'secondary';
  disabled?: boolean;
  loading?: boolean;
  size?: 'small' | 'medium' | 'large';
}

export function Button({
  title,
  onPress,
  variant = 'primary',
  disabled = false,
  loading = false,
  size = 'medium',
}: ButtonProps) {
  const theme = useAppTheme();

  const getButtonStyle = () => {
    const sizeStyles = {
      small: { paddingHorizontal: 12, paddingVertical: 8 },
      medium: { paddingHorizontal: 20, paddingVertical: 12 },
      large: { paddingHorizontal: 24, paddingVertical: 16 },
    };

    const baseStyle = {
      ...sizeStyles[size],
      borderRadius: theme.radius.base,
      alignItems: 'center' as const,
      justifyContent: 'center' as const,
    };

    if (disabled || loading) {
      return {
        ...baseStyle,
        backgroundColor: theme.colors.textTertiary,
        opacity: theme.opacity[60],
      };
    }

    return variant === 'primary'
      ? {
          ...baseStyle,
          backgroundColor: '#059669',
        }
      : {
          ...baseStyle,
          backgroundColor: 'transparent',
          borderWidth: 1,
          borderColor: '#059669',
        };
  };

  const getTextStyle = () => {
    const sizeStyles = {
      small: { fontSize: 14 },
      medium: { fontSize: 16 },
      large: { fontSize: 18 },
    };

    // No modo dark, textInverted pode ser escuro, então usamos branco diretamente para botões
    const textColor = variant === 'primary' ? '#FFFFFF' : '#059669';
    
    if (disabled || loading) return { ...sizeStyles[size], color: '#FFFFFF' };
    return { ...sizeStyles[size], color: textColor };
  };

  return (
    <TouchableOpacity
      style={getButtonStyle()}
      onPress={onPress}
      disabled={disabled || loading}
      activeOpacity={0.8}
    >
      {loading ? (
        <ActivityIndicator
          size='small'
          color={variant === 'primary' ? '#FFFFFF' : '#059669'}
        />
      ) : (
        <Text style={[{ fontWeight: '600' }, getTextStyle()]}>{title}</Text>
      )}
    </TouchableOpacity>
  );
}

// TextField Component
export interface TextFieldProps extends Omit<TextInputProps, 'style'> {
  label?: string;
  error?: string;
  disabled?: boolean;
  variant?: 'outlined' | 'filled';
  labelColor?: string; // Cor customizada para o label
}

export function TextField({
  label,
  error,
  disabled = false,
  variant = 'outlined',
  labelColor,
  ...textInputProps
}: TextFieldProps) {
  const theme = useAppTheme();
  const [isFocused, setIsFocused] = useState(false);

  // Extrai onFocus e onBlur dos props para combiná-los com os handlers internos
  const { onFocus: externalOnFocus, onBlur: externalOnBlur, ...restTextInputProps } = textInputProps;

  const getContainerStyle = () => {
    const baseStyle = {
      borderRadius: theme.radius.base,
      paddingHorizontal: theme.spacing[4],
      paddingVertical: theme.spacing[3],
      minHeight: 48,
    };

    if (variant === 'filled') {
      return {
        ...baseStyle,
        backgroundColor: theme.colors.backgroundSecondary,
        borderWidth: 0,
      };
    }

    // Outlined variant
    const borderColor = error
      ? theme.colors.error
      : isFocused
        ? theme.colors.borderFocus
        : theme.colors.border;

    return {
      ...baseStyle,
      backgroundColor: disabled ? theme.colors.backgroundSecondary : theme.colors.background,
      borderWidth: 1,
      borderColor,
    };
  };

  const handleFocus = (e: any) => {
    setIsFocused(true);
    if (externalOnFocus) {
      externalOnFocus(e);
    }
  };

  const handleBlur = (e: any) => {
    setIsFocused(false);
    if (externalOnBlur) {
      externalOnBlur(e);
    }
  };

  return (
    <View style={styles.textFieldContainer}>
      {label && (
        <Text
          style={[
            styles.textFieldLabel,
            {
              color: error ? theme.colors.error : labelColor ? labelColor : theme.colors.text,
              marginBottom: theme.spacing[2],
            },
          ]}
        >
          {label}
        </Text>
      )}

      <TextInput
        style={[
          getContainerStyle(),
          {
            color: disabled ? theme.colors.textTertiary : theme.colors.text,
            fontFamily: theme.typography.body.fontFamily,
            fontSize: theme.typography.body.fontSize,
            lineHeight: theme.typography.body.lineHeight,
          },
        ]}
        placeholderTextColor={theme.colors.textTertiary}
        editable={!disabled}
        onFocus={handleFocus}
        onBlur={handleBlur}
        {...restTextInputProps}
      />

      {error && (
        <Text
          style={[
            styles.textFieldError,
            {
              color: theme.colors.error,
              marginTop: theme.spacing[1],
            },
          ]}
        >
          {error}
        </Text>
      )}
    </View>
  );
}

// Card Component (já existente, mantido)
export interface CardProps {
  children: React.ReactNode;
  onPress?: () => void;
  variant?: 'elevated' | 'outlined' | 'filled';
}

export function Card({ children, onPress, variant = 'elevated' }: CardProps) {
  const theme = useAppTheme();
  const Component = onPress ? TouchableOpacity : View;

  const getCardStyle = () => {
    const baseStyle = {
      borderRadius: theme.radius.md,
      padding: theme.spacing[4],
      marginBottom: theme.spacing[3],
    };

    switch (variant) {
      case 'outlined':
        return {
          ...baseStyle,
          backgroundColor: theme.colors.background,
          borderWidth: 1,
          borderColor: theme.colors.border,
        };
      case 'filled':
        return {
          ...baseStyle,
          backgroundColor: theme.colors.backgroundSecondary,
        };
      default: // elevated
        return {
          ...baseStyle,
          backgroundColor: theme.colors.surface,
          ...theme.shadows.base,
        };
    }
  };

  return (
    <Component style={getCardStyle()} onPress={onPress} activeOpacity={onPress ? 0.8 : 1}>
      {children}
    </Component>
  );
}

// Divider Component
export interface DividerProps {
  orientation?: 'horizontal' | 'vertical';
  thickness?: number;
  color?: string;
  margin?: number;
}

export function Divider({
  orientation = 'horizontal',
  thickness = 1,
  color,
  margin,
}: DividerProps) {
  const theme = useAppTheme();
  const dividerColor = color || theme.colors.border;
  const dividerMargin = margin ?? theme.spacing[4];

  const style =
    orientation === 'horizontal'
      ? {
          height: thickness,
          backgroundColor: dividerColor,
          marginVertical: dividerMargin,
        }
      : {
          width: thickness,
          backgroundColor: dividerColor,
          marginHorizontal: dividerMargin,
          alignSelf: 'stretch' as const,
        };

  return <View style={style} />;
}

// Loader Component (variações do loading spinner)
export interface LoaderProps {
  variant?: 'spinner' | 'dots' | 'pulse';
  size?: 'small' | 'medium' | 'large';
  color?: string;
  text?: string;
}

export function Loader({ variant = 'spinner', size = 'medium', color, text }: LoaderProps) {
  const theme = useAppTheme();
  const loaderColor = color || theme.colors.primary;

  const getSizeValue = () => {
    switch (size) {
      case 'small':
        return 20;
      case 'large':
        return 40;
      default:
        return 30;
    }
  };

  const renderLoader = () => {
    switch (variant) {
      case 'dots':
        return (
          <View style={styles.dotsContainer}>
            {[0, 1, 2].map(index => (
              <View
                key={index}
                style={[
                  styles.dot,
                  {
                    backgroundColor: loaderColor,
                    width: getSizeValue() / 3,
                    height: getSizeValue() / 3,
                  },
                ]}
              />
            ))}
          </View>
        );
      case 'pulse':
        return (
          <View
            style={[
              styles.pulse,
              {
                backgroundColor: loaderColor,
                width: getSizeValue(),
                height: getSizeValue(),
                borderRadius: getSizeValue() / 2,
              },
            ]}
          />
        );
      default:
        return <ActivityIndicator size={size === 'medium' ? 'large' : size} color={loaderColor} />;
    }
  };

  return (
    <View style={styles.loaderContainer}>
      {renderLoader()}
      {text && (
        <Text
          style={[
            styles.loaderText,
            {
              color: theme.colors.textSecondary,
              marginTop: theme.spacing[2],
            },
          ]}
        >
          {text}
        </Text>
      )}
    </View>
  );
}

// Loading Spinner (mantido para compatibilidade)
export interface LoadingSpinnerProps {
  size?: 'small' | 'large';
  color?: string;
}

export function LoadingSpinner({ size = 'large', color }: LoadingSpinnerProps) {
  const theme = useAppTheme();
  const spinnerColor = color || theme.colors.primary;

  return (
    <View style={[styles.loadingContainer, { backgroundColor: theme.colors.background }]}>
      <ActivityIndicator size={size} color={spinnerColor} />
    </View>
  );
}

// Error Message (mantido)
export interface ErrorMessageProps {
  message: string;
  onRetry?: () => void;
}

export function ErrorMessage({ message, onRetry }: ErrorMessageProps) {
  const theme = useAppTheme();

  return (
    <View style={[styles.errorContainer, { backgroundColor: theme.colors.background }]}>
      <Text style={[styles.errorText, { color: theme.colors.error }]}>{message}</Text>
      {onRetry && <Button title='Tentar Novamente' onPress={onRetry} variant='secondary' />}
    </View>
  );
}

const styles = StyleSheet.create({
  // TextField Styles
  textFieldContainer: {
    marginBottom: 16,
  },
  textFieldLabel: {
    fontSize: 14,
    fontWeight: '500',
  },
  textFieldError: {
    fontSize: 12,
  },

  // Card Styles (simplificado, estilos dinâmicos aplicados via theme)

  // Loader Styles
  loaderContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    padding: 20,
  },
  loaderText: {
    fontSize: 14,
    textAlign: 'center',
  },
  dotsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    width: 40,
  },
  dot: {
    borderRadius: 10,
  },
  pulse: {
    // Animation would be added here with Animated API
  },

  // Loading Styles (mantido)
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },

  // Error Styles (mantido)
  errorContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  errorText: {
    fontSize: 16,
    textAlign: 'center',
    marginBottom: 20,
  },
});
