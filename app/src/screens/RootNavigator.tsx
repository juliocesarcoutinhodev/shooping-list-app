import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { useAuthStore } from '../store/useAuthStore';

import { colors } from '../styles/colors';
import { CreateListScreen } from './CreateListScreen';
import { DashboardScreen } from './DashboardScreen';
import { ForgotPasswordScreen } from './ForgotPasswordScreen';
import { ListDetailsScreen } from './ListDetailsScreen';
import { LoginScreen } from './LoginScreen';
import { SignUpScreen } from './SignUpScreen';
import { SplashScreen } from './SplashScreen';

const Stack = createNativeStackNavigator();

export function RootNavigator() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  return (
    <NavigationContainer>
      <Stack.Navigator
        initialRouteName={isAuthenticated ? 'dashboard' : 'splash'}
        screenOptions={{
          headerShown: false,
          contentStyle: {
            backgroundColor: colors.background,
          },
        }}
      >
        {!isAuthenticated ? (
          <>
            <Stack.Screen
              name="splash"
              component={SplashScreen}
              options={{ animationEnabled: false }}
            />
            <Stack.Screen
              name="login"
              component={LoginScreen}
              options={{
                animationEnabled: true,
              }}
            />
            <Stack.Screen
              name="signup"
              component={SignUpScreen}
              options={{
                animationEnabled: true,
              }}
            />
            <Stack.Screen
              name="forgot-password"
              component={ForgotPasswordScreen}
              options={{
                animationEnabled: true,
              }}
            />
          </>
        ) : (
          <>
            <Stack.Screen
              name="dashboard"
              component={DashboardScreen}
              options={{ animationEnabled: false }}
            />
            <Stack.Screen
              name="create-list"
              component={CreateListScreen}
              options={{
                animationEnabled: true,
              }}
            />
            <Stack.Screen
              name="list-details"
              component={ListDetailsScreen}
              options={{
                animationEnabled: true,
              }}
            />
          </>
        )}
      </Stack.Navigator>
    </NavigationContainer>
  );
}
