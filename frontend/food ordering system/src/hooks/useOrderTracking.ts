import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";

import {
  getOrderTracking,
  type Order,
  type OrderStatus,
  type OrderTracking,
} from "../services/OrderService";

/*
 * Order statuses during which live
 * tracking is worth polling.
 */
const ACTIVE_STATUSES: OrderStatus[] = [
  "CONFIRMED",
  "PREPARING",
  "READY_FOR_PICKUP",
  "OUT_FOR_DELIVERY",
];

/*
 * How often the tracking snapshot is
 * refreshed while the order is active.
 */
const POLL_INTERVAL_MS = 10_000;

export function useOrderTracking(
  order: Order | null
) {
  const [
    tracking,
    setTracking,
  ] = useState<OrderTracking | null>(
    null
  );

  const [
    error,
    setError,
  ] = useState("");

  const [
    isRefreshing,
    setIsRefreshing,
  ] = useState(false);

  const inFlightRef =
    useRef(false);

  const orderId =
    order?.id ?? null;

  const isActive =
    order != null
    && ACTIVE_STATUSES.includes(
      order.status
    );

  const refresh =
    useCallback(async () => {

      if (!orderId || inFlightRef.current) {
        return;
      }

      inFlightRef.current = true;

      try {
        setIsRefreshing(true);

        const data =
          await getOrderTracking(
            orderId
          );

        setTracking(data);
        setError("");

      } catch (requestError) {
        console.error(
          "Failed to refresh tracking:",
          requestError
        );

        setError(
          "Live tracking is temporarily unavailable."
        );

      } finally {
        inFlightRef.current = false;
        setIsRefreshing(false);
      }
    }, [orderId]);

  useEffect(() => {

    if (!isActive || !orderId) {
      setTracking(null);
      setError("");
      return;
    }

    void refresh();

    const intervalId =
      window.setInterval(
        () => void refresh(),
        POLL_INTERVAL_MS
      );

    return () => {
      window.clearInterval(intervalId);
    };

  }, [isActive, orderId, refresh]);

  return {
    tracking,
    trackingError: error,
    isRefreshing,
    refresh,
  };
}
