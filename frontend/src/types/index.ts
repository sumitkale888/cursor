export interface AuthResponse {
  token: string;
  tokenType: string;
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  stock: number;
  imageUrl: string | null;
  categoryId: number;
  categoryName: string;
}

export interface Category {
  id: number;
  name: string;
  description?: string;
  imageUrl?: string | null;
  productCount?: number;
}

export interface CartItem {
  id: number;
  productId: number;
  productName: string;
  productImageUrl: string | null;
  price: number;
  quantity: number;
  subtotal: number;
}

export interface Cart {
  id: number;
  items: CartItem[];
  totalAmount: number;
  totalItems: number;
}

export interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  price: number;
  subtotal: number;
}

export interface OrderSummary {
  id: number;
  status: string;
  totalAmount: number;
  createdAt: string;
  itemCount: number;
}

export interface Order {
  id: number;
  totalAmount: number;
  status: string;
  createdAt: string;
  items: OrderItem[];
}

export interface ApiError {
  status: number;
  message: string;
  errors?: Record<string, string>;
}

export interface User {
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
}
