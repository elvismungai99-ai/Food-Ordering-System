import api from "../api/axios";
import { requestData } from "./request";

export interface CartItem {
  id: string;
  menuItemId: string;
  name: string;
  description?: string | null;
  imageUrl?: string | null;
  quantity: number;
  unitPrice: number;
  currentPrice: number;
  subtotal: number;
  currentSubtotal: number;
  priceChanged: boolean;
  available: boolean;

  selectedSize?: string | null;
  selectedAddOns?: string[];
  specialInstructions?: string | null;
  removalRequests?: string[];
}

export interface Cart {
  id: string;
  customerId: string;
  items: CartItem[];
  totalItems: number;
  previousTotalAmount: number;
  totalAmount: number;
  subtotalAmount?: number;
  deliveryFee?: number;
  serviceFee?: number;
  taxAmount?: number;
  discountAmount?: number;
  finalTotalAmount?: number;
  hasPriceChanges: boolean;
  hasUnavailableItems: boolean;
}

export interface AddCartItemRequest {
  menuItemId: string;
  quantity: number;
  selectedSize?: string | null;
  selectedAddOns?: string[];
  specialInstructions?: string | null;
  removalRequests?: string[];
  extraPrice?: number;
}

export async function getCart(): Promise<Cart> {
  return requestData(
    () => api.get<Cart>("/cart"),
    "Unable to load your cart."
  );
}

export async function addCartItem(
  request: AddCartItemRequest
): Promise<Cart> {
  return requestData(
    () => api.post<Cart>(
      "/cart/items",
      request
    ),
    "Unable to add this item to your cart."
  );
}

export async function updateCartItemQuantity(
  cartItemId: string,
  quantity: number
): Promise<Cart> {
  return requestData(
    () => api.patch<Cart>(
      `/cart/items/${cartItemId}`,
      { quantity }
    ),
    "Unable to update the item quantity."
  );
}

export async function removeCartItem(
  cartItemId: string
): Promise<Cart> {
  return requestData(
    () => api.delete<Cart>(
      `/cart/items/${cartItemId}`
    ),
    "Unable to remove the cart item."
  );
}

export async function acceptCartPriceChanges(): Promise<Cart> {
  return requestData(
    () => api.post<Cart>(
      "/cart/accept-price-changes"
    ),
    "Unable to accept the updated prices."
  );
}
