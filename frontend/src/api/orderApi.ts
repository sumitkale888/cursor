import {apiClient} from './client';
import {Order, OrderSummary} from '../types';

export async function placeOrder(): Promise<Order> {
  const response = await apiClient.post<Order>('/orders');
  return response.data;
}

export async function getOrderHistory(): Promise<OrderSummary[]> {
  const response = await apiClient.get<OrderSummary[]>('/orders');
  return response.data;
}

export async function getOrderDetails(orderId: number): Promise<Order> {
  const response = await apiClient.get<Order>(`/orders/${orderId}`);
  return response.data;
}
