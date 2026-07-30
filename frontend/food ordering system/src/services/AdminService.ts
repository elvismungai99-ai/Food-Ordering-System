import api from "../api/axios";
import {
  requestData,
  requestVoid,
} from "./request";

export interface Customer {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  role: string;
  active: boolean;
  createdAt: string;
}

export interface Restaurant {
  id: string;
  name: string;
  description: string;
  address: string;
  openingTime: string;
  closingTime: string;
  status: string;
  category?: string;
  openNow?: boolean;
  averageRating?: number;
  reviewCount?: number;
  ownerId?: string;
}

export type RiderStatus =
  | "PENDING_APPROVAL"
  | "APPROVED"
  | "SUSPENDED"
  | "REJECTED";

export interface Rider {
  id: string;
  userId: string;
  fullName: string;
  phoneNumber: string;
  email: string;
  vehicleType: string;
  licencePlate: string;
  status: RiderStatus;
  operationalStatus: string;
  online: boolean;
  totalRejections: number;
  currentLatitude?: number | null;
  currentLongitude?: number | null;
  lastLocationUpdatedAt?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface DeliveryActivity {
  id: string;
  orderId: string;
  riderId: string;
  restaurantId: string;
  restaurantName: string;
  restaurantAddress: string;
  customerAddress: string;
  distanceKm?: number | null;
  estimatedPayout: number;
  assignmentScore?: number | null;
  status: string;
  rejectionReason?: string | null;
  requestedAt?: string | null;
  respondedAt?: string | null;
  arrivedAtRestaurantAt?: string | null;
  pickedUpAt?: string | null;
  deliveredAt?: string | null;
}

export const getAllCustomers = async (): Promise<Customer[]> => {
  return requestData(
    () => api.get<Customer[]>("/admin/customers"),
    "Unable to load customers."
  );
};

export const deleteCustomer = async (id: string): Promise<void> => {
  await requestVoid(
    () => api.delete(`/admin/customers/${id}`),
    "Unable to delete the customer."
  );
};

export const getAllRestaurants = async (): Promise<Restaurant[]> => {
  return requestData(
    () => api.get<Restaurant[]>("/admin/restaurants"),
    "Unable to load restaurants."
  );
};

export const deleteRestaurant = async (id: string): Promise<void> => {
  await requestVoid(
    () => api.delete(`/admin/restaurants/${id}`),
    "Unable to delete the restaurant."
  );
};

export const updateRestaurantStatus = async (
  id: string,
  status:
    | "PENDING_APPROVAL"
    | "APPROVED"
    | "SUSPENDED"
    | "REJECTED"
): Promise<Restaurant> => {
  return requestData(
    () => api.patch<Restaurant>(
      `/admin/restaurants/${id}/status`,
      null,
      {
        params: {
          status,
        },
      }
    ),
    "Unable to update restaurant approval status."
  );
};

export const getAllRiders = async (): Promise<Rider[]> => {
  return requestData(
    () => api.get<Rider[]>("/admin/riders"),
    "Unable to load riders."
  );
};

export const updateRiderStatus = async (
  id: string,
  status: RiderStatus
): Promise<Rider> => {
  return requestData(
    () => api.patch<Rider>(
      `/admin/riders/${id}/status`,
      null,
      {
        params: {
          status,
        },
      }
    ),
    "Unable to update rider status."
  );
};

export const getRiderActivities = async ():
Promise<DeliveryActivity[]> => {
  return requestData(
    () => api.get<DeliveryActivity[]>(
      "/admin/riders/activities"
    ),
    "Unable to load rider activities."
  );
};
