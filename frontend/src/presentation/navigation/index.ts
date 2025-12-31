/**
 * Presentation Layer - Navigation
 *
 * Navigation configuration and routing setup.
 */

export const AppRoutes = {
  Home: '/' as const,
  ShoppingLists: '/shopping-lists' as const,
  ShoppingListDetail: '/shopping-lists/[id]' as const,
  Profile: '/profile' as const,
  Modal: '/modal' as const,
} as const;

export type AppRoute = (typeof AppRoutes)[keyof typeof AppRoutes];

export interface NavigationProps {
  navigation: {
    navigate: (route: string, params?: any) => void;
    goBack: () => void;
    push: (route: string, params?: any) => void;
  };
  route: {
    params?: any;
  };
}
