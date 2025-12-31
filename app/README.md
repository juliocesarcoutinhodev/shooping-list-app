# 🛒 Shopping List App - React Native (Expo)

Uma aplicação móvel completa de **Lista de Compras** construída com **React Native**, **Expo** e **TypeScript**. Versão nativa do projeto web, com arquitetura moderna, navegação robusta e state management com Zustand.

---

## 📱 Sobre o Projeto

Aplicativo mobile para gerenciar listas de compras com interface intuitiva, totalmente responsiva e otimizada para dispositivos móveis. Permite:

- ✅ **Autenticação**: Login, cadastro e recuperação de senha
- ✅ **Dashboard**: Visualizar todas as listas com progresso
- ✅ **Gerenciamento de Listas**: Criar, editar e excluir listas de compras
- ✅ **Gestão de Itens**: Adicionar, editar, deletar e marcar itens como comprados
- ✅ **Cálculos**: Total automatizado (quantidade × preço) por lista
- ✅ **Persistência**: Dados em memória com Zustand (pronto para AsyncStorage)

**Idioma:** Português Brasileiro (pt-BR)  
**Plataformas:** iOS, Android, Web (via Expo)  
**Status:** ✅ Funcional e pronto para desenvolvimento

---

## 🚀 Início Rápido

### Pré-requisitos
- **Node.js** 16+ com npm/yarn
- **Expo CLI**: `npm install -g expo-cli`
- **Expo Go** app (iOS: App Store | Android: Google Play)

### Instalação & Execução

```bash
# 1. Instalar dependências
npm install

# 2. Iniciar servidor Expo
npm start

# 3. No terminal, escolha:
#    - Pressione 'i' para iOS
#    - Pressione 'a' para Android
#    - Pressione 'w' para Web
#    - Escaneie QR code com Expo Go
```

### Comandos Disponíveis

```bash
# Iniciar desenvolvimento (hot reload)
npm start

# Rodar Android (requer Android Studio)
npm run android

# Rodar iOS (macOS apenas)
npm run ios

# Rodar na web
npm run web

# Executar TypeScript check
npm run type-check
```

---

## 🛠️ Stack Tecnológico

### Core Runtime
- **React Native** 0.81.5 - Framework mobile cross-platform
- **Expo** 54.0.30 - Plataforma de desenvolvimento
- **TypeScript** 5.9.2 - Type safety e IntelliSense
- **React Navigation** 7.x - Navegação nativa (NativeStackNavigator)
- **Zustand** 5.0.9 - State management minimalista

### UI, Styling & Icons
- **React Native Built-in** - Components nativos (View, Text, ScrollView, FlatList, Modal, Pressable, etc.)
- **Lucide React Native** 0.562.0 - 562+ ícones SVG otimizados
- **Componentes Customizados** - Button, Input, Card, Label com design system próprio
- **SafeAreaContext** - Suporte a notch/safe areas do dispositivo

### Dependências Opcionais Instaladas
- **react-native-gesture-handler** - Suporte a gestos avançados
- **react-native-reanimated** - Animações de performance
- **expo-font** - Carregamento de fontes
- **expo-splash-screen** - Tela de splash customizável
- **expo-status-bar** - Controle de barra de status

---

## 📁 Estrutura do Projeto Detalhada

```
app-mobile/
├── App.tsx                                  # Componente raiz com GestureHandlerRootView
├── index.js                                 # Entry point com registerRootComponent
├── app.json                                 # Configuração Expo (sem Expo Router)
├── package.json                             # Dependências e scripts
├── tsconfig.json                            # Configuração TypeScript
├── README.md                                # Este arquivo
│
├── src/
│   ├── screens/                             # 7 telas principais
│   │   ├── RootNavigator.tsx                # Stack Navigator condicional (auth)
│   │   ├── SplashScreen.tsx                 # Tela inicial com auto-redirect
│   │   ├── LoginScreen.tsx                  # Login (email + senha)
│   │   ├── SignUpScreen.tsx                 # Cadastro (name, email, senha, confirm)
│   │   ├── ForgotPasswordScreen.tsx         # Recuperação de senha
│   │   ├── DashboardScreen.tsx              # Listagem com 3 listas + FAB
│   │   ├── CreateListScreen.tsx             # Formulário criar lista
│   │   └── ListDetailsScreen.tsx            # Gerenciar 8 itens de exemplo
│   │
│   ├── components/                          # Componentes reutilizáveis
│   │   ├── Button.tsx                       # Pressable customizado (3 variants + 3 sizes)
│   │   ├── Input.tsx                        # TextInput customizado (border + styling)
│   │   ├── Card.tsx                         # Container customizado (6 sub-components)
│   │   ├── Label.tsx                        # Text label para forms
│   │   └── index.ts                         # Barrel export
│   │
│   ├── store/                               # Zustand stores
│   │   ├── useAuthStore.ts                  # Auth state + login/signup/logout
│   │   ├── useListStore.ts                  # Lists + items CRUD + sample data
│   │   └── [sample data: 3 lists, 8 items]
│   │
│   ├── types/                               # TypeScript types
│   │   └── index.ts                         # ListItem, ShoppingList, User
│   │
│   └── styles/                              # Tema global
│       └── colors.ts                        # Paleta verde customizada
│
├── assets/
│   └── images/                              # Imagens (vazio por padrão)
│
└── constants/
    └── theme.ts                             # Tema Expo (não utilizado)
```

