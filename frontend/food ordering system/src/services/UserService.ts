import api from "../api/axios";
import { requestData } from "./request";

export interface UserProfile {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  phoneNumber?: string | null;
  role: string;
  createdAt: string;
}

export interface UpdateProfileRequest {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface SavedAddress {
  id: string;
  userId: string;
  label: string;
  address: string;
  buildingName?: string | null;
  apartmentNumber?: string | null;
  landmarks?: string | null;
  deliveryInstructions?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  default: boolean;
  createdAt: string;
}

export interface SavedAddressRequest {
  label: string;
  address: string;
  buildingName?: string;
  apartmentNumber?: string;
  landmarks?: string;
  deliveryInstructions?: string;
  latitude?: number;
  longitude?: number;
  isDefault?: boolean;
}

export async function getUserProfile(): Promise<UserProfile> {
  return requestData(
    () => api.get<UserProfile>("/users/profile"),
    "Unable to load profile information."
  );
}

export async function updateUserProfile(request: UpdateProfileRequest): Promise<UserProfile> {
  return requestData(
    () => api.put<UserProfile>("/users/profile", request),
    "Unable to update your profile."
  );
}

export async function changeUserPassword(request: ChangePasswordRequest): Promise<{ message: string }> {
  return requestData(
    () => api.put<{ message: string }>("/users/password", request),
    "Unable to update your password."
  );
}

export async function getSavedAddresses(): Promise<SavedAddress[]> {
  return requestData(
    () => api.get<SavedAddress[]>("/users/addresses"),
    "Unable to load saved addresses."
  );
}

export async function createSavedAddress(request: SavedAddressRequest): Promise<SavedAddress> {
  return requestData(
    () => api.post<SavedAddress>("/users/addresses", request),
    "Unable to save delivery address."
  );
}

export async function updateSavedAddress(addressId: string, request: SavedAddressRequest): Promise<SavedAddress> {
  return requestData(
    () => api.put<SavedAddress>(`/users/addresses/${addressId}`, request),
    "Unable to update delivery address."
  );
}

export async function deleteSavedAddress(addressId: string): Promise<{ message: string }> {
  return requestData(
    () => api.delete<{ message: string }>(`/users/addresses/${addressId}`),
    "Unable to delete delivery address."
  );
}

export async function setDefaultAddress(addressId: string): Promise<SavedAddress> {
  return requestData(
    () => api.put<SavedAddress>(`/users/addresses/${addressId}/default`, {}),
    "Unable to set default address."
  );
}

