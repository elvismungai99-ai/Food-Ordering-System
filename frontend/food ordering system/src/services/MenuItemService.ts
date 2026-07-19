import api from "../api/axios";

export interface MenuItem {
  id: string;
  restaurantId: string;
  name: string;
  description: string;
  price: number;
  category: string;
  available: boolean;
  imageUrl?: string | null;
}

export interface MenuItemRequest {
  name: string;
  description?: string;
  price: number;
  category?: string;
  available: boolean;
  imageUrl?: string | null;
}

/*
 * Customer:
 * Get menu items for a specific restaurant.
 */
export const getMenuByRestaurant = async (
  restaurantId: string
): Promise<MenuItem[]> => {
  const response = await api.get<MenuItem[]>(
    `/menu-items/restaurant/${restaurantId}`
  );

  return response.data;
};

/*
 * Restaurant owner:
 * Get their restaurant menu.
 *
 * This currently uses the same backend endpoint
 * when a restaurantId is supplied.
 */
export const getRestaurantMenu = async (
  restaurantId: string
): Promise<MenuItem[]> => {
  const response = await api.get<MenuItem[]>(
    `/menu-items/restaurant/${restaurantId}`
  );

  return response.data;
};

/*
 * Restaurant owner:
 * Create a menu item.
 */
export const createMenuItem = async (
  menuItem: MenuItemRequest
): Promise<MenuItem> => {
  const response = await api.post<MenuItem>(
    "/menu-items",
    menuItem
  );

  return response.data;
};

/*
 * Restaurant owner:
 * Update a menu item.
 */
export const updateMenuItem = async (
  menuItemId: string,
  menuItem: Partial<MenuItemRequest>
): Promise<MenuItem> => {
  const response = await api.put<MenuItem>(
    `/menu-items/${menuItemId}`,
    menuItem
  );

  return response.data;
};

/*
 * Restaurant owner:
 * Delete a menu item.
 */
export const deleteMenuItem = async (
  menuItemId: string
): Promise<void> => {
  await api.delete(
    `/menu-items/${menuItemId}`
  );
};