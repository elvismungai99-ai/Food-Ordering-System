import api from "../api/axios";
import {
  requestData,
  requestVoid,
} from "./request";

export interface MenuItem {
  id: string;
  restaurantId: string;

  name: string;
  description: string;
  price: number;
  category: string;
  addOns?: string[];
  available: boolean;
  averageRating?: number;
  reviewCount?: number;

  imageUrl?: string | null;
}

export interface MenuItemRequest {
  name: string;
  description?: string;
  price: number;
  category?: string;
  addOns?: string[];
  available: boolean;

  /*
   * Value entered by the restaurant admin.
   */
  imageUrl?: string | null;
}

/*
 * Customer:
 * Get the menu belonging to a restaurant.
 */
export async function getMenuByRestaurant(
  restaurantId: string
): Promise<MenuItem[]> {
  return requestData(
    () => api.get<MenuItem[]>(
      `/menu-items/restaurant/${restaurantId}`
    ),
    "Unable to load the menu."
  );
}

/*
 * Restaurant owner:
 * Load their restaurant menu.
 */
export async function getRestaurantMenu(
  restaurantId: string
): Promise<MenuItem[]> {
  return requestData(
    () => api.get<MenuItem[]>(
      `/menu-items/restaurant/${restaurantId}`
    ),
    "Unable to load your menu."
  );
}

/*
 * Restaurant owner:
 * Create a new menu item.
 */
export async function createMenuItem(
  request: MenuItemRequest
): Promise<MenuItem> {
  return requestData(
    () => api.post<MenuItem>(
      "/menu-items",
      request
    ),
    "Unable to create the menu item."
  );
}

/*
 * Restaurant owner:
 * Edit an existing menu item.
 */
export async function updateMenuItem(
  menuItemId: string,
  request: MenuItemRequest
): Promise<MenuItem> {
  return requestData(
    () => api.put<MenuItem>(
      `/menu-items/${menuItemId}`,
      request
    ),
    "Unable to update the menu item."
  );
}

/*
 * Restaurant owner:
 * Delete an existing menu item.
 */
export async function deleteMenuItem(
  menuItemId: string
): Promise<void> {
  await requestVoid(
    () => api.delete(
      `/menu-items/${menuItemId}`
    ),
    "Unable to delete the menu item."
  );
}

/*
 * Restaurant owner:
 * Toggle availability of an existing menu item.
 */
export async function toggleMenuItemAvailability(
  menuItemId: string,
  available?: boolean
): Promise<MenuItem> {
  return requestData(
    () =>
      api.patch<MenuItem>(
        `/menu-items/${menuItemId}/availability${
          available !== undefined ? `?available=${available}` : ""
        }`
      ),
    "Unable to toggle item availability."
  );
}

