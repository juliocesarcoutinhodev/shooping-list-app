/**
 * ConfirmModal - Modal de confirmação customizado
 *
 * Substitui o Alert nativo com design consistente ao app
 * Usado para ações destrutivas que exigem confirmação
 */

import React from 'react';
import {
  Modal,
  StyleSheet,
  Text,
  TouchableOpacity,
  TouchableWithoutFeedback,
  View,
  ViewStyle,
} from 'react-native';

import { useAppTheme } from '../../hooks';

export interface ConfirmModalProps {
  visible: boolean;
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  confirmVariant?: 'destructive' | 'primary';
  onConfirm: () => void;
  onCancel: () => void;
  loading?: boolean;
}

export const ConfirmModal: React.FC<ConfirmModalProps> = ({
  visible,
  title,
  message,
  confirmText = 'Confirmar',
  cancelText = 'Cancelar',
  confirmVariant = 'primary',
  onConfirm,
  onCancel,
  loading = false,
}) => {
  const theme = useAppTheme();

  return (
    <Modal visible={visible} transparent animationType='fade' statusBarTranslucent>
      <TouchableWithoutFeedback onPress={onCancel}>
        <View style={styles.overlay}>
          <TouchableWithoutFeedback>
            <View
              style={[
                styles.modal,
                {
                  backgroundColor: theme.colors.surface,
                  borderRadius: theme.radius.lg,
                },
              ]}
            >
              {/* Título */}
              <Text
                style={[
                  styles.title,
                  {
                    color: theme.colors.text,
                  },
                ]}
              >
                {title}
              </Text>

              {/* Mensagem */}
              <Text
                style={[
                  styles.message,
                  {
                    color: theme.colors.textSecondary,
                  },
                ]}
              >
                {message}
              </Text>

              {/* Botões */}
              <View style={styles.buttons}>
                {/* Botão Cancelar */}
                <TouchableOpacity
                  style={[
                    styles.button,
                    styles.buttonSecondary,
                    {
                      borderColor: theme.colors.border,
                    },
                  ]}
                  onPress={onCancel}
                  disabled={loading}
                  activeOpacity={0.7}
                >
                  <Text
                    style={[
                      styles.buttonText,
                      {
                        color: theme.colors.text,
                      },
                    ]}
                  >
                    {cancelText}
                  </Text>
                </TouchableOpacity>

                {/* Botão Confirmar */}
                <TouchableOpacity
                  style={[
                    styles.button,
                    styles.buttonPrimary,
                    {
                      backgroundColor:
                        confirmVariant === 'destructive'
                          ? theme.colors.error
                          : theme.colors.primary,
                      opacity: loading ? 0.6 : 1,
                    },
                  ]}
                  onPress={onConfirm}
                  disabled={loading}
                  activeOpacity={0.7}
                >
                  <Text
                    style={[
                      styles.buttonText,
                      styles.buttonTextPrimary,
                      {
                        color: theme.colors.textInverted,
                      },
                    ]}
                  >
                    {loading ? 'Aguarde...' : confirmText}
                  </Text>
                </TouchableOpacity>
              </View>
            </View>
          </TouchableWithoutFeedback>
        </View>
      </TouchableWithoutFeedback>
    </Modal>
  );
};

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  } as ViewStyle,
  modal: {
    width: '100%',
    maxWidth: 400,
    padding: 24,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15,
    shadowRadius: 12,
    elevation: 8,
  } as ViewStyle,
  title: {
    fontSize: 20,
    fontWeight: '700',
    marginBottom: 12,
    textAlign: 'left',
  },
  message: {
    fontSize: 15,
    lineHeight: 22,
    marginBottom: 24,
    textAlign: 'left',
  },
  buttons: {
    flexDirection: 'row',
    gap: 12,
  } as ViewStyle,
  button: {
    flex: 1,
    paddingVertical: 14,
    paddingHorizontal: 20,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
  } as ViewStyle,
  buttonSecondary: {
    borderWidth: 1.5,
    backgroundColor: 'transparent',
  } as ViewStyle,
  buttonPrimary: {
    borderWidth: 0,
  } as ViewStyle,
  buttonText: {
    fontSize: 16,
    fontWeight: '600',
  },
  buttonTextPrimary: {
    // Cor definida via prop theme
  },
});

export default ConfirmModal;
