import api from "../api/axios";

export interface Restaurant {
  id: string;
  name: string;
  description: string;
  address: string;
  openingTime: string;
  closingTime: string;
  status: string;
  category: string;
}

export interface MenuItem {
  id: string;
  restaurantId: string;
  name: string;
  description: string;
  price: number;
  category: string;
  available: boolean;
  imageUrl?: string;
}

export const getRestaurantById = async (
  restaurantId: string
): Promise<Restaurant> => {
  const response = await api.get<Restaurant>(
    `/restaurants/${restaurantId}`
  );

  return response.data;
};

export const getRestaurantMenu = async (
  restaurantId: string
): Promise<MenuItem[]> => {
  const response = await api.get<MenuItem[]>(
    `/menu-items/restaurant/${restaurantId}`
  );

  return response.data;
};