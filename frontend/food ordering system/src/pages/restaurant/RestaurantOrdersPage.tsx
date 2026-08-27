import {
  useCallback,
  useEffect,
  useState,
} from "react";

import {
  useNavigate,
} from "react-router-dom";

import axios from "axios";

import {
  getRestaurantOrders,
  cancelRestaurantOrder,
  updateOrderStatus,
  type Order,
  type OrderStatus,
} from "../../services/OrderService";

import {
  getMyRestaurant,
} from "../../services/RestaurantService";

import {
  buildGoogleMapsPlaceUrl,
} from "../../utils/location";

import {
  createAutomaticDeliveryRequest,
  createDeliveryRequest,
  getAvailableRiders,
  getRestaurantDeliveryRequests,
  type DeliveryRequest,
  type Rider,
} from "../../services/RiderService";
import CancellationModal from "../../components/common/CancellationModal";

const activeDeliveryStatuses = new Set([
  "REQUESTED",
  "ACCEPTED",
  "ARRIVED_AT_RESTAURANT",
  "PICKED_UP",
]);

const getCurrentRestaurantPosition = () =>
  new Promise<GeolocationPosition>(
    (resolve, reject) => {
      if (!navigator.geolocation) {
        reject(
          new Error(
            "Location sharing is not supported by this browser."
          )
        );
        return;
      }

      navigator.geolocation.getCurrentPosition(
        resolve,
        () => {
          reject(
            new Error(
              "Allow location access so the restaurant location can be shared with the rider."
            )
          );
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 60000,
        }
      );
    }
  );

