import api from "../api/axios";
import {
  requestData,
} from "./request";

export type VehicleType =
  | "BICYCLE"
  | "MOTORCYCLE"
  | "CAR";

export type RiderOperationalStatus =
  | "OPEN"
  | "CLOSED";

export type DeliveryRequestStatus =
  | "REQUESTED"
  | "ACCEPTED"
  | "REJECTED"
  | "ARRIVED_AT_RESTAURANT"
  | "PICKED_UP"
  | "DELIVERED"
  | "CANCELLED";

export interface RiderRegistrationRequest {
  fullName: string;
  phoneNumber: string;
  email: string;
  password: string;
  vehicleType: VehicleType;
  licencePlate: string;
}

export interface AuthResponse {
  token: string;
  userId: string;
  role: string;
  firstName: string;
}

export interface Rider {
  id: string;
  userId: string;
  fullName: string;
  phoneNumber: string;
  email: string;
  vehicleType: VehicleType;
  licencePlate: string;
  status: string;
  operationalStatus: RiderOperationalStatus;
  online: boolean;
  totalRejections: number;
  currentLatitude?: number | null;
  currentLongitude?: number | null;
  lastLocationUpdatedAt?: string | null;
}

export interface DeliveryRequest {
  id: string;
  orderId: string;
  riderId: string;
  restaurantId: string;
  restaurantName: string;
  restaurantAddress: string;
  restaurantLatitude?: number | null;
  restaurantLongitude?: number | null;
  customerAddress: string;
  customerLatitude?: number | null;
  customerLongitude?: number | null;
  distanceKm?: number | null;
  estimatedPayout: number;
  assignmentScore?: number | null;
  status: DeliveryRequestStatus;
  rejectionReason?: string | null;
  requestedAt?: string | null;
  respondedAt?: string | null;
  arrivedAtRestaurantAt?: string | null;
  pickedUpAt?: string | null;
  deliveredAt?: string | null;
}

export interface RiderDashboard {
  rider: Rider;
  totalEarnings: number;
  pendingPayout: number;
  completedDeliveries: number;
  rejectedRequests: number;
  deliveryRequests: DeliveryRequest[];
}

export interface CreateDeliveryRequest {
  orderId: string;
  riderId: string;
  restaurantLatitude?: number | null;
  restaurantLongitude?: number | null;
  estimatedPayout?: number | null;
}

export interface AutoDeliveryRequest {
  orderId: string;
  restaurantLatitude: number;
  restaurantLongitude: number;
  estimatedPayout?: number | null;
}

export function registerRider(
  request: RiderRegistrationRequest
): Promise<AuthResponse> {
  return requestData(
    () => api.post<AuthResponse>(
      "/riders/register",
      request
    ),
    "Unable to register rider."
  );
}

export function getRiderDashboard():
Promise<RiderDashboard> {
  return requestData(
    () => api.get<RiderDashboard>(
      "/riders/me/dashboard"
    ),
    "Unable to load rider dashboard."
  );
}

export function updateRiderAvailability(
  operationalStatus: RiderOperationalStatus,
  online: boolean
): Promise<Rider> {
  return requestData(
    () => api.patch<Rider>(
      "/riders/me/availability",
      {
        operationalStatus,
        online,
      }
    ),
    "Unable to update rider availability."
  );
}

export function updateRiderLocation(
  latitude: number,
  longitude: number
): Promise<Rider> {
  return requestData(
    () => api.patch<Rider>(
      "/riders/me/location",
      {
        latitude,
        longitude,
      }
    ),
    "Unable to update rider location."
  );
}

export function acceptDeliveryRequest(
  requestId: string
): Promise<DeliveryRequest> {
  return requestData(
    () => api.patch<DeliveryRequest>(
      `/riders/delivery-requests/${requestId}/accept`
    ),
    "Unable to accept delivery request."
  );
}

export function rejectDeliveryRequest(
  requestId: string,
  reason: string
): Promise<DeliveryRequest> {
  return requestData(
    () => api.patch<DeliveryRequest>(
      `/riders/delivery-requests/${requestId}/reject`,
      {
        reason,
      }
    ),
    "Unable to reject delivery request."
  );
}

export function markArrivedAtRestaurant(
  requestId: string
): Promise<DeliveryRequest> {
  return requestData(
    () => api.patch<DeliveryRequest>(
      `/riders/delivery-requests/${requestId}/arrived-restaurant`
    ),
    "Unable to mark arrival."
  );
}

export function confirmPickup(
  requestId: string
): Promise<DeliveryRequest> {
  return requestData(
    () => api.patch<DeliveryRequest>(
      `/riders/delivery-requests/${requestId}/pickup`
    ),
    "Unable to confirm pickup."
  );
}

export function confirmDelivery(
  requestId: string
): Promise<DeliveryRequest> {
  return requestData(
    () => api.patch<DeliveryRequest>(
      `/riders/delivery-requests/${requestId}/delivered`
    ),
    "Unable to confirm delivery."
  );
}

export function getAvailableRiders():
Promise<Rider[]> {
  return requestData(
    () => api.get<Rider[]>(
      "/delivery-dispatch/available-riders"
    ),
    "Unable to load available riders."
  );
}

export function getRestaurantDeliveryRequests():
Promise<DeliveryRequest[]> {
  return requestData(
    () => api.get<DeliveryRequest[]>(
      "/delivery-dispatch/requests"
    ),
    "Unable to load delivery requests."
  );
}

export function createDeliveryRequest(
  request: CreateDeliveryRequest
): Promise<DeliveryRequest> {
  return requestData(
    () => api.post<DeliveryRequest>(
      "/delivery-dispatch/requests",
      request
    ),
    "Unable to send delivery request."
  );
}

export function createAutomaticDeliveryRequest(
  request: AutoDeliveryRequest
): Promise<DeliveryRequest> {
  return requestData(
    () => api.post<DeliveryRequest>(
      "/delivery-dispatch/requests/auto",
      request
    ),
    "Unable to assign rider automatically."
  );
}
