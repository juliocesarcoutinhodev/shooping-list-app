import { useNavigation } from '@react-navigation/native';
import { ArrowLeft, Mail } from 'lucide-react-native';
import { useState } from 'react';
import { ScrollView, Text, TouchableOpacity, View } from 'react-native';
import { Button } from '../components/Button';
import { Input } from '../components/Input';
import { Label } from '../components/Label';
import { colors } from '../styles/colors';

export function ForgotPasswordScreen() {
  const navigation = useNavigation();
  const [email, setEmail] = useState('');

  const handleSubmit = () => {
    // Handle password reset
    alert('Link de redefinição enviado para ' + email);
    navigation.goBack();
  };

  return (
    <ScrollView
      style={{
        flex: 1,
        backgroundColor: colors.background,
      }}
      contentContainerStyle={{ flexGrow: 1 }}
    >
      <View
        style={{
          flex: 1,
          paddingHorizontal: 24,
          paddingVertical: 24,
          justifyContent: 'center',
          alignItems: 'center',
        }}
      >
        {/* Back Button */}
        <TouchableOpacity
          onPress={() => navigation.goBack()}
          style={{
            flexDirection: 'row',
            alignItems: 'center',
            marginBottom: 32,
            width: 'fit-content',
          }}
        >
          <ArrowLeft size={20} color={colors.mutedForeground} />
          <Text
            style={{
              marginLeft: 8,
              fontSize: 14,
              color: colors.mutedForeground,
            }}
          >
            Voltar para login
          </Text>
        </TouchableOpacity>

        {/* Header */}
        <View style={{ marginBottom: 48, width: '100%' }}>
          <Text
            style={{
              fontSize: 32,
              fontWeight: '700',
              color: colors.foreground,
              marginBottom: 8,
              textAlign: 'center',
            }}
          >
            Redefinir senha
          </Text>
          <Text
            style={{
              fontSize: 14,
              color: colors.mutedForeground,
              lineHeight: 20,
              textAlign: 'center',
            }}
          >
            Digite seu email e enviaremos um link para redefinir sua senha
          </Text>
        </View>

        {/* Form */}
        <View style={{ gap: 20, width: '100%' }}>
          {/* Email */}
          <View>
            <Label>Email</Label>
            <View style={{ flexDirection: 'row', alignItems: 'center', position: 'relative' }}>
              <Mail
                size={20}
                color={colors.mutedForeground}
                style={{ position: 'absolute', left: 16, zIndex: 10 }}
              />
              <Input
                placeholder="Digite seu email"
                value={email}
                onChangeText={setEmail}
                keyboardType="email-address"
                style={{
                  paddingLeft: 48,
                  flex: 1,
                }}
              />
            </View>
          </View>

          {/* Submit Button */}
          <Button onPress={handleSubmit}>Enviar link de redefinição</Button>
        </View>
      </View>
    </ScrollView>
  );
}
