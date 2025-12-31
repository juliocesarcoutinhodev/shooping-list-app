import { Text, View } from 'react-native';
import { colors } from '../styles/colors';

interface CardProps {
  children: React.ReactNode;
  style?: any;
}

export function Card({ children, style }: CardProps) {
  return (
    <View
      style={[
        {
          backgroundColor: colors.card,
          borderRadius: 16,
          borderWidth: 1,
          borderColor: colors.border,
          padding: 16,
        },
        style,
      ]}
    >
      {children}
    </View>
  );
}

interface CardHeaderProps {
  children: React.ReactNode;
  style?: any;
}

export function CardHeader({ children, style }: CardHeaderProps) {
  return (
    <View style={[{ marginBottom: 16 }, style]}>{children}</View>
  );
}

interface CardTitleProps {
  children: React.ReactNode;
  style?: any;
}

export function CardTitle({ children, style }: CardTitleProps) {
  return (
    <Text
      style={[
        {
          fontSize: 18,
          fontWeight: '600',
          color: colors.foreground,
        },
        style,
      ]}
    >
      {children}
    </Text>
  );
}

interface CardDescriptionProps {
  children: React.ReactNode;
  style?: any;
}

export function CardDescription({
  children,
  style,
}: CardDescriptionProps) {
  return (
    <Text
      style={[
        {
          fontSize: 14,
          color: colors.mutedForeground,
          marginTop: 4,
        },
        style,
      ]}
    >
      {children}
    </Text>
  );
}

interface CardContentProps {
  children: React.ReactNode;
  style?: any;
}

export function CardContent({ children, style }: CardContentProps) {
  return <View style={style}>{children}</View>;
}
