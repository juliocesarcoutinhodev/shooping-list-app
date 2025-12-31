# Design System - Guia de Uso

## 🎨 **Sistema de Design Tokens**

Este projeto utiliza um sistema centralizado de Design Tokens para garantir consistência visual e facilitar a manutenção da identidade visual.

### 📁 **Estrutura**

```
src/presentation/theme/
├── colors.ts          # Paleta de cores light/dark
├── typography.ts      # Sistema tipográfico (Inter + fallbacks)
├── layout.ts          # Espaçamento, bordas, sombras, z-index
├── theme.ts          # Combinação de todos os tokens
└── index.ts          # Barrel export
```

## 🔧 **Como Usar**

### **1. Hook useAppTheme**

```tsx
import { useAppTheme } from '../hooks';

export function MeuComponente() {
  const theme = useAppTheme();
  
  return (
    <View style={{ backgroundColor: theme.colors.background }}>
      <Text style={{ color: theme.colors.text }}>
        Texto que respeita o tema
      </Text>
    </View>
  );
}
```

### **2. Tokens Disponíveis**

#### **Cores**
```tsx
theme.colors.background       // Fundo principal
theme.colors.text            // Texto principal
theme.colors.textSecondary   // Texto secundário
theme.colors.primary         // Cor primária (azul)
theme.colors.surface         // Cards e modais
theme.colors.border          // Bordas
theme.colors.error           // Estados de erro
```

#### **Tipografia**
```tsx
theme.typography.h1          // Heading 1
theme.typography.body        // Corpo de texto
theme.typography.button      // Texto de botões
theme.typography.caption     // Legendas
```

#### **Espaçamento**
```tsx
theme.spacing[4]             // 16px
theme.spacing[6]             // 24px
theme.spacing[8]             // 32px
```

#### **Border Radius**
```tsx
theme.radius.base            // 8px (padrão)
theme.radius.lg              // 16px
theme.radius.full            // Círculo
```

#### **Sombras**
```tsx
theme.shadows.base           // Sombra padrão
theme.shadows.lg             // Sombra mais acentuada
```

### **3. Exemplo Prático**

```tsx
import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { useAppTheme } from '../hooks';

export function ExampleCard() {
  const theme = useAppTheme();

  return (
    <View 
      style={[
        styles.card, 
        { 
          backgroundColor: theme.colors.surface,
          borderColor: theme.colors.border,
          ...theme.shadows.base,
        }
      ]}
    >
      <Text style={[styles.title, { color: theme.colors.text }]}>
        Título do Card
      </Text>
      <Text style={[styles.description, { color: theme.colors.textSecondary }]}>
        Descrição usando design tokens
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    padding: 16,
    borderRadius: 12,
    borderWidth: 1,
    marginBottom: 12,
  },
  title: {
    fontSize: 18,
    fontWeight: '600',
    marginBottom: 8,
  },
  description: {
    fontSize: 14,
    lineHeight: 20,
  },
});
```

## 🌙 **Suporte a Tema Claro/Escuro**

O sistema detecta automaticamente a preferência do usuário:

- **Light Mode**: Cores claras, fundo branco
- **Dark Mode**: Cores escuras, fundo preto

## ✅ **Vantagens**

1. **Consistência**: Todas as cores e espaçamentos são centralizados
2. **Manutenibilidade**: Mudanças em um lugar se aplicam em toda a app
3. **Acessibilidade**: Suporte automático a temas claro/escuro
4. **Escalabilidade**: Fácil adicionar novos tokens
5. **Type Safety**: TypeScript garante que tokens existem

## 📏 **Escalas Utilizadas**

- **Espaçamento**: Múltiplos de 4px (4, 8, 12, 16, 20, 24, 32...)
- **Tipografia**: Escala harmônica baseada em 1.25 (Major Third)
- **Cores**: Paleta inspirada no iOS Human Interface Guidelines

---

**Sempre use os design tokens ao invés de valores hardcoded!** 🎨✨
