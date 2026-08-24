import api from "../api/axios";
import {
  requestData,
} from "./request";

export type OrderStatus =
  | "PENDING"
  | "CONFIRMED"
  | "PREPARING"
  | "READY_FOR_PICKUP"
  | "OUT_FOR_DELIVERY"
  | "DELIVERED"
  | "CANCELLED";

export type PaymentMethod =
  | "MPESA"
  | "CASH_ON_DELIVERY";

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
  deliveryLatitude?: number | null;
  deliveryLongitude?: number | null;

  status: OrderStatus;

  paymentStatus: string;
  paymentMethod: PaymentMethod;
  paymentReference?: string | null;
  cancellationReason?: string | null;
  cancelledAt?: string | null;

  subtotalAmount: number;
  deliveryFee: number;
  serviceFee: number;
  taxAmount: number;
  discountAmount: number;
  totalAmount: number;

  items: OrderItem[];

  createdAt: string;
  updatedAt: string;
}

export interface PlaceOrderRequest {
  deliveryAddress: string;
  deliveryLatitude?: number | null;
  deliveryLongitude?: number | null;
  paymentMethod?: PaymentMethod;
  mpesaPhoneNumber?: string;
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
  return requestData(
    () => api.post<Order>(
      "/orders",
      request
    ),
    "Unable to place your order."
  );
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
  return requestData(
    () => api.get<Order[]>(
      "/orders"
    ),
    "Unable to load your orders."
  );
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
  return requestData(
    () => api.get<Order>(
      `/orders/${orderId}`
    ),
    "Unable to load this order."
  );
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
  return requestData(
    () => api.get<Order[]>(
      `/orders/restaurant/${restaurantId}`
    ),
    "Unable to load restaurant orders."
  );
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
  return requestData(
    () => api.patch<Order>(
      `/orders/${orderId}/status`,
      {
        status,
      }
    ),
    "Unable to update the order status."
  );
}

/*
 * Live tracking snapshot returned by:
 * GET /api/orders/{orderId}/tracking
 */
export interface OrderTracking {
  orderId: string;
  status: OrderStatus;

  restaurantName: string;
  restaurantLatitude?: number | null;
  restaurantLongitude?: number | null;

  destinationLatitude?: number | null;
  destinationLongitude?: number | null;

  riderAssigned: boolean;
  riderId?: string | null;
  riderName?: string | null;
  riderPhoneNumber?: string | null;
  vehicleType?: VehicleType | null;
  licencePlate?: string | null;

  riderLatitude?: number | null;
  riderLongitude?: number | null;
  riderLocationUpdatedAt?: string | null;

  deliveryStatus?: DeliveryRequestStatus | null;
  requestedAt?: string | null;
  pickedUpAt?: string | null;
  deliveredAt?: string | null;
}

type VehicleType =
  | "BICYCLE"
  | "MOTORCYCLE"
  | "CAR";

type DeliveryRequestStatus =
  | "REQUESTED"
  | "ACCEPTED"
  | "REJECTED"
  | "ARRIVED_AT_RESTAURANT"
  | "PICKED_UP"
  | "DELIVERED"
  | "CANCELLED";

/*
 * Customer:
 * Get the live tracking snapshot for one
 * specific order (status, restaurant pickup
 * point, destination and the assigned
 * rider's most recent GPS position).
 *
 * Backend:
 * GET /api/orders/{orderId}/tracking
 */
export async function getOrderTracking(
  orderId: string
): Promise<OrderTracking> {
  return requestData(
    () => api.get<OrderTracking>(
      `/orders/${orderId}/tracking`
    ),
    "Unable to load live tracking."
  );
}

export async function cancelCustomerOrder(
  orderId: string,
  reason: string
): Promise<Order> {
  return requestData(
    () => api.patch<Order>(
      `/orders/${orderId}/cancel`,
      {
        reason,
      }
    ),
    "Unable to cancel this order."
  );
}

export async function cancelRestaurantOrder(
  orderId: string,
  reason: string
): Promise<Order> {
  return requestData(
    () => api.patch<Order>(
      `/orders/${orderId}/restaurant-cancel`,
      {
        reason,
      }
    ),
    "Unable to cancel this restaurant order."
  );
}
