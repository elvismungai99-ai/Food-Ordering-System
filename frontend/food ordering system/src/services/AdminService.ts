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
  ownerId: string;
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
