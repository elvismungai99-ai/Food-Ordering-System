import api from "../api/axios";

export type OrderStatus =
  | "PENDING"
  | "CONFIRMED"
  | "PREPARING"
  | "READY_FOR_PICKUP"
  | "OUT_FOR_DELIVERY"
  | "DELIVERED"
  | "CANCELLED";

export interface OrderItem {
  id: string;
  menuItemId: string | null;

  itemName: string;
  itemDescription?: string | null;
  imageUrl?: string | null;

  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface Order {
  id: string;

  customerId: string;
  restaurantId: string;

  restaurantName: string;
  deliveryAddress: string;

  status: OrderStatus;

  paymentStatus: string;
  paymentReference?: string | null;

  totalAmount: number;

  items: OrderItem[];

  createdAt: string;
  updatedAt: string;
}

export interface PlaceOrderRequest {
  deliveryAddress: string;
}

/*
 * Customer:
 * Place a new order using the current cart.
 *
 * Backend:
 * POST /api/orders
 */
export async function placeOrder(
  request: PlaceOrderRequest
): Promise<Order> {
  const response =
    await api.post<Order>(
      "/orders",
      request
    );

  return response.data;
}

/*
 * Customer:
 * Get all orders belonging to the
 * currently authenticated customer.
 *
 * Backend:
 * GET /api/orders
 */
export async function getCustomerOrders():
Promise<Order[]> {
  const response =
    await api.get<Order[]>(
      "/orders"
    );

  return response.data;
}

/*
 * Customer:
 * Get one specific order belonging
 * to the currently authenticated customer.
 *
 * Backend:
 * GET /api/orders/{orderId}
 */
export async function getCustomerOrder(
  orderId: string
): Promise<Order> {
  const response =
    await api.get<Order>(
      `/orders/${orderId}`
    );

  return response.data;
}

/*
 * Restaurant admin:
 * Get all orders belonging to
 * a specific restaurant.
 *
 * Backend:
 * GET /api/orders/restaurant/{restaurantId}
 */
export async function getRestaurantOrders(
  restaurantId: string
): Promise<Order[]> {
  const response =
    await api.get<Order[]>(
      `/orders/restaurant/${restaurantId}`
    );

  return response.data;
}

/*
 * Restaurant admin:
 * Move an order through the
 * order state machine.
 *
 * Backend:
 * PATCH /api/orders/{orderId}/status
 */
export async function updateOrderStatus(
  orderId: string,
  status: OrderStatus
): Promise<Order> {
  const response =
    await api.patch<Order>(
      `/orders/${orderId}/status`,
      {
        status,
      }
    );

  return response.data;
}