function RestaurantOrdersPage() {
  const navigate = useNavigate();

  const [orders, setOrders] =
    useState<Order[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState("");

  const [
    updatingOrderId,
    setUpdatingOrderId,
  ] = useState<string | null>(null);

  const [
    availableRiders,
    setAvailableRiders,
  ] = useState<Rider[]>([]);

  const [
    selectedRiders,
    setSelectedRiders,
  ] = useState<Record<string, string>>({});

  const [
    dispatchingOrderId,
    setDispatchingOrderId,
  ] = useState<string | null>(null);

  const [
    deliveryRequests,
    setDeliveryRequests,
  ] = useState<DeliveryRequest[]>([]);

  const [
    cancellingOrderId,
    setCancellingOrderId,
  ] = useState<string | null>(null);

  const [
    cancelling,
    setCancelling,
  ] = useState(false);

  const loadOrders =
    useCallback(async () => {
      try {
        setLoading(true);
        setError("");

        let restaurantId =
          localStorage.getItem(
            "restaurantId"
          );

        if (!restaurantId) {
          const restaurant =
            await getMyRestaurant();

          restaurantId =
            restaurant.id;

          localStorage.setItem(
            "restaurantId",
            restaurant.id
          );
        }

        const data =
          await getRestaurantOrders(
            restaurantId
          );

        const riders =
          await getAvailableRiders();

        const requests =
          await getRestaurantDeliveryRequests();

        setOrders(data);
        setAvailableRiders(riders);
        setDeliveryRequests(requests);

      } catch (requestError) {
        console.error(
          "Failed to load restaurant orders:",
          requestError
        );

        setError(
          "Unable to load restaurant orders."
        );

      } finally {
        setLoading(false);
      }

    }, []);

  useEffect(() => {
    loadOrders();
  }, [loadOrders]);

  const getNextStatus = (
    status: OrderStatus
  ): OrderStatus | null => {
    switch (status) {
      case "PENDING":
        return "CONFIRMED";

      case "CONFIRMED":
        return "PREPARING";

      case "PREPARING":
        return "READY_FOR_PICKUP";

      case "READY_FOR_PICKUP":
        return "OUT_FOR_DELIVERY";

      case "OUT_FOR_DELIVERY":
        return "DELIVERED";

      default:
        return null;
    }
  };

  const getActionLabel = (
    status: OrderStatus
  ): string | null => {
    switch (status) {
      case "PENDING":
        return "Confirm Order";

      case "CONFIRMED":
        return "Start Preparing";

      case "PREPARING":
        return "Mark Ready";

      case "READY_FOR_PICKUP":
        return "Mark Out for Delivery";

      case "OUT_FOR_DELIVERY":
        return "Mark Delivered";

      default:
        return null;
    }
  };

  const handleStatusUpdate =
    async (
      order: Order
    ) => {
      const nextStatus =
        getNextStatus(
          order.status
        );

      if (!nextStatus) {
        return;
      }

      try {
        setUpdatingOrderId(
          order.id
        );

        setError("");

        const updatedOrder =
          await updateOrderStatus(
            order.id,
            nextStatus
          );

        setOrders(
          currentOrders =>
            currentOrders.map(
              currentOrder =>
                currentOrder.id
                  === updatedOrder.id
                  ? updatedOrder
                  : currentOrder
            )
        );

      } catch (requestError) {
        console.error(
          "Failed to update order status:",
          requestError
        );

        if (
          axios.isAxiosError(
            requestError
          )
        ) {
          const responseData =
            requestError.response?.data;

          if (
            typeof responseData
            === "string"
          ) {
            setError(
              responseData
            );
          } else {
            setError(
              "Unable to update order status."
            );
          }

        } else {
          setError(
            "Unable to update order status."
          );
        }

      } finally {
        setUpdatingOrderId(null);
      }
    };

  const handleConfirmRestaurantCancellation = async (reason: string) => {
    if (!cancellingOrderId) return;
    try {
      setCancelling(true);
      setError("");
      const updated = await cancelRestaurantOrder(cancellingOrderId, reason);
      setOrders((current) =>
        current.map((o) => (o.id === cancellingOrderId ? updated : o))
      );
      setCancellingOrderId(null);
    } catch (requestError) {
      console.error("Failed to cancel order:", requestError);
      setError("Unable to cancel / reject this order.");
    } finally {
      setCancelling(false);
    }
  };

  const handleDispatchRider =
    async (
      order: Order
    ) => {
      const riderId =
        selectedRiders[order.id];

      if (!riderId) {
        setError(
          "Select an available rider before sending the delivery request."
        );
        return;
      }

      const estimatedPayout =
        window.prompt(
          "Enter estimated payout, or leave blank to calculate automatically"
        );

      try {
        setDispatchingOrderId(order.id);
        setError("");

        const position =
          await getCurrentRestaurantPosition();

        const deliveryRequest =
          await createDeliveryRequest({
            orderId: order.id,
            riderId,
            restaurantLatitude:
              position.coords.latitude,
            restaurantLongitude:
              position.coords.longitude,
            estimatedPayout:
              estimatedPayout?.trim()
                ? Number(estimatedPayout)
                : null,
          });

        setDeliveryRequests(currentRequests => [
          deliveryRequest,
          ...currentRequests.filter(
            currentRequest =>
              currentRequest.id
              !== deliveryRequest.id
          ),
        ]);

        setError("");
      } catch (requestError) {
        console.error(
          "Failed to send delivery request:",
          requestError
        );
        setError(
          requestError instanceof Error
            ? requestError.message
            : "Unable to send delivery request."
        );
      } finally {
        setDispatchingOrderId(null);
      }
    };

  const handleAutoDispatchRider =
    async (
      order: Order
    ) => {
      const estimatedPayout =
        window.prompt(
          "Enter estimated payout, or leave blank to calculate automatically"
        );

      try {
        setDispatchingOrderId(order.id);
        setError("");

        const position =
          await getCurrentRestaurantPosition();

        const deliveryRequest =
          await createAutomaticDeliveryRequest({
            orderId: order.id,
            restaurantLatitude:
              position.coords.latitude,
            restaurantLongitude:
              position.coords.longitude,
            estimatedPayout:
              estimatedPayout?.trim()
                ? Number(estimatedPayout)
                : null,
          });

        setDeliveryRequests(currentRequests => [
          deliveryRequest,
          ...currentRequests.filter(
            currentRequest =>
              currentRequest.id
              !== deliveryRequest.id
          ),
        ]);

        setError("");
      } catch (requestError) {
        console.error(
          "Failed to assign rider automatically:",
          requestError
        );
        setError(
          requestError instanceof Error
            ? requestError.message
            : "Unable to assign rider automatically."
        );
      } finally {
        setDispatchingOrderId(null);
      }
    };

  const getActiveDeliveryRequest = (
    orderId: string
  ) =>
    deliveryRequests.find(
      request =>
        request.orderId === orderId
        && activeDeliveryStatuses.has(
          request.status
        )
    );

  const handleCancelOrder =
    async (
      order: Order
    ) => {
      const reason =
        window.prompt(
          "Enter the cancellation reason"
        );

      if (!reason?.trim()) {
        return;
      }

      try {
        setUpdatingOrderId(order.id);
        setError("");

        const updatedOrder =
          await cancelRestaurantOrder(
            order.id,
            reason.trim()
          );

        setOrders(currentOrders =>
          currentOrders.map(currentOrder =>
            currentOrder.id === updatedOrder.id
              ? updatedOrder
              : currentOrder
          )
        );
      } catch (requestError) {
        console.error(
          "Failed to cancel order:",
          requestError
        );

        setError(
          "Unable to cancel this order."
        );
      } finally {
        setUpdatingOrderId(null);
      }
    };

  const formatPrice = (
    amount: number
  ) => {
    return new Intl.NumberFormat(
      "en-KE",
      {
        style: "currency",
        currency: "KES",
      }
    ).format(amount);
  };

  const formatStatus = (
    status: string
  ) => {
    return status.replaceAll(
      "_",
      " "
    );
  };

  const getMapUrl = (
    order: Order
  ) => {
    if (
      order.deliveryLatitude == null
      || order.deliveryLongitude == null
    ) {
      return null;
    }

    return buildGoogleMapsPlaceUrl(
      order.deliveryLatitude,
      order.deliveryLongitude
    );
  };

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-100">
        <p className="text-slate-500">
          Loading orders...
        </p>
      </div>
    );
  }

  return (
    <main className="min-h-screen bg-slate-100 p-6 md:p-8">

      <div className="mx-auto max-w-6xl">

        <div className="mb-8 flex flex-wrap items-center justify-between gap-4">

          <div>
            <h1 className="text-3xl font-bold text-slate-950">
              Restaurant Orders
            </h1>

            <p className="mt-2 text-slate-500">
              Manage incoming orders and update their progress.
            </p>
          </div>

          <button
            type="button"
            onClick={() =>
              navigate(
                "/restaurant/dashboard"
              )
            }
            className="rounded-3xl border border-slate-300 bg-white px-5 py-2 text-sm font-semibold text-slate-700"
          >
            ← Dashboard
          </button>

        </div>

        {error && (
          <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}

        {orders.length === 0 ? (
          <div className="rounded-[24px] border border-slate-200 bg-white p-10 text-center">
            <p className="text-slate-500">
              No orders have been placed yet.
            </p>
          </div>
        ) : (
          <div className="space-y-6">

            {orders.map(
              order => {
                const nextStatus =
                  getNextStatus(
                    order.status
                  );

                const actionLabel =
                  getActionLabel(
                    order.status
                  );

                const mapUrl =
                  getMapUrl(order);

                const activeDeliveryRequest =
                  getActiveDeliveryRequest(
                    order.id
                  );

                return (
                  <article
                    key={order.id}
                    className="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm"
                  >

                    <div className="flex flex-wrap items-start justify-between gap-4">

                      <div>
                        <p className="text-xs uppercase tracking-wide text-slate-400">
                          Order
                        </p>

                        <p className="mt-1 font-semibold text-slate-900">
                          #
                          {order.id.slice(
                            0,
                            8
                          )}
                        </p>
                      </div>

                      <span className="rounded-full bg-indigo-100 px-4 py-2 text-sm font-semibold text-indigo-700">
                        {formatStatus(
                          order.status
                        )}
                      </span>

                    </div>

                    <div className="mt-6 grid gap-5 sm:grid-cols-2 lg:grid-cols-4">

                      <div>
                        <p className="text-xs text-slate-400">
                          Total
                        </p>

                        <p className="mt-1 font-semibold text-slate-900">
                          {formatPrice(
                            order.totalAmount
                          )}
                        </p>
                      </div>

                      <div>
                        <p className="text-xs text-slate-400">
                          Payment
                        </p>

                        <p className="mt-1 font-semibold text-slate-900">
                          {order.paymentStatus}
                        </p>
                      </div>

                      <div>
                        <p className="text-xs text-slate-400">
                          Delivery Address
                        </p>

                        <p className="mt-1 font-medium text-slate-700">
                          {
                            order.deliveryAddress
                          }
                        </p>

                        {mapUrl && (
                          <a
                            href={mapUrl}
                            target="_blank"
                            rel="noreferrer"
                            className="mt-2 inline-flex text-sm font-semibold text-indigo-600 hover:text-indigo-700"
                          >
                            Open map
                          </a>
                        )}
                      </div>

                      <div>
                        <p className="text-xs text-slate-400">
                          Items
                        </p>

                        <p className="mt-1 font-semibold text-slate-900">
                          {
                            order.items.reduce(
                              (
                                total,
                                item
                              ) =>
                                total
                                + item.quantity,
                              0
                            )
                          }
                        </p>
                      </div>

                    </div>

                    <div className="mt-6 border-t border-slate-200 pt-5">
                      <h2 className="font-semibold text-slate-900">
                        Order Items & Customizations
                      </h2>

                      <div className="mt-3 space-y-3">
                        {order.items.map((item) => (
                          <div
                            key={item.id}
                            className="rounded-xl bg-slate-50 p-3 border border-slate-100 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 text-sm"
                          >
                            <div>
                              <div className="font-semibold text-slate-900">
                                {item.quantity}× {item.itemName}
                              </div>

                              <div className="mt-1 flex flex-wrap gap-1.5">
                                {item.selectedSize && (
                                  <span className="rounded bg-indigo-50 px-2 py-0.5 text-xs font-semibold text-indigo-700">
                                    Size: {item.selectedSize}
                                  </span>
                                )}
                                {item.selectedAddOns && item.selectedAddOns.length > 0 && (
                                  <span className="rounded bg-emerald-50 px-2 py-0.5 text-xs font-semibold text-emerald-700">
                                    +{item.selectedAddOns.join(", ")}
                                  </span>
                                )}
                                {item.removalRequests && item.removalRequests.length > 0 && (
                                  <span className="rounded bg-rose-50 px-2 py-0.5 text-xs font-semibold text-rose-700">
                                    {item.removalRequests.join(", ")}
                                  </span>
                                )}
                              </div>

                              {item.specialInstructions && (
                                <p className="mt-1 text-xs italic text-amber-800 font-medium">
                                  Kitchen Note: "{item.specialInstructions}"
                                </p>
                              )}
                            </div>

                            <span className="font-bold text-slate-900 whitespace-nowrap">
                              {formatPrice(item.subtotal)}
                            </span>
                          </div>
                        ))}
                      </div>
                    </div>

                    {nextStatus &&
                      actionLabel && (
                        <div className="mt-6 flex flex-wrap gap-3">

                          <button
                            type="button"
                            disabled={
                              updatingOrderId
                                === order.id
                            }
                            onClick={() =>
                              handleStatusUpdate(
                                order
                              )
                            }
                            className="rounded-3xl bg-indigo-600 px-6 py-3 text-sm font-semibold text-white hover:bg-indigo-700 disabled:cursor-not-allowed disabled:bg-slate-300"
                          >
                            {updatingOrderId
                              === order.id
                              ? "Updating..."
                              : actionLabel}
                          </button>

                          {(order.status === "PENDING"
                            || order.status === "CONFIRMED"
                            || order.status === "PREPARING") && (
                            <button
                              type="button"
                              disabled={
                                updatingOrderId === order.id || cancelling
                              }
                              onClick={() => setCancellingOrderId(order.id)}
                              className="rounded-3xl border border-red-300 px-6 py-3 text-sm font-semibold text-red-600 hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-50 transition"
                            >
                              Reject / Cancel Order
                            </button>
                          )}

                        </div>
                      )}

                    {order.status ===
                      "READY_FOR_PICKUP" && (
                      <div className="mt-6 rounded-2xl border border-slate-200 bg-slate-50 p-4">
                        <h3 className="font-semibold text-slate-900">
                          Dispatch Rider
                        </h3>

                        <p className="mt-1 text-sm text-slate-500">
                          Auto assignment shares this restaurant device location with the assigned rider.
                        </p>

                        {activeDeliveryRequest ? (
                          <div className="mt-3 rounded-2xl border border-indigo-100 bg-white px-4 py-3 text-sm text-slate-700">
                            Delivery request already sent.
                            {" "}
                            Status:
                            {" "}
                            <span className="font-semibold text-indigo-700">
                              {formatStatus(
                                activeDeliveryRequest.status
                              )}
                            </span>
                          </div>
                        ) : (
                          <div className="mt-3 flex flex-col gap-3 lg:flex-row">
                            <button
                              type="button"
                              disabled={
                                dispatchingOrderId
                                === order.id
                              }
                              onClick={() =>
                                handleAutoDispatchRider(
                                  order
                                )
                              }
                              className="rounded-3xl bg-indigo-600 px-5 py-3 text-sm font-semibold text-white disabled:bg-slate-300"
                            >
                              {dispatchingOrderId
                                === order.id
                                ? "Assigning..."
                                : "Auto Assign Rider"}
                            </button>

                            <select
                              value={
                                selectedRiders[order.id]
                                || ""
                              }
                              onChange={event =>
                                setSelectedRiders(
                                  current => ({
                                    ...current,
                                    [order.id]:
                                      event.target.value,
                                  })
                                )
                              }
                              className="min-w-0 flex-1 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm"
                            >
                              <option value="">
                                Select available rider
                              </option>
                              {availableRiders.map(rider => (
                                <option
                                  key={rider.id}
                                  value={rider.id}
                                >
                                  {rider.fullName} - {rider.vehicleType} - {rider.licencePlate}
                                </option>
                              ))}
                            </select>

                            <button
                              type="button"
                              disabled={
                                dispatchingOrderId
                                === order.id
                              }
                              onClick={() =>
                                handleDispatchRider(
                                  order
                                )
                              }
                              className="rounded-3xl bg-slate-900 px-5 py-3 text-sm font-semibold text-white disabled:bg-slate-300"
                            >
                              {dispatchingOrderId
                                === order.id
                                ? "Sending..."
                                : "Send Request"}
                            </button>
                          </div>
                        )}
                      </div>
                    )}

                    {order.status ===
                      "DELIVERED" && (
                      <div className="mt-6 rounded-2xl bg-green-50 px-4 py-3 text-sm font-semibold text-green-700">
                        Order completed successfully.
                      </div>
                    )}

                    {order.status ===
                      "CANCELLED" && (
                      <div className="mt-6 rounded-2xl bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
                        This order was cancelled.
                        {order.cancellationReason
                          ? ` Reason: ${order.cancellationReason}`
                          : ""}
                      </div>
                    )}

                  </article>
                );
              }
            )}

          </div>
        )}

        {/* Restaurant Order Cancellation / Rejection Modal */}
        <CancellationModal
          isOpen={cancellingOrderId !== null}
          isSubmitting={cancelling}
          title="Reject / Cancel Order"
          description="Select the reason for rejecting or cancelling this customer order."
          confirmLabel="Reject Order"
          presetReasons={[
            "Kitchen is at maximum capacity",
            "Out of stock item(s)",
            "Restaurant closing early",
            "Customer requested order cancellation",
            "Delivery address out of delivery radius",
          ]}
          onClose={() => setCancellingOrderId(null)}
          onConfirm={handleConfirmRestaurantCancellation}
        />
      </div>

    </main>
  );
}

export default RestaurantOrdersPage;

