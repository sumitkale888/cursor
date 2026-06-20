import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react';
import * as cartApi from '../api/cartApi';
import {parseApiError} from '../api/client';
import {Cart} from '../types';
import {useAuth} from './AuthContext';

interface CartContextValue {
  cart: Cart | null;
  isLoading: boolean;
  error: string | null;
  refreshCart: () => Promise<void>;
  addItem: (productId: number, quantity?: number) => Promise<void>;
  removeItem: (productId: number) => Promise<void>;
  updateQuantity: (productId: number, quantity: number) => Promise<void>;
  clearCartState: () => void;
  itemCount: number;
}

const emptyCart: Cart = {
  id: 0,
  items: [],
  totalAmount: 0,
  totalItems: 0,
};

const CartContext = createContext<CartContextValue | undefined>(undefined);

export function CartProvider({children}: {children: React.ReactNode}) {
  const {isAuthenticated} = useAuth();
  const [cart, setCart] = useState<Cart | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refreshCart = useCallback(async () => {
    if (!isAuthenticated) {
      setCart(null);
      return;
    }
    setIsLoading(true);
    setError(null);
    try {
      const data = await cartApi.getCart();
      setCart(data);
    } catch (err) {
      const apiError = parseApiError(err);
      setError(apiError.message);
    } finally {
      setIsLoading(false);
    }
  }, [isAuthenticated]);

  const addItem = useCallback(
    async (productId: number, quantity = 1) => {
      setIsLoading(true);
      setError(null);
      try {
        const data = await cartApi.addToCart(productId, quantity);
        setCart(data);
      } catch (err) {
        const apiError = parseApiError(err);
        setError(apiError.message);
        throw new Error(apiError.message);
      } finally {
        setIsLoading(false);
      }
    },
    [],
  );

  const removeItem = useCallback(async (productId: number) => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await cartApi.removeFromCart(productId);
      setCart(data);
    } catch (err) {
      const apiError = parseApiError(err);
      setError(apiError.message);
      throw new Error(apiError.message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const updateQuantity = useCallback(
    async (productId: number, quantity: number) => {
      setIsLoading(true);
      setError(null);
      try {
        const data = await cartApi.updateCartQuantity(productId, quantity);
        setCart(data);
      } catch (err) {
        const apiError = parseApiError(err);
        setError(apiError.message);
        throw new Error(apiError.message);
      } finally {
        setIsLoading(false);
      }
    },
    [],
  );

  const clearCartState = useCallback(() => {
    setCart(null);
    setError(null);
  }, []);

  useEffect(() => {
    if (isAuthenticated) {
      refreshCart();
    } else {
      clearCartState();
    }
  }, [isAuthenticated, refreshCart, clearCartState]);

  const itemCount = cart?.totalItems ?? 0;

  const value = useMemo(
    () => ({
      cart: cart ?? (isAuthenticated ? emptyCart : null),
      isLoading,
      error,
      refreshCart,
      addItem,
      removeItem,
      updateQuantity,
      clearCartState,
      itemCount,
    }),
    [
      cart,
      isAuthenticated,
      isLoading,
      error,
      refreshCart,
      addItem,
      removeItem,
      updateQuantity,
      clearCartState,
      itemCount,
    ],
  );

  return (
    <CartContext.Provider value={value}>{children}</CartContext.Provider>
  );
}

export function useCart(): CartContextValue {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within CartProvider');
  }
  return context;
}
