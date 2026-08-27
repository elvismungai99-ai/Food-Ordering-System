import api from "../api/axios";
import {
  requestData,
} from "./request";

export interface DailySales {
  date: string;
  sales: number;
  orderCount: number;
}

export interface PopularMenuItem {
  menuItemId: string;
  itemName: string;
  quantitySold: number;
  revenue: number;
}

export interface RestaurantAnalytics {
  restaurantId: string;
  restaurantName: string;
  totalSales: number;
  todaySales: number;
  averageOrderValue: number;
  totalOrders: number;
  completedOrders: number;
  activeOrders: number;
  cancelledOrders: number;
  cancellationRate: number;
  averagePrepTimeMinutes?: number;
  averageDeliveryTimeMinutes?: number;
  fulfillmentRate?: number;
  dailySales: DailySales[];
  popularMenuItems: PopularMenuItem[];
}

export async function getRestaurantAnalytics():
Promise<RestaurantAnalytics> {
  return requestData(
    () => api.get<RestaurantAnalytics>(
      "/analytics/restaurant"
    ),
    "Unable to load restaurant analytics."
  );
}
