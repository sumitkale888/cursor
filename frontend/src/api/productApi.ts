import {apiClient} from './client';
import {Product} from '../types';

export async function getAllProducts(): Promise<Product[]> {
  const response = await apiClient.get<Product[]>('/products');
  return response.data;
}

export async function getProductById(id: number): Promise<Product> {
  const response = await apiClient.get<Product>(`/products/${id}`);
  return response.data;
}

export async function searchProducts(query: string): Promise<Product[]> {
  const response = await apiClient.get<Product[]>('/products/search', {
    params: {q: query},
  });
  return response.data;
}

export async function getProductsByCategory(
  categoryId: number,
): Promise<Product[]> {
  const response = await apiClient.get<Product[]>(
    `/products/category/${categoryId}`,
  );
  return response.data;
}

export function extractCategories(products: Product[]) {
  const map = new Map<
    number,
    {id: number; name: string; productCount: number}
  >();
  products.forEach(product => {
    const existing = map.get(product.categoryId);
    if (existing) {
      existing.productCount += 1;
    } else {
      map.set(product.categoryId, {
        id: product.categoryId,
        name: product.categoryName,
        productCount: 1,
      });
    }
  });
  return Array.from(map.values());
}
