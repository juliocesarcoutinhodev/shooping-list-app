/**
 * EditItemModal - Modal para editar item existente na lista
 *
 * Modal que aparece de baixo para cima com formulário pré-preenchido para editar item.
 * Validação com RHF + Zod (reaproveitando schema do AddItemModal).
 */

import { Ionicons } from '@expo/vector-icons';
import { zodResolver } from '@hookform/resolvers/zod';
import React, { useEffect, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import {
  Animated,
  Dimensions,
  KeyboardAvoidingView,
  Modal,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  TouchableOpacity,
  TouchableWithoutFeedback,
  View,
  ViewStyle,
} from 'react-native';
import { z } from 'zod';

import { useAppTheme } from '../../hooks';
import { Button, TextField } from '../index';

// Componente para campo de quantidade com estado local
const QuantityField: React.FC<{
  value: number | undefined;
  onChange: (value: number) => void;
  onBlur: () => void;
  error?: string;
  loading?: boolean;
  labelColor?: string;
}> = ({ value, onChange, onBlur, error, loading, labelColor }) => {
  const [displayValue, setDisplayValue] = useState<string>(value?.toString() || '1');
  const [isFocused, setIsFocused] = useState(false);

  useEffect(() => {
    // Só sincroniza o displayValue com o value quando o campo não está focado
    // Isso permite que o usuário apague e digite livremente durante a edição
    if (!isFocused && value !== undefined && value !== null) {
      const newDisplayValue = value.toString();
      if (displayValue !== newDisplayValue) {
        setDisplayValue(newDisplayValue);
      }
    }
  }, [value, isFocused]);

  const handleQuantityChange = (text: string) => {
    const digits = text.replace(/\D/g, '');
    if (digits === '') {
      setDisplayValue('');
      // Não chama onChange quando está vazio, permite que o usuário apague completamente
      return;
    }
    const num = parseFloat(digits);
    if (!isNaN(num) && num > 0) {
      setDisplayValue(digits);
      onChange(num);
    }
  };

  const handleQuantityFocus = () => {
    setIsFocused(true);
  };

  const handleQuantityBlur = () => {
    setIsFocused(false);
    if (displayValue === '' || parseFloat(displayValue) <= 0 || isNaN(parseFloat(displayValue))) {
      setDisplayValue('1');
      onChange(1);
    }
    onBlur();
  };

  return (
    <TextField
      label='Quantidade'
      placeholder='Ex: 2'
      value={displayValue}
      onChangeText={handleQuantityChange}
      onFocus={handleQuantityFocus}
      onBlur={handleQuantityBlur}
      error={error}
      keyboardType='numeric'
      returnKeyType='next'
      disabled={loading}
      labelColor={labelColor}
    />
  );
};

// Componente para campo de preço com estado local
const PriceField: React.FC<{
  value: number | undefined;
  onChange: (value: number | undefined) => void;
  onBlur: () => void;
  error?: string;
  loading?: boolean;
  labelColor?: string;
}> = ({ value, onChange, onBlur, error, loading, labelColor }) => {
  const [displayValue, setDisplayValue] = useState<string>('');

  useEffect(() => {
    if (value === undefined || value === null) {
      if (displayValue !== '') {
        setDisplayValue('');
      }
    } else {
      const formatted = formatForDisplay(value);
      if (displayValue !== formatted) {
        setDisplayValue(formatted);
      }
    }
  }, [value, displayValue]);

  const formatForDisplay = (num: number): string => {
    const parts = num.toFixed(2).split('.');
    const reais = parts[0];
    const centavos = parts[1];
    const reaisFormatados = reais.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
    return `${reaisFormatados},${centavos}`;
  };

  const formatInput = (input: string): string => {
    let digits = input.replace(/\D/g, '');
    if (digits === '') return '';
    digits = digits.replace(/^0+/, '') || '0';
    if (digits.length === 1) {
      return `0,0${digits}`;
    }
    if (digits.length === 2) {
      return `0,${digits}`;
    }
    const reais = digits.slice(0, -2);
    const centavos = digits.slice(-2);
    const reaisFormatados = reais.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
    return `${reaisFormatados},${centavos}`;
  };

  const handlePriceChange = (text: string) => {
    const digits = text.replace(/\D/g, '');
    if (digits === '') {
      setDisplayValue('');
      onChange(undefined);
      return;
    }
    const formatted = formatInput(digits);
    setDisplayValue(formatted);
    const numString = formatted.replace(/\./g, '').replace(',', '.');
    const numValue = parseFloat(numString);
    if (!isNaN(numValue) && numValue >= 0) {
      onChange(numValue);
    }
  };

  const handleFocus = () => {
    if (value !== undefined && value !== null && displayValue === '') {
      setDisplayValue(formatForDisplay(value));
    }
  };

  return (
    <TextField
      label='Preço (R$)'
      placeholder='Ex: 4,99'
      value={displayValue}
      onChangeText={handlePriceChange}
      onFocus={handleFocus}
      onBlur={onBlur}
      error={error}
      keyboardType='numeric'
      returnKeyType='done'
      disabled={loading}
      labelColor={labelColor}
    />
  );
};

// Schema de validação Zod (reaproveitado do AddItemModal)
const editItemSchema = z.object({
  name: z
    .string()
    .min(2, 'Nome deve ter no mínimo 2 caracteres')
    .max(80, 'Nome deve ter no máximo 80 caracteres')
    .trim(),
  quantity: z
    .number()
    .min(1, 'Quantidade deve ser maior ou igual a 1')
    .positive('Quantidade deve ser positiva'),
  unitPrice: z.preprocess(val => {
    if (val === '' || val === undefined || val === null) return undefined;
    if (typeof val === 'string') {
      const cleaned = val.trim().replace(',', '.');
      const num = parseFloat(cleaned);
      return isNaN(num) ? undefined : num;
    }
    return typeof val === 'number' ? val : undefined;
  }, z.number().min(0, 'Preço unitário não pode ser negativo').optional()),
});

type EditItemFormData = {
  name: string;
  quantity: number;
  unitPrice?: number;
};

export interface EditItemModalProps {
  visible: boolean;
  item: {
    id: string;
    name: string;
    quantity: number;
    unitPrice?: number;
  } | null;
  onClose: () => void;
  onSubmit: (data: { name: string; quantity: number; unitPrice?: number }) => Promise<void>;
  loading?: boolean;
  error?: string | null;
}

export const EditItemModal: React.FC<EditItemModalProps> = ({
  visible,
  item,
  onClose,
  onSubmit,
  loading = false,
  error: externalError = null,
}) => {
  const theme = useAppTheme();
  const slideAnim = React.useRef(new Animated.Value(0)).current;
  const screenHeight = Dimensions.get('window').height;

  const {
    control,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<EditItemFormData>({
    resolver: zodResolver(editItemSchema) as any,
    defaultValues: {
      name: '',
      quantity: 1,
      unitPrice: undefined,
    },
  });

  // Pré-preenche o formulário quando o item muda
  useEffect(() => {
    if (item && visible) {
      reset({
        name: item.name,
        quantity: item.quantity,
        unitPrice:
          item.unitPrice !== undefined && item.unitPrice !== null && item.unitPrice > 0
            ? item.unitPrice
            : undefined,
      });
    } else if (!visible) {
      reset();
    }
  }, [item, visible, reset]);

  // Animação de slide up quando o modal aparece
  useEffect(() => {
    if (visible) {
      Animated.spring(slideAnim, {
        toValue: 1,
        useNativeDriver: true,
        tension: 50,
        friction: 10,
      }).start();
    } else {
      Animated.timing(slideAnim, {
        toValue: 0,
        duration: 200,
        useNativeDriver: true,
      }).start();
    }
  }, [visible, slideAnim]);

  const handleFormSubmit = async (data: EditItemFormData) => {
    try {
      await onSubmit({
        name: data.name,
        quantity: data.quantity,
        unitPrice: data.unitPrice,
      });
      // Reset form após sucesso
      reset();
    } catch (error) {
      // Erro é tratado no componente pai
      console.error('Erro ao editar item:', error);
    }
  };

  const translateY = slideAnim.interpolate({
    inputRange: [0, 1],
    outputRange: [screenHeight, 0],
  });

  if (!item) {
    return null;
  }

  return (
    <Modal visible={visible} transparent animationType='none' statusBarTranslucent>
      <TouchableWithoutFeedback onPress={onClose}>
        <View style={styles.overlay}>
          <TouchableWithoutFeedback>
            <Animated.View
              style={[
                styles.modal,
                {
                  backgroundColor: theme.colors.surface,
                  transform: [{ translateY }],
                },
              ]}
            >
              {/* Header fixo - não scrolla */}
              <View style={styles.header}>
                <View style={styles.headerTitleContainer}>
                  <Ionicons
                    name='pencil'
                    size={20}
                    color={theme.colors.text}
                    style={styles.headerIcon}
                  />
                  <Text style={[styles.title, { color: theme.colors.text }]}>Editar Item</Text>
                </View>
                <TouchableOpacity
                  onPress={onClose}
                  hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
                  disabled={loading}
                >
                  <Ionicons name='close' size={24} color={theme.colors.text} />
                </TouchableOpacity>
              </View>

              {/* KeyboardAvoidingView: ajusta o layout quando o teclado aparece
                  - iOS: usa 'padding' para adicionar padding inferior
                  - Android: usa 'height' para ajustar a altura do container */}
              <KeyboardAvoidingView
                behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
                style={styles.keyboardAvoidingView}
                keyboardVerticalOffset={Platform.OS === 'ios' ? 0 : 0}
              >
                {/* ScrollView: permite rolagem quando o teclado cobre o conteúdo
                    - keyboardShouldPersistTaps='handled': permite tocar em botões mesmo com teclado aberto
                    - O ScrollView automaticamente scrolla para o campo focado no iOS */}
                <ScrollView
                  style={styles.scrollView}
                  contentContainerStyle={styles.scrollContent}
                  keyboardShouldPersistTaps='handled'
                  showsVerticalScrollIndicator={false}
                  bounces={false}
                  nestedScrollEnabled={true}
                  keyboardDismissMode='interactive'
                >
                {/* Error Message */}
                {externalError && (
                  <View
                    style={[
                      styles.errorBanner,
                      {
                        // Mantém fundo vermelho sempre para dar destaque ao erro
                        backgroundColor: theme.colors.error,
                      },
                    ]}
                  >
                    <Text style={[styles.errorText, { color: '#FFFFFF' }]}>
                      {externalError}
                    </Text>
                  </View>
                )}

                {/* Form */}
                <View style={styles.form}>
                  <Controller
                    control={control}
                    name='name'
                    render={({ field: { onChange, onBlur, value } }) => (
                      <TextField
                        label='Nome do Item'
                        placeholder='Ex: Arroz, Feijão, Leite...'
                        value={value}
                        onChangeText={onChange}
                        onBlur={onBlur}
                        error={errors.name?.message}
                        autoCapitalize='sentences'
                        returnKeyType='next'
                        autoFocus
                        disabled={loading}
                        labelColor={theme.colors.text}
                      />
                    )}
                  />

                  <Controller
                    control={control}
                    name='quantity'
                    render={({ field: { onChange, onBlur, value } }) => (
                      <QuantityField
                        value={value}
                        onChange={onChange}
                        onBlur={onBlur}
                        error={errors.quantity?.message}
                        loading={loading}
                        labelColor={theme.colors.text}
                      />
                    )}
                  />

                  <Controller
                    control={control}
                    name='unitPrice'
                    render={({ field: { onChange, onBlur, value } }) => (
                      <PriceField
                        value={value}
                        onChange={onChange}
                        onBlur={onBlur}
                        error={errors.unitPrice?.message}
                        loading={loading}
                        labelColor={theme.colors.text}
                      />
                    )}
                  />
                </View>

                  {/* Button */}
                  <Button
                    title='Salvar Alterações'
                    onPress={handleSubmit(handleFormSubmit)}
                    loading={loading}
                    disabled={loading}
                    variant='primary'
                    size='large'
                  />
                </ScrollView>
              </KeyboardAvoidingView>
            </Animated.View>
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
    justifyContent: 'flex-end',
  } as ViewStyle,
  modal: {
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    width: '100%',
    maxHeight: '90%',
    paddingTop: 24,
    // flex: 1 garante que o modal ocupe o espaço disponível
    flex: 1,
  } as ViewStyle,
  keyboardAvoidingView: {
    // flex: 1 permite que o KeyboardAvoidingView funcione corretamente
    // dando espaço para ajustar quando o teclado aparece
    flex: 1,
  } as ViewStyle,
  scrollView: {
    // flex: 1 permite que o ScrollView ocupe todo o espaço disponível
    // removendo maxHeight fixo para permitir ajuste dinâmico
    flex: 1,
  } as ViewStyle,
  scrollContent: {
    paddingHorizontal: 24,
    // paddingBottom aumentado para garantir espaço suficiente quando o teclado está aberto
    // 100px é suficiente para a maioria dos teclados + botão
    paddingBottom: 100,
    // flexGrow permite que o conteúdo cresça se necessário, mas não força altura mínima
    flexGrow: 1,
  } as ViewStyle,
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 24,
    marginBottom: 24,
  } as ViewStyle,
  headerTitleContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  } as ViewStyle,
  headerIcon: {
    marginRight: 4,
  },
  title: {
    fontSize: 22,
    fontWeight: '700',
    letterSpacing: 0.2,
  },
  form: {
    gap: 20,
    marginBottom: 24,
  } as ViewStyle,
  errorBanner: {
    padding: 12,
    borderRadius: 8,
    marginBottom: 16,
  } as ViewStyle,
  errorText: {
    fontSize: 14,
    textAlign: 'center',
    fontWeight: '500',
  },
});

export default EditItemModal;
