import { Pressable, PressableProps, Text } from 'react-native';
import { colors } from '../styles/colors';

interface ButtonProps extends PressableProps {
  variant?: 'primary' | 'outline' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
  children: React.ReactNode;
}

export function Button({
  variant = 'primary',
  size = 'md',
  children,
  style,
  ...props
}: ButtonProps) {
  const isText = typeof children === 'string';

  const heightMap = {
    sm: 40,
    md: 48,
    lg: 56,
  };

  const backgroundColorMap = {
    primary: colors.primary,
    outline: colors.card,
    ghost: 'transparent',
  };

  const textColorMap = {
    primary: colors.primaryForeground,
    outline: colors.foreground,
    ghost: colors.foreground,
  };

  const borderColorMap = {
    primary: colors.primary,
    outline: colors.border,
    ghost: 'transparent',
  };

  return (
    <Pressable
      {...props}
      style={[
        {
          height: heightMap[size],
          paddingHorizontal: 16,
          borderRadius: 12,
          justifyContent: 'center',
          alignItems: 'center',
          backgroundColor: backgroundColorMap[variant],
          borderWidth: variant === 'outline' ? 1 : 0,
          borderColor: borderColorMap[variant],
          opacity: props.disabled ? 0.5 : 1,
        },
        style,
      ]}
    >
      {isText ? (
        <Text
          style={{
            color: textColorMap[variant],
            fontSize: 16,
            fontWeight: '600',
          }}
        >
          {children}
        </Text>
      ) : (
        children
      )}
    </Pressable>
  );
}
