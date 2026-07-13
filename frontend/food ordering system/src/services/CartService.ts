import api from "../api/axios";

export interface CartItem {
  id: string;
  menuItemId: string;
  name: string;
  description: string;
  imageUrl?: string | null;
  quantity: number;
  unitPrice: number;
  subtotal: number;
  available: boolean;
}

export interface Cart {
  id: string;
  customerId: string;
  items: CartItem[];
  totalItems: number;
  totalAmount: number;
}

export interface AddCartItemRequest {
  menuItemId: string;
  quantity: number;
}

export async function getCart(): Promise<Cart> {
  const response = await api.get<Cart>("/cart");
  return response.data;
}

export async function addCartItem(
  request: AddCartItemRequest
): Promise<Cart> {
  const response = await api.post<Cart>(
    "/cart/items",
    request
  );

  return response.data;
}

export async function updateCartItemQuantity(
  cartItemId: string,
  quantity: number
): Promise<Cart> {
  const response = await api.patch<Cart>(
    `/cart/items/${cartItemId}`,
    { quantity }
  );

  return response.data;
}

export async function removeCartItem(
  cartItemId: string
): Promise<Cart> {
  const response = await api.delete<Cart>(
    `/cart/items/${cartItemId}`
  );

  return response.data;
}