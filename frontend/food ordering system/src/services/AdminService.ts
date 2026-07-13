import api from "../api/axios";

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

const authHeader = () => ({
  headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
});

export const getAllCustomers = async (): Promise<Customer[]> => {
  const response = await api.get("/admin/customers", authHeader());
  return response.data;
};

export const deleteCustomer = async (id: string): Promise<void> => {
  await api.delete(`/admin/customers/${id}`, authHeader());
};

export const getAllRestaurants = async (): Promise<Restaurant[]> => {
  const response = await api.get("/admin/restaurants", authHeader());
  return response.data;
};

export const deleteRestaurant = async (id: string): Promise<void> => {
  await api.delete(`/admin/restaurants/${id}`, authHeader());
};