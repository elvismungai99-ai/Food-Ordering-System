import api from "../api/axios";
import {
  requestData,
} from "./request";

export interface Review {
  id: string;
  orderId: string;
  customerId: string;
  restaurantId: string;
  menuItemId?: string | null;
  rating: number;
  comment?: string | null;
  createdAt: string;
}

export interface CreateReviewRequest {
  orderId: string;
  menuItemId?: string | null;
  rating: number;
  comment?: string;
}

export async function createReview(
  request: CreateReviewRequest
): Promise<Review> {
  return requestData(
    () => api.post<Review>(
      "/reviews",
      request
    ),
    "Unable to submit your review."
  );
}

export async function getRestaurantReviews(
  restaurantId: string
): Promise<Review[]> {
  return requestData(
    () => api.get<Review[]>(
      `/reviews/restaurant/${restaurantId}`
    ),
    "Unable to load restaurant reviews."
  );
}

export async function getMenuItemReviews(
  menuItemId: string
): Promise<Review[]> {
  return requestData(
    () => api.get<Review[]>(
      `/reviews/menu-item/${menuItemId}`
    ),
    "Unable to load menu item reviews."
  );
}
