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
  updateOrderStatus,
  type Order,
  type OrderStatus,
} from "../../services/OrderService";

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

  const restaurantId =
    localStorage.getItem(
      "restaurantId"
    );

  const loadOrders =
    useCallback(async () => {
      if (!restaurantId) {
        setError(
          "Restaurant ID was not found."
        );

        setLoading(false);

        return;
      }

      try {
        setLoading(true);
        setError("");

        const data =
          await getRestaurantOrders(
            restaurantId
          );

        setOrders(data);

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

    }, [restaurantId]);

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
    status: OrderStatus
  ) => {
    return status.replaceAll(
      "_",
      " "
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
                        Order Items
                      </h2>

                      <div className="mt-3 space-y-2">

                        {order.items.map(
                          item => (
                            <div
                              key={item.id}
                              className="flex justify-between gap-4 text-sm"
                            >

                              <span className="text-slate-600">
                                {item.itemName}
                                {" × "}
                                {item.quantity}
                              </span>

                              <span className="font-medium text-slate-900">
                                {formatPrice(
                                  item.subtotal
                                )}
                              </span>

                            </div>
                          )
                        )}

                      </div>

                    </div>

                    {nextStatus &&
                      actionLabel && (
                        <div className="mt-6">

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
                      </div>
                    )}

                  </article>
                );
              }
            )}

          </div>
        )}

      </div>

    </main>
  );
}

export default RestaurantOrdersPage;