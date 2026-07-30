import api from "../api/axios";
import {
  requestData,
} from "./request";

export interface Restaurant {
  id: string;
  name: string;
  description: string;
  address: string;
  openingTime: string;
  closingTime: string;
  status: string;
  category: string;
  openNow?: boolean;
  averageRating?: number;
  reviewCount?: number;
}

export interface RestaurantRequest {
  name: string;
  description?: string;
  address: string;
  openingTime: string;
  closingTime: string;
  category?: string;
}

export interface MenuItem {
  id: string;
  restaurantId: string;
  name: string;
  description: string;
  price: number;
  category: string;
  addOns?: string[];
  available: boolean;
  imageUrl?: string;
  averageRating?: number;
  reviewCount?: number;
}

export const getRestaurantById = async (
  restaurantId: string
): Promise<Restaurant> => {
  return requestData(
    () => api.get<Restaurant>(
      `/restaurants/${restaurantId}`
    ),
    "Unable to load restaurant details."
  );
};

export const getMyRestaurant = async (): Promise<Restaurant> => {
  return requestData(
    () => api.get<Restaurant>(
      "/restaurants/me"
    ),
    "Unable to load your restaurant details."
  );
};

export const createRestaurant = async (
  restaurant: RestaurantRequest
): Promise<Restaurant> => {
  return requestData(
    () => api.post<Restaurant>(
      "/restaurants",
      restaurant
    ),
    "Unable to submit your restaurant for approval."
  );
};

export const getRestaurantMenu = async (
  restaurantId: string
): Promise<MenuItem[]> => {
  return requestData(
    () => api.get<MenuItem[]>(
      `/menu-items/restaurant/${restaurantId}`
    ),
    "Unable to load the restaurant menu."
  );
};
