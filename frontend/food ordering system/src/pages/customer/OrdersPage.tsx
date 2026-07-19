import {
  useCallback,
  useEffect,
  useState,
} from "react";

import {
  useNavigate,
} from "react-router-dom";

import {
  getCustomerOrders,
  type Order,
  type OrderStatus,
} from "../../services/OrderService";

function OrdersPage() {
  const navigate =
    useNavigate();

  const [
    orders,
    setOrders,
  ] = useState<Order[]>([]);

  const [
    loading,
    setLoading,
  ] = useState(true);

  const [
    error,
    setError,
  ] = useState("");

  /*
   * Load the authenticated customer's
   * real order history.
   */
  const loadOrders =
    useCallback(async () => {
      try {
        setError("");

        const data =
          await getCustomerOrders();

        setOrders(data);

      } catch (requestError) {
        console.error(
          "Failed to load customer orders:",
          requestError
        );

        setError(
          "Unable to load your orders."
        );

      } finally {
        setLoading(false);
      }
    }, []);

  /*
   * Load orders immediately.
   *
   * Poll every 10 seconds so the customer
   * can see state-machine changes made by
   * the restaurant admin without manually
   * refreshing the browser.
   */
  useEffect(() => {
    loadOrders();

    const intervalId =
      window.setInterval(
        loadOrders,
        10000
      );

    return () => {
      window.clearInterval(
        intervalId
      );
    };

  }, [loadOrders]);

  /*
   * Format money in Kenyan Shillings.
   */
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

  /*
   * Convert:
   *
   * READY_FOR_PICKUP
   *
   * into:
   *
   * READY FOR PICKUP
   */
  const formatStatus = (
    status: OrderStatus
  ) => {
    return status.replaceAll(
      "_",
      " "
    );
  };

  /*
   * Format order creation date.
   */
  const formatDate = (
    date: string
  ) => {
    return new Date(
      date
    ).toLocaleString(
      "en-KE",
      {
        dateStyle: "medium",
        timeStyle: "short",
      }
    );
  };

  /*
   * Status badge styling.
   *
   * This does not change the order status.
   * It only displays the current backend state.
   */
  const getStatusBadgeClass = (
    status: OrderStatus
  ) => {
    switch (status) {
      case "PENDING":
        return "bg-amber-100 text-amber-700";

      case "CONFIRMED":
        return "bg-blue-100 text-blue-700";

      case "PREPARING":
        return "bg-orange-100 text-orange-700";

      case "READY_FOR_PICKUP":
        return "bg-purple-100 text-purple-700";

      case "OUT_FOR_DELIVERY":
        return "bg-indigo-100 text-indigo-700";

      case "DELIVERED":
        return "bg-green-100 text-green-700";

      case "CANCELLED":
        return "bg-red-100 text-red-700";

      default:
        return "bg-slate-100 text-slate-700";
    }
  };

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-100">
        <p className="text-slate-500">
          Loading your orders...
        </p>
      </div>
    );
  }

  return (
    <main className="min-h-screen bg-slate-100 p-6 md:p-8">

      <div className="mx-auto max-w-6xl">

        {/* ============================= */}
        {/* PAGE HEADER */}
        {/* ============================= */}

        <div className="mb-8 flex flex-wrap items-center justify-between gap-4">

          <div>

            <h1 className="text-3xl font-bold text-slate-950">
              My Orders
            </h1>

            <p className="mt-2 text-slate-500">
              View your order history and track current orders.
            </p>

          </div>


          <button
            type="button"
            onClick={() =>
              navigate(
                "/customer/dashboard"
              )
            }
            className="rounded-3xl border border-slate-300 bg-white px-5 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
          >
            ← Dashboard
          </button>

        </div>


        {/* ============================= */}
        {/* ERROR MESSAGE */}
        {/* ============================= */}

        {error && (

          <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">

            {error}

          </div>

        )}


        {/* ============================= */}
        {/* EMPTY ORDER HISTORY */}
        {/* ============================= */}

        {orders.length === 0 ? (

          <div className="rounded-[24px] border border-slate-200 bg-white p-10 text-center">

            <div className="text-5xl">
              📦
            </div>

            <h2 className="mt-5 text-xl font-semibold text-slate-900">
              No orders yet
            </h2>

            <p className="mt-2 text-slate-500">
              Orders you place will appear here.
            </p>


            <button
              type="button"
              onClick={() =>
                navigate(
                  "/customer/restaurants"
                )
              }
              className="mt-6 rounded-3xl bg-indigo-600 px-6 py-3 text-sm font-semibold text-white hover:bg-indigo-700"
            >
              Browse Restaurants
            </button>

          </div>

        ) : (

          /* ============================= */
          /* ORDER HISTORY */
          /* ============================= */

          <div className="space-y-6">

            {orders.map(
              order => (

                <article
                  key={
                    order.id
                  }
                  className="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm"
                >


                  {/* ============================= */}
                  {/* ORDER HEADER */}
                  {/* ============================= */}

                  <div className="flex flex-wrap items-start justify-between gap-4">

                    <div>

                      <p className="text-xs uppercase tracking-wide text-slate-400">
                        Order
                      </p>

                      <p className="mt-1 text-lg font-semibold text-slate-900">
                        #
                        {
                          order.id.slice(
                            0,
                            8
                          )
                        }
                      </p>


                      <p className="mt-2 text-sm font-medium text-slate-700">
                        {
                          order.restaurantName
                        }
                      </p>


                      <p className="mt-1 text-xs text-slate-400">
                        {
                          formatDate(
                            order.createdAt
                          )
                        }
                      </p>

                    </div>


                    {/* STATUS BADGE */}

                    <span
                      className={
                        `rounded-full px-4 py-2 text-xs font-semibold ${getStatusBadgeClass(
                          order.status
                        )}`
                      }
                    >

                      {
                        formatStatus(
                          order.status
                        )
                      }

                    </span>

                  </div>


                  {/* ============================= */}
                  {/* ORDER SUMMARY */}
                  {/* ============================= */}

                  <div className="mt-6 grid gap-5 sm:grid-cols-3">


                    {/* TOTAL */}

                    <div>

                      <p className="text-xs uppercase tracking-wide text-slate-400">
                        Total
                      </p>

                      <p className="mt-1 text-lg font-semibold text-slate-900">

                        {
                          formatPrice(
                            order.totalAmount
                          )
                        }

                      </p>

                    </div>


                    {/* PAYMENT */}

                    <div>

                      <p className="text-xs uppercase tracking-wide text-slate-400">
                        Payment
                      </p>

                      <p className="mt-1 font-semibold text-slate-800">

                        {
                          order.paymentStatus
                        }

                      </p>

                    </div>


                    {/* ITEM COUNT */}

                    <div>

                      <p className="text-xs uppercase tracking-wide text-slate-400">
                        Items
                      </p>

                      <p className="mt-1 font-semibold text-slate-800">

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


                  {/* ============================= */}
                  {/* ORDER PROGRESS */}
                  {/* ============================= */}

                  <div className="mt-8 border-t border-slate-200 pt-6">

                    <h2 className="font-semibold text-slate-900">
                      Order Progress
                    </h2>

                    <OrderProgress
                      status={
                        order.status
                      }
                    />

                  </div>


                  {/* ============================= */}
                  {/* ACTIONS */}
                  {/* ============================= */}

                  <div className="mt-6 flex flex-wrap items-center justify-between gap-4 border-t border-slate-200 pt-5">


                    <div>

                      {order.status ===
                        "DELIVERED" && (

                        <p className="text-sm font-semibold text-green-700">
                          Order delivered successfully.
                        </p>

                      )}


                      {order.status ===
                        "CANCELLED" && (

                        <p className="text-sm font-semibold text-red-700">
                          This order was cancelled.
                        </p>

                      )}

                    </div>


                    {/* VIEW ORDER DETAILS */}

                    <button
                      type="button"
                      onClick={() =>
                        navigate(
                          `/customer/orders/${order.id}`
                        )
                      }
                      className="rounded-3xl bg-indigo-600 px-5 py-2 text-sm font-semibold text-white transition hover:bg-indigo-700"
                    >
                      View Order Details
                    </button>

                  </div>


                </article>

              )
            )}

          </div>

        )}

      </div>

    </main>
  );
}


