import { TextInput, TextInputProps } from 'react-native';
import { colors } from '../styles/colors';

interface InputProps extends TextInputProps {
  variant?: 'default' | 'outline';
}

export function Input({ variant = 'default', style, ...props }: InputProps) {
  return (
    <TextInput
      {...props}
      placeholderTextColor={colors.mutedForeground}
      style={[
        {
          height: 56,
          paddingHorizontal: 16,
          paddingVertical: 12,
          borderRadius: 12,
          fontSize: 16,
          color: colors.foreground,
          backgroundColor: colors.card,
          borderWidth: 1,
          borderColor: colors.border,
        },
        style,
      ]}
    />
  );
}
