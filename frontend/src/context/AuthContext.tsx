import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react';
import * as authApi from '../api/authApi';
import {parseApiError} from '../api/client';
import {LoginRequest, RegisterRequest, User} from '../types';
import * as storage from '../utils/storage';

interface AuthContextValue {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({children}: {children: React.ReactNode}) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function loadSession() {
      try {
        const storedUser = await storage.getUser();
        const token = await storage.getToken();
        if (storedUser && token) {
          setUser(storedUser);
        }
      } finally {
        setIsLoading(false);
      }
    }
    loadSession();
  }, []);

  const login = useCallback(async (data: LoginRequest) => {
    try {
      const auth = await authApi.login(data);
      await storage.saveAuth(auth);
      setUser({
        userId: auth.userId,
        email: auth.email,
        firstName: auth.firstName,
        lastName: auth.lastName,
      });
    } catch (error) {
      const apiError = parseApiError(error);
      throw new Error(apiError.message);
    }
  }, []);

  const register = useCallback(async (data: RegisterRequest) => {
    try {
      const auth = await authApi.register(data);
      await storage.saveAuth(auth);
      setUser({
        userId: auth.userId,
        email: auth.email,
        firstName: auth.firstName,
        lastName: auth.lastName,
      });
    } catch (error) {
      const apiError = parseApiError(error);
      throw new Error(apiError.message);
    }
  }, []);

  const logout = useCallback(async () => {
    await storage.clearAuth();
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({
      user,
      isLoading,
      isAuthenticated: !!user,
      login,
      register,
      logout,
    }),
    [user, isLoading, login, register, logout],
  );

  return (
    <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}
