/**
 * Data Layer - Models
 *
 * DTOs and data transfer objects for external APIs and data sources.
 * These may differ from domain entities to handle API-specific formats.
 */

// Auth DTOs
export interface LoginRequestDto {
  email: string;
  password: string;
}

export interface RegisterRequestDto {
  name: string;
  email: string;
  password: string;
}

export interface LoginResponseDto {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface RegisterResponseDto {
  id: number;
  email: string;
  name: string;
  provider: string;
  status: string;
  createdAt: string;
}

export interface RefreshTokenRequestDto {
  refreshToken: string;
}

export interface RefreshTokenResponseDto {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface LogoutRequestDto {
  refreshToken: string;
}

export interface GoogleLoginRequestDto {
  idToken: string;
}

export interface GoogleLoginResponseDto {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface UserMeResponseDto {
  id: number;
  email: string;
  name: string;
  provider: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface ShoppingItemDto {
  id: string | number;
  name: string;
  quantity: number;
  unit_price?: number;
  unitPrice?: number;
  unit?: string;
  status?: string; // "PENDING" ou "PURCHASED" (formato do backend)
  is_purchased?: boolean;
  isPurchased?: boolean;
  is_completed?: boolean;
  isCompleted?: boolean;
  created_at?: string;
  createdAt?: string;
  updated_at?: string;
  updatedAt?: string;
}

export interface ShoppingListDto {
  id: string | number;
  title: string;
  description?: string;
  items?: ShoppingItemDto[];
  itemsCount?: number;
  pendingItemsCount?: number;
  // API pode retornar camelCase ou snake_case
  createdAt?: string;
  updatedAt?: string;
  created_at?: string;
  updated_at?: string;
}

export interface UserDto {
  id: string;
  email: string;
  name: string;
  created_at: string;
}

export interface CreateShoppingListRequest {
  title: string;
}

export interface AddItemRequestDto {
  name: string;
  quantity: number;
  unit?: string;
  unitPrice?: number;
}

export interface UpdateItemRequestDto {
  name?: string;
  quantity?: number;
  unit?: string;
  unitPrice?: number;
  status?: 'PENDING' | 'PURCHASED';
}
