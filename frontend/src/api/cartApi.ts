import {apiClient} from './client';
import {Cart} from '../types';

export async function getCart(): Promise<Cart> {
  const response = await apiClient.get<Cart>('/cart');
  return response.data;
}

export async function addToCart(
  productId: number,
  quantity: number,
): Promise<Cart> {
  const response = await apiClient.post<Cart>('/cart/items', {
    productId,
    quantity,
  });
  return response.data;
}

export async function removeFromCart(productId: number): Promise<Cart> {
  const response = await apiClient.delete<Cart>(`/cart/items/${productId}`);
  return response.data;
}

export async function updateCartQuantity(
  productId: number,
  quantity: number,
): Promise<Cart> {
  const response = await apiClient.put<Cart>(`/cart/items/${productId}`, {
    quantity,
  });
  return response.data;
}
