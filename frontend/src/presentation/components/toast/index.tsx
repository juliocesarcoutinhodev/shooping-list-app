/**
 * Toast - Feedback não bloqueante
 *
 * Componente de notificação temporária que aparece e desaparece automaticamente
 * Usado para feedback de sucesso/erro sem interromper o fluxo do usuário
 */

import React, { useEffect } from 'react';
import { Animated, StyleSheet, Text, ViewStyle } from 'react-native';

import { useAppTheme } from '../../hooks';

export interface ToastProps {
  visible: boolean;
  message: string;
  type?: 'success' | 'error' | 'info';
  duration?: number;
  onHide?: () => void;
  position?: 'top' | 'bottom';
}

export const Toast: React.FC<ToastProps> = ({
  visible,
  message,
  type = 'success',
  duration = 3000,
  onHide,
  position = 'bottom',
}) => {
  const theme = useAppTheme();
  const translateY = React.useRef(new Animated.Value(100)).current;
  const opacity = React.useRef(new Animated.Value(0)).current;

  const hideToast = React.useCallback(() => {
    Animated.parallel([
      Animated.timing(translateY, {
        toValue: position === 'bottom' ? 100 : -100,
        duration: 250,
        useNativeDriver: true,
      }),
      Animated.timing(opacity, {
        toValue: 0,
        duration: 250,
        useNativeDriver: true,
      }),
    ]).start(() => {
      onHide?.();
    });
  }, [position, translateY, opacity, onHide]);

  useEffect(() => {
    if (visible) {
      // Animação de entrada
      Animated.parallel([
        Animated.timing(translateY, {
          toValue: 0,
          duration: 300,
          useNativeDriver: true,
        }),
        Animated.timing(opacity, {
          toValue: 1,
          duration: 300,
          useNativeDriver: true,
        }),
      ]).start();

      // Auto-esconder após duration
      const timer = setTimeout(() => {
        hideToast();
      }, duration);

      return () => clearTimeout(timer);
    } else {
      // Reset position when hidden
      translateY.setValue(position === 'bottom' ? 100 : -100);
      opacity.setValue(0);
    }
  }, [visible, duration, position, translateY, opacity, hideToast]);

  if (!visible) return null;

  const getBackgroundColor = () => {
    switch (type) {
      case 'success':
        return '#059669';
      case 'error':
        return theme.colors.error;
      case 'info':
        return theme.colors.primary;
      default:
        return '#059669';
    }
  };

  return (
    <Animated.View
      style={[
        styles.container,
        position === 'top' ? styles.positionTop : styles.positionBottom,
        {
          backgroundColor: getBackgroundColor(),
          transform: [{ translateY }],
          opacity,
          borderRadius: theme.radius.base,
        },
      ]}
    >
      <Text
        style={[
          styles.message,
          {
            color: theme.colors.textInverted,
          },
        ]}
      >
        {message}
      </Text>
    </Animated.View>
  );
};

const styles = StyleSheet.create({
  container: {
    position: 'absolute',
    left: 20,
    right: 20,
    paddingVertical: 16,
    paddingHorizontal: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.2,
    shadowRadius: 8,
    elevation: 6,
    zIndex: 9999,
  } as ViewStyle,
  positionTop: {
    top: 60,
  } as ViewStyle,
  positionBottom: {
    bottom: 40,
  } as ViewStyle,
  message: {
    fontSize: 15,
    fontWeight: '600',
    textAlign: 'center',
    lineHeight: 20,
  },
});

export default Toast;
