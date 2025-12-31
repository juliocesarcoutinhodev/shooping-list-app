/**
 * Infrastructure Layer - Storage Services
 *
 * Local storage and cache implementations for offline support.
 */

import AsyncStorage from '@react-native-async-storage/async-storage';

export * from './auth-storage';

export interface StorageService {
  setItem(key: string, value: string): Promise<void>;
  getItem(key: string): Promise<string | null>;
  removeItem(key: string): Promise<void>;
  clear(): Promise<void>;
}

export class AsyncStorageService implements StorageService {
  async setItem(key: string, value: string): Promise<void> {
    try {
      await AsyncStorage.setItem(key, value);
    } catch (error) {
      console.error('AsyncStorage setItem error:', error);
      throw error;
    }
  }

  async getItem(key: string): Promise<string | null> {
    try {
      return await AsyncStorage.getItem(key);
    } catch (error) {
      console.error('AsyncStorage getItem error:', error);
      return null;
    }
  }

  async removeItem(key: string): Promise<void> {
    try {
      await AsyncStorage.removeItem(key);
    } catch (error) {
      console.error('AsyncStorage removeItem error:', error);
      throw error;
    }
  }

  async clear(): Promise<void> {
    try {
      await AsyncStorage.clear();
    } catch (error) {
      console.error('AsyncStorage clear error:', error);
      throw error;
    }
  }
}

export class CacheService {
  private static readonly CACHE_PREFIX = 'cache_';
  private static readonly CACHE_EXPIRY = 'cache_expiry_';

  constructor(private storage: StorageService) {}

  async set<T>(key: string, value: T, expiryMinutes: number = 60): Promise<void> {
    const cacheKey = CacheService.CACHE_PREFIX + key;
    const expiryKey = CacheService.CACHE_EXPIRY + key;
    const expiryTime = Date.now() + expiryMinutes * 60 * 1000;

    await Promise.all([
      this.storage.setItem(cacheKey, JSON.stringify(value)),
      this.storage.setItem(expiryKey, expiryTime.toString()),
    ]);
  }

  async get<T>(key: string): Promise<T | null> {
    const cacheKey = CacheService.CACHE_PREFIX + key;
    const expiryKey = CacheService.CACHE_EXPIRY + key;

    try {
      const [cachedValue, expiryTimeStr] = await Promise.all([
        this.storage.getItem(cacheKey),
        this.storage.getItem(expiryKey),
      ]);

      if (!cachedValue || !expiryTimeStr) {
        return null;
      }

      const expiryTime = parseInt(expiryTimeStr, 10);
      if (Date.now() > expiryTime) {
        // Cache expired, clean up
        await this.remove(key);
        return null;
      }

      return JSON.parse(cachedValue);
    } catch (error) {
      console.error('Cache get error:', error);
      return null;
    }
  }

  async remove(key: string): Promise<void> {
    const cacheKey = CacheService.CACHE_PREFIX + key;
    const expiryKey = CacheService.CACHE_EXPIRY + key;

    await Promise.all([this.storage.removeItem(cacheKey), this.storage.removeItem(expiryKey)]);
  }

  async clear(): Promise<void> {
    await this.storage.clear();
  }
}
