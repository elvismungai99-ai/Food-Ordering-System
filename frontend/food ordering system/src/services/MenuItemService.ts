import api from "../api/axios";

export interface MenuItem {
  id: string;
  restaurantId: string;
  name: string;
  description: string;
  price: number;
  category: string;
  available: boolean;
  imageUrl: string;
}

const authHeader = () => ({
  headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
});

export const getMenuByRestaurant = async (restaurantId: string): Promise<MenuItem[]> => {
  const response = await api.get(`/menu-items/restaurant/${restaurantId}`);
  return response.data;
};

export const createMenuItem = async (item: Partial<MenuItem>): Promise<MenuItem> => {
  const response = await api.post("/menu-items", item, authHeader());
  return response.data;
};

export const updateMenuItem = async (id: string, item: Partial<MenuItem>): Promise<MenuItem> => {
  const response = await api.put(`/menu-items/${id}`, item, authHeader());
  return response.data;
};

export const deleteMenuItem = async (id: string): Promise<void> => {
  await api.delete(`/menu-items/${id}`, authHeader());
};