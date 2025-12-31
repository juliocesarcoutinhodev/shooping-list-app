import { useNavigation } from '@react-navigation/native';
import { Lock, Mail, User } from 'lucide-react-native';
import { useState } from 'react';
import { ScrollView, Text, TouchableOpacity, View } from 'react-native';
import { Button } from '../components/Button';
import { Input } from '../components/Input';
import { Label } from '../components/Label';
import { useAuthStore } from '../store/useAuthStore';
import { colors } from '../styles/colors';

export function SignUpScreen() {
  const navigation = useNavigation();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const signup = useAuthStore((state) => state.signup);

  const handleSubmit = async () => {
    if (password !== confirmPassword) {
      alert('Senhas não coincidem');
      return;
    }
    try {
      await signup(name, email, password);
      navigation.reset({
        index: 0,
        routes: [{ name: 'dashboard' as never }],
      });
    } catch (error) {
      console.error('SignUp failed:', error);
    }
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
            Criar Conta
          </Text>
          <Text
            style={{
              fontSize: 14,
              color: colors.mutedForeground,
              lineHeight: 20,
              textAlign: 'center',
            }}
          >
            Comece a usar a Lista de Compras
          </Text>
        </View>

        {/* Form */}
        <View style={{ gap: 20, width: '100%' }}>
          {/* Name */}
          <View>
            <Label>Nome</Label>
            <View style={{ flexDirection: 'row', alignItems: 'center', position: 'relative' }}>
              <User
                size={20}
                color={colors.mutedForeground}
                style={{ position: 'absolute', left: 16, zIndex: 10 }}
              />
              <Input
                placeholder="Digite seu nome"
                value={name}
                onChangeText={setName}
                style={{
                  paddingLeft: 48,
                  flex: 1,
                }}
              />
            </View>
          </View>

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

          {/* Password */}
          <View>
            <Label>Senha</Label>
            <View style={{ flexDirection: 'row', alignItems: 'center', position: 'relative' }}>
              <Lock
                size={20}
                color={colors.mutedForeground}
                style={{ position: 'absolute', left: 16, zIndex: 10 }}
              />
              <Input
                placeholder="Crie uma senha"
                value={password}
                onChangeText={setPassword}
                secureTextEntry
                style={{
                  paddingLeft: 48,
                  flex: 1,
                }}
              />
            </View>
          </View>

          {/* Confirm Password */}
          <View>
            <Label>Confirmar Senha</Label>
            <View style={{ flexDirection: 'row', alignItems: 'center', position: 'relative' }}>
              <Lock
                size={20}
                color={colors.mutedForeground}
                style={{ position: 'absolute', left: 16, zIndex: 10 }}
              />
              <Input
                placeholder="Confirme sua senha"
                value={confirmPassword}
                onChangeText={setConfirmPassword}
                secureTextEntry
                style={{
                  paddingLeft: 48,
                  flex: 1,
                }}
              />
            </View>
          </View>

          {/* Submit Button */}
          <Button onPress={handleSubmit}>Criar Conta</Button>
        </View>

        {/* Login Link */}
        <View
          style={{
            marginTop: 32,
            alignItems: 'center',
            gap: 8,
            flexDirection: 'row',
            justifyContent: 'center',
            width: '100%',
          }}
        >
          <Text
            style={{
              fontSize: 14,
              color: colors.mutedForeground,
            }}
          >
            Já tem uma conta?
          </Text>
          <TouchableOpacity onPress={() => navigation.navigate('login' as never)}>
            <Text
              style={{
                fontSize: 14,
                color: colors.primary,
                fontWeight: '600',
              }}
            >
              Entrar
            </Text>
          </TouchableOpacity>
        </View>
      </View>
    </ScrollView>
  );
}