---

## 🗺️ Navegação & Fluxo de Telas

### Arquitetura de Navegação

Utiliza **React Navigation 7.x** com **NativeStackNavigator** e roteamento condicional baseado em autenticação:

```
NOT AUTHENTICATED (isAuthenticated = false)
└── Splash Stack
    ├── SplashScreen (2.5s auto-redirect)
    └── Login Stack
        ├── LoginScreen ────────────┐
        ├── SignUpScreen ───────────┤
        └── ForgotPasswordScreen ───┴──→ [login() success] → AUTHENTICATED

AUTHENTICATED (isAuthenticated = true)
└── App Stack
    ├── DashboardScreen ──────────┐
    ├── CreateListScreen ─────────┤
    └── ListDetailsScreen ────────┘
         [logout()] → NOT AUTHENTICATED
```

### Detalhes de Cada Tela

#### 1️⃣ SplashScreen
- **Função**: Tela inicial com ícone ShoppingCart
- **Comportamento**: 2.5 segundos → auto-redirect para LoginScreen
- **UI**: Fundo verde primary (#059669), ícone centralizado
- **Arquivo**: [src/screens/SplashScreen.tsx](src/screens/SplashScreen.tsx)

#### 2️⃣ LoginScreen
- **Função**: Login com email e senha
- **Componentes**: 
  - Header centralizado "Bem-vindo" + subtitle
  - Input para email (ícone Mail)
  - Input para senha (ícone Lock)
  - Button "Entrar"
  - Links: "Esqueceu a senha?" + "Criar conta"
- **Ação**: Chama `useAuthStore.login()` e navega para Dashboard
- **Arquivo**: [src/screens/LoginScreen.tsx](src/screens/LoginScreen.tsx)

#### 3️⃣ SignUpScreen
- **Função**: Registro de novo usuário
- **Componentes**:
  - Header "Criar Conta"
  - 4 Inputs: Nome, Email, Senha, Confirmar Senha
  - Validação: senha === confirmPassword
  - Button "Criar Conta"
  - Link para voltar ao login
- **Ação**: Chama `useAuthStore.signup()` e navega para Dashboard
- **Arquivo**: [src/screens/SignUpScreen.tsx](src/screens/SignUpScreen.tsx)

#### 4️⃣ ForgotPasswordScreen
- **Função**: Recuperação de senha
- **Componentes**:
  - Header "Recuperar Senha"
  - Input para email
  - Button "Enviar"
  - Back button para LoginScreen
- **Ação**: Simula envio de link de reset
- **Arquivo**: [src/screens/ForgotPasswordScreen.tsx](src/screens/ForgotPasswordScreen.tsx)

#### 5️⃣ DashboardScreen
- **Função**: Página principal com lista de compras
- **Componentes**:
  - SafeAreaView com FlatList (3 listas de exemplo)
  - Cada Card mostra: nome, progresso (visual bar), items count
  - Floating Action Button (FAB) com ícone Plus
  - Logout button (ícone LogOut)
- **Sample Data**:
  - "Compras do Supermercado" (8/12 itens)
  - "Compras Semanais" (3/8 itens)
  - "Compras para Festa" (15/15 itens)
- **Ações**: 
  - Tap card → navega para ListDetailsScreen
  - FAB → navega para CreateListScreen
  - Logout → chama `useAuthStore.logout()`
- **Arquivo**: [src/screens/DashboardScreen.tsx](src/screens/DashboardScreen.tsx)

#### 6️⃣ CreateListScreen
- **Função**: Criar nova lista
- **Componentes**:
  - Header "Criar Lista"
  - Input para nome da lista
  - Input para descrição
  - Button "Criar"
  - Back button
- **Ação**: Chama `useListStore.addList()` e volta para Dashboard
- **Arquivo**: [src/screens/CreateListScreen.tsx](src/screens/CreateListScreen.tsx)

#### 7️⃣ ListDetailsScreen (MAIS COMPLEXA)
- **Função**: Gerenciar itens de uma lista
- **Componentes**:
  - Header com nome da lista
  - FlatList com 8 itens de exemplo
  - Cada item tem: checkbox, nome, quantidade, preço, delete button
  - Total calculado em tempo real (sum de preço × quantidade)
  - FAB para adicionar novo item
- **Sample Items**:
  ```
  1. Maçã (10 un) R$3.50 → R$35.00
  2. Arroz (5 kg) R$22.00 → R$110.00
  3. Feijão (3 kg) R$8.50 → R$25.50
  4. Leite (6 lit) R$4.80 → R$28.80
  5. Pão (2 un) R$5.00 → R$10.00
  6. Frango (2 kg) R$18.00 → R$36.00
  7. Cenoura (1 kg) R$2.50 → R$2.50
  8. Tomate (1 kg) R$4.00 → R$4.00
  Total: R$ 251.80 (7 itens comprados, 1 pendente)
  ```
- **Modais**:
  - Add Item Modal: TextInput para nome
  - Edit Item Modal: Inputs para nome, quantidade, preço (cálculo automático)
- **Ações**:
  - Tap checkbox → `toggleItemComplete()`
  - Tap item → `updateItem()` (modal edit)
  - Swipe/delete → `deleteItem()`
  - FAB → abre Add Item Modal
- **Arquivo**: [src/screens/ListDetailsScreen.tsx](src/screens/ListDetailsScreen.tsx)

---

## 🎨 Design System & Tema

### Paleta de Cores (Verde #059669)

| Cor | Hex | Uso |
|-----|-----|-----|
| **Primary** | `#059669` | Botões, headers, ícones ativos |
| **Secondary** | `#10B981` | Acentos, highlights |
| **Background** | `#F0FDF4` | Fundo geral das telas |
| **Foreground** | `#064E3B` | Texto principal |
| **MutedForeground** | `#6B7280` | Texto secundário, subtitles |
| **Card** | `#ffffff` | Fundo de cards, inputs |
| **Border** | `#A7F3D0` | Borders, separators |
| **Accent** | `#A7F3D0` | Hover states |
| **Destructive** | `#DC2626` | Delete buttons, warnings |

**Arquivo**: [src/styles/colors.ts](src/styles/colors.ts)

### Tipografia

- **Headers**: fontSize 32, fontWeight '700'
- **Subtítulos**: fontSize 14, fontWeight '500'
- **Body Text**: fontSize 14, fontWeight '400'
- **Button Text**: fontSize 16, fontWeight '600'
- **Input Labels**: fontSize 14, fontWeight '500'

### Componentes UI

#### Button.tsx
```tsx
<Button 
  variant="primary"  // "primary" | "outline" | "ghost"
  size="md"          // "sm" (40px) | "md" (48px) | "lg" (56px)
  onPress={handler}
>
  Texto
</Button>
```

#### Input.tsx
```tsx
<Input
  placeholder="Email"
  value={email}
  onChangeText={setEmail}
  editable={true}
  backgroundColor="card"  // Sempre branco com border verde
/>
```

#### Card.tsx
```tsx
<Card>
  <CardHeader>
    <CardTitle>Título</CardTitle>
    <CardDescription>Descrição</CardDescription>
  </CardHeader>
  <CardContent>Conteúdo</CardContent>
</Card>
```

#### Label.tsx
```tsx
<Label>Label do Input</Label>
```

---

## ✨ Funcionalidades Implementadas

### ✅ Autenticação & Navegação
- [x] Tela SplashScreen com auto-redirect (2.5s)
- [x] Sistema de login com email/senha
- [x] Cadastro com validação de senha
- [x] Recuperação de senha
- [x] React Navigation com roteamento condicional (auth-based)
- [x] Logout e reset de sessão

### ✅ Gestão de Listas
- [x] Dashboard com listagem de 3 listas de exemplo
- [x] Cards com progresso visual (progress bar)
- [x] Criar nova lista com nome e descrição
- [x] Editar lista
- [x] Deletar lista
- [x] Navegar para detalhes da lista

### ✅ Gestão de Itens
- [x] Adicionar item na lista
- [x] Editar item (nome, quantidade, preço)
- [x] Deletar item
- [x] Marcar item como comprado (checkbox)
- [x] Visualizar 8 items de exemplo com dados reais
- [x] Cálculo automático de total (preço × quantidade)
- [x] Persistência em memória com Zustand

### ✅ UI/UX
- [x] 7 telas totalmente desenvolvidas
- [x] 4 componentes base (Button, Input, Card, Label)
- [x] Ícones com Lucide React Native (562+ opções)
- [x] Design responsivo com flexbox
- [x] SafeAreaView para notch support
- [x] Centralization de conteúdo (horizontal + vertical)
- [x] Modais para adicionar/editar itens
- [x] FAB (Floating Action Button) com ícone Plus
- [x] Progress bars com cálculo de percentage
- [x] Loading states simulados

### ✅ Desenvolvimento
- [x] TypeScript com strict mode
- [x] State management com Zustand
- [x] Custom hooks ready
- [x] Sample data com dados realistas
- [x] Estrutura scalable
- [x] Código bem organizado e comentado
- [x] Expo Go support para testes em celular real

### 🚀 Pronto Para Próximos Passos
- [ ] **AsyncStorage**: Persistência local de dados
- [ ] **API Backend**: Integração com servidor
- [ ] **Validação**: React Hook Form + Zod
- [ ] **Autenticação Real**: JWT/OAuth
- [ ] **Notificações**: Push notifications
- [ ] **Build**: APK/IPA para distribuição

---

## 🧠 State Management (Zustand)

### useAuthStore
**Localização**: [src/store/useAuthStore.ts](src/store/useAuthStore.ts)

```tsx
interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => void;
  signup: (name: string, email: string, password: string) => void;
  logout: () => void;
}
```

**Exemplo de Uso**:
```tsx
const { user, isAuthenticated, login, logout } = useAuthStore();

// Login
login('user@example.com', 'password123');

// Logout
logout();
```

### useListStore
**Localização**: [src/store/useListStore.ts](src/store/useListStore.ts)

```tsx
interface ListState {
  lists: ShoppingList[];
  currentList: ShoppingList | null;
  
  // List operations
  addList: (list: ShoppingList) => void;
  updateList: (id: string, updates: Partial<ShoppingList>) => void;
  deleteList: (id: string) => void;
  setCurrentList: (id: string) => void;
  
  // Item operations
  addItem: (listId: string, item: ListItem) => void;
  updateItem: (listId: string, itemId: string, updates: Partial<ListItem>) => void;
  deleteItem: (listId: string, itemId: string) => void;
  toggleItemComplete: (listId: string, itemId: string) => void;
}
```

**Sample Data** (carregado por padrão):
```tsx
// 3 Listas
1. "Compras do Supermercado" - 8/12 itens
2. "Compras Semanais" - 3/8 itens
3. "Compras para Festa" - 15/15 itens (completa)

// 8 Itens (lista 1)
- Maçã (10x R$3.50)
- Arroz (5x R$22.00)
- Feijão (3x R$8.50)
- ... e mais 5 itens
```

**Exemplo de Uso**:
```tsx
const { lists, addItem, updateItem, deleteItem } = useListStore();

// Adicionar item
addItem('list-1', { 
  id: 'item-1',
  name: 'Maçã',
  completed: false,
  quantity: 10,
  price: 3.50
});

// Atualizar item
updateItem('list-1', 'item-1', { quantity: 15 });

// Deletar item
deleteItem('list-1', 'item-1');

// Marcar como comprado
toggleItemComplete('list-1', 'item-1');
```

---

## 📚 TypeScript Types

**Localização**: [src/types/index.ts](src/types/index.ts)

```tsx
// Usuário
interface User {
  id: string;
  name: string;
  email: string;
}

// Item da lista
interface ListItem {
  id: string;
  name: string;
  completed: boolean;
  quantity?: number;
  price?: number;
}

// Lista de compras
interface ShoppingList {
  id: string;
  name: string;
  description?: string;
  items: ListItem[];
  completed: number;  // Quantidade de itens comprados
  createdAt: Date;
}
```

---

## 🔧 Guia de Desenvolvimento

### Adicionar Nova Tela

1. **Criar arquivo** em `src/screens/NovaScreen.tsx`:
```tsx
import { View, Text, SafeAreaView } from 'react-native';
import colors from '@/styles/colors';

export default function NovaScreen() {
  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: colors.background }}>
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
        <Text>Nova Tela</Text>
      </View>
    </SafeAreaView>
  );
}
```

2. **Adicionar rota** em `src/screens/RootNavigator.tsx`:
```tsx
<Stack.Screen name="NovaScreen" component={NovaScreen} />
```

### Adicionar Novo Componente

1. **Criar arquivo** em `src/components/NovoComponente.tsx`
2. **Exportar** em `src/components/index.ts`:
```tsx
export { default as NovoComponente } from './NovoComponente';
```

3. **Usar** em qualquer tela:
```tsx
import { NovoComponente } from '@/components';
```

### Usar Estado Zustand

```tsx
import { useAuthStore } from '@/store/useAuthStore';
import { useListStore } from '@/store/useListStore';

export default function MinhaScreen() {
  const { user, logout } = useAuthStore();
  const { lists, addList } = useListStore();

  return (
    // ...
  );
}
```

---

## 🎯 Próximas Melhorias

### Curto Prazo
- [ ] AsyncStorage para persistência
- [ ] Validação de forms com React Hook Form
- [ ] Mensagens de erro/sucesso com Toast
- [ ] Animações de transição entre telas
- [ ] Testes unitários (Jest)

### Médio Prazo
- [ ] Integração com API backend
- [ ] Autenticação real (JWT)
- [ ] Busca e filtro de listas
- [ ] Compartilhamento de listas
- [ ] Temas claro/escuro (Dark Mode)

### Longo Prazo
- [ ] Sincronização offline-first
- [ ] Push notifications
- [ ] Suporte a múltiplos idiomas (i18n)
- [ ] Backup e restore de dados
- [ ] Build APK/IPA para produção

---

## 🐛 Troubleshooting

### Problema: Metro Bundler não inicia
```bash
# Solução: Limpar cache e reinstalar
npm start -- --reset-cache
```

### Problema: Módulos TypeScript não encontrados
```bash
# Solução: Garantir que tsconfig.json está correto
npm run type-check
```

### Problema: Expo Go não conecta
```bash
# Solução: Verificar rede local
# 1. Certifique-se de estar na mesma rede WiFi
# 2. Escaneie novamente o QR code
# 3. Use: npm start -- --tunnel (para conexão por internet)
```

### Problema: Telas não centralizam corretamente
- Verifique se o container tem `flex: 1`
- Adicione `justifyContent: 'center'` e `alignItems: 'center'` ao View
- Use `textAlign: 'center'` em elementos Text

---

## 📚 Recursos & Documentação

### Oficial
- 📖 [Expo Official Docs](https://docs.expo.dev/)
- 📖 [React Native Official Docs](https://reactnative.dev/)
- 📖 [React Navigation Docs](https://reactnavigation.org/)

### Bibliotecas Utilizadas
- 🎯 [Zustand - State Management](https://github.com/pmndrs/zustand)
- 🎨 [Lucide React Native - Icons](https://lucide.dev/docs/lucide-react-native)
- 🛡️ [React Native Safe Area Context](https://github.com/th3rdwave/react-native-safe-area-context)
- 🎭 [React Native Gesture Handler](https://docs.swmansion.com/react-native-gesture-handler/)
- ✨ [React Native Reanimated](https://docs.swmansion.com/react-native-reanimated/)

### Tutoriais Recomendados
- [Expo Tutorial Completo](https://docs.expo.dev/tutorial/introduction/)
- [React Navigation Tutorial](https://reactnavigation.org/docs/hello-react-navigation)
- [Zustand Basics](https://github.com/pmndrs/zustand#basic-example)

---

## 📞 Suporte & Comunidade

- **Expo Community**: https://forums.expo.dev/
- **React Native Community**: https://reactnativecommunity.org/
- **Stack Overflow**: Tag `react-native`, `expo`
- **GitHub Issues**: Abra uma issue neste repositório

---

## 📄 Licença

Este projeto é de código aberto e disponível sob a licença MIT.

---

## 🎉 Conclusão

O Shopping List App é um exemplo completo de aplicação móvel moderna com:
- ✅ Autenticação e navegação condicional
- ✅ State management robusto
- ✅ UI/UX responsivo e intuitivo
- ✅ TypeScript type-safe
- ✅ Arquitetura escalável
- ✅ Pronto para produção com pequenas adições

**Desenvolvido com ❤️ usando Expo e React Native**

---

**Última atualização:** 28 de dezembro de 2025  
**Versão:** 1.0.0  
**Status:** ✅ Produção Pronta
