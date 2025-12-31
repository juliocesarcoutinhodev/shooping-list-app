import { Text } from 'react-native';
import { colors } from '../styles/colors';

interface LabelProps {
  children: React.ReactNode;
  htmlFor?: string;
  style?: any;
}

export function Label({ children, htmlFor, style }: LabelProps) {
  return (
    <Text
      style={[
        {
          fontSize: 14,
          fontWeight: '500',
          color: colors.foreground,
          marginBottom: 8,
        },
        style,
      ]}
    >
      {children}
    </Text>
  );
}