/*
 * =========================================================
 * CUSTOMER ORDER STATE MACHINE VISUALIZATION
 * =========================================================
 *
 * The customer cannot change these states.
 *
 * The restaurant admin changes the status.
 * This component only displays progress.
 */

function OrderProgress({
  status,
}: {
  status: OrderStatus;
}) {

  const stages: {
    status: OrderStatus;
    label: string;
  }[] = [

    {
      status: "PENDING",
      label: "Order Placed",
    },

    {
      status: "CONFIRMED",
      label: "Confirmed",
    },

    {
      status: "PREPARING",
      label: "Preparing",
    },

    {
      status: "READY_FOR_PICKUP",
      label: "Ready",
    },

    {
      status: "OUT_FOR_DELIVERY",
      label: "Out for Delivery",
    },

    {
      status: "DELIVERED",
      label: "Delivered",
    },

  ];


  /*
   * Cancelled orders leave the normal
   * progression path.
   */
  if (
    status === "CANCELLED"
  ) {

    return (

      <div className="mt-5 rounded-2xl bg-red-50 p-4 text-sm font-semibold text-red-700">

        Order Cancelled

      </div>

    );

  }


  /*
   * Determine how far the order has
   * progressed through the state machine.
   */
  const currentIndex =
    stages.findIndex(
      stage =>
        stage.status ===
        status
    );


  return (

    <div className="mt-6">


      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-6">


        {stages.map(
          (
            stage,
            index
          ) => {

            const completed =
              index <=
              currentIndex;


            return (

              <div
                key={
                  stage.status
                }
                className="text-center"
              >


                {/* STAGE CIRCLE */}

                <div
                  className={
                    `mx-auto flex h-10 w-10 items-center justify-center rounded-full text-sm font-bold ${
                      completed
                        ? "bg-indigo-600 text-white"
                        : "bg-slate-200 text-slate-500"
                    }`
                  }
                >

                  {
                    completed
                      ? "✓"
                      : index + 1
                  }

                </div>


                {/* STAGE LABEL */}

                <p
                  className={
                    `mt-2 text-xs font-medium ${
                      completed
                        ? "text-indigo-700"
                        : "text-slate-400"
                    }`
                  }
                >

                  {
                    stage.label
                  }

                </p>


              </div>

            );

          }
        )}


      </div>


    </div>

  );

}


export default OrdersPage;