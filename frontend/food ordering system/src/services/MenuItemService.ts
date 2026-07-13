import api from "../api/axios";

export interface MenuItem {
  id: string;
  restaurantId: string;
  name: string;
  description?: string | null;
  price: number;
  category?: string | null;
  available: boolean;
  imageUrl?: string | null;
}

export interface CreateMenuItemRequest {
  name: string;
  description?: string | null;
  price: number;
  category?: string | null;
  available: boolean;
  imageUrl?: string | null;
}

export interface UpdateMenuItemRequest {
  name?: string;
  description?: string | null;
  price?: number;
  category?: string | null;
  available?: boolean;
  imageUrl?: string | null;
}

export async function getRestaurantMenu(
  restaurantId: string
): Promise<MenuItem[]> {
  const response = await api.get<MenuItem[]>(
    `/menu-items/restaurant/${restaurantId}`
  );

  return response.data;
}

export async function createMenuItem(
  request: CreateMenuItemRequest
): Promise<MenuItem> {
  const response = await api.post<MenuItem>(
    "/menu-items",
    request
  );

  return response.data;
}

export async function updateMenuItem(
  menuItemId: string,
  request: UpdateMenuItemRequest
): Promise<MenuItem> {
  const response = await api.put<MenuItem>(
    `/menu-items/${menuItemId}`,
    request
  );

  return response.data;
}

export async function deleteMenuItem(
  menuItemId: string
): Promise<void> {
  await api.delete(
    `/menu-items/${menuItemId}`
  );
}