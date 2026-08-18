import {
  useEffect,
  useState,
} from "react";

import {
  useNavigate,
  useParams,
} from "react-router-dom";

import {
  cancelCustomerOrder,
  getCustomerOrder,
  type Order,
  type OrderStatus,
} from "../../services/OrderService";

import {
  createReview,
} from "../../services/ReviewService";

import {
  buildGoogleMapsPlaceUrl,
} from "../../utils/location";

function OrderDetailPage() {
  const navigate =
    useNavigate();

  const {
    orderId,
  } = useParams<{
    orderId: string;
  }>();

  const [
    order,
    setOrder,
  ] = useState<Order | null>(
    null
  );

  const [
    loading,
    setLoading,
  ] = useState(true);

  const [
    error,
    setError,
  ] = useState("");

  const [
    successMessage,
    setSuccessMessage,
  ] = useState("");

  const [
    reviewRating,
    setReviewRating,
  ] = useState("5");

  const [
    reviewTarget,
    setReviewTarget,
  ] = useState("restaurant");

  const [
    reviewComment,
    setReviewComment,
  ] = useState("");

  const [
    submittingReview,
    setSubmittingReview,
  ] = useState(false);

  useEffect(() => {
    const loadOrder =
      async () => {
        if (!orderId) {
          setError(
            "Order ID is missing."
          );

          setLoading(false);

          return;
        }

        try {
          setLoading(true);
          setError("");

          const data =
            await getCustomerOrder(
              orderId
            );

          setOrder(data);

        } catch (requestError) {
          console.error(
            "Failed to load order:",
            requestError
          );

          setError(
            "Unable to load the order details."
          );

        } finally {
          setLoading(false);
        }
      };

    loadOrder();

  }, [orderId]);

  const formatPrice = (
    value: number
  ) => {
    return new Intl.NumberFormat(
      "en-KE",
      {
        style: "currency",
        currency: "KES",
      }
    ).format(value);
  };

  const formatStatus = (
    status: OrderStatus
  ) => {
    return status.replaceAll(
      "_",
      " "
    );
  };

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

  const handleCancelOrder = async () => {
    if (!order) {
      return;
    }

    const reason =
      window.prompt(
        "Enter the cancellation reason"
      );

    if (!reason?.trim()) {
      return;
    }

    try {
      setError("");

      const updatedOrder =
        await cancelCustomerOrder(
          order.id,
          reason.trim()
        );

      setOrder(updatedOrder);
    } catch (requestError) {
      console.error(
        "Failed to cancel order:",
        requestError
      );
      setError(
        "Unable to cancel this order."
      );
    }
  };

  const handleReviewSubmit = async () => {
    if (!order) {
      return;
    }

    try {
      setSubmittingReview(true);
      setError("");
      setSuccessMessage("");

      await createReview({
        orderId: order.id,
        menuItemId:
          reviewTarget === "restaurant"
            ? null
            : reviewTarget,
        rating: Number(reviewRating),
        comment: reviewComment.trim(),
      });

      setReviewComment("");
      setSuccessMessage(
        "Review submitted successfully."
      );
    } catch (requestError) {
      console.error(
        "Failed to submit review:",
        requestError
      );
      setError(
        "Unable to submit this review."
      );
    } finally {
      setSubmittingReview(false);
    }
  };

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-100">
        <p className="text-slate-500">
          Loading order details...
        </p>
      </div>
    );
  }

  if (!order) {
    return (
      <div className="min-h-screen bg-slate-100 p-8">

        <div className="mx-auto max-w-3xl rounded-2xl border border-red-200 bg-red-50 p-6">

          <p className="text-red-700">
            {error ||
              "Order not found."}
          </p>

          <button
            type="button"
            onClick={() =>
              navigate(
                "/customer/orders"
              )
            }
            className="mt-5 rounded-3xl bg-indigo-600 px-5 py-2 text-sm font-semibold text-white"
          >
            Back to Orders
          </button>

        </div>

      </div>
    );
  }

  const mapUrl =
    order.deliveryLatitude != null
    && order.deliveryLongitude != null
      ? buildGoogleMapsPlaceUrl(
          order.deliveryLatitude,
          order.deliveryLongitude
        )
      : null;

  return (
    <main className="min-h-screen bg-slate-100 p-6 md:p-8">

      <div className="mx-auto max-w-5xl">

        {/* Header */}

        <div className="mb-8 flex flex-wrap items-start justify-between gap-4">

          <div>

            <button
              type="button"
              onClick={() =>
                navigate(
                  "/customer/orders"
                )
              }
              className="mb-4 text-sm font-semibold text-indigo-600"
            >
              ← Back to Orders
            </button>

            <h1 className="text-3xl font-bold text-slate-950">
              Order Details
            </h1>

            <p className="mt-2 text-slate-500">
              Order #
              {order.id.slice(
                0,
                8
              )}
            </p>

          </div>

          <span
            className={`rounded-full px-4 py-2 text-sm font-semibold ${getStatusBadgeClass(
              order.status
            )}`}
          >
            {formatStatus(
              order.status
            )}
          </span>

          {order.status === "PENDING" && (
            <button
              type="button"
              onClick={handleCancelOrder}
              className="rounded-3xl border border-red-300 px-5 py-2 text-sm font-semibold text-red-600"
            >
              Cancel Order
            </button>
          )}

        </div>

        {error && (
          <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}

        {successMessage && (
          <div className="mb-6 rounded-2xl border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-700">
            {successMessage}
          </div>
        )}

        {/* Order Summary */}

        <section className="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">

          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">

            <div>

              <p className="text-xs uppercase tracking-wide text-slate-400">
                Restaurant
              </p>

              <p className="mt-2 font-semibold text-slate-900">
                {
                  order.restaurantName
                }
              </p>

            </div>


            <div>

              <p className="text-xs uppercase tracking-wide text-slate-400">
                Order Date
              </p>

              <p className="mt-2 font-medium text-slate-700">
                {
                  formatDate(
                    order.createdAt
                  )
                }
              </p>

            </div>


            <div>

              <p className="text-xs uppercase tracking-wide text-slate-400">
                Payment
              </p>

              <p className="mt-2 font-semibold text-slate-900">
                {
                  order.paymentStatus
                }
              </p>

            </div>


            <div>

              <p className="text-xs uppercase tracking-wide text-slate-400">
                Total
              </p>

              <p className="mt-2 text-lg font-bold text-indigo-600">
                {
                  formatPrice(
                    order.totalAmount
                  )
                }
              </p>

            </div>

          </div>


          <div className="mt-6 border-t border-slate-200 pt-5">

            <p className="text-xs uppercase tracking-wide text-slate-400">
              Delivery Address
            </p>

            <p className="mt-2 text-slate-700">
              {
                order.deliveryAddress
              }
            </p>

            {mapUrl && (
              <a
                href={mapUrl}
                target="_blank"
                rel="noreferrer"
                className="mt-3 inline-flex text-sm font-semibold text-indigo-600 hover:text-indigo-700"
              >
                Open map
              </a>
            )}

          </div>


          {order.paymentReference && (

            <div className="mt-5">

              <p className="text-xs uppercase tracking-wide text-slate-400">
                Payment Reference
              </p>

              <p className="mt-2 font-mono text-sm text-slate-700">
                {
                  order.paymentReference
                }
              </p>

            </div>

          )}

        </section>


        {/* Order Items */}

        <section className="mt-8 rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">

          <h2 className="text-xl font-semibold text-slate-950">
            Items Ordered
          </h2>

          <div className="mt-6 space-y-5">

            {
              order.items.map(
                item => (

                  <article
                    key={
                      item.id
                    }
                    className="flex flex-col gap-4 border-b border-slate-200 pb-5 last:border-b-0 last:pb-0 sm:flex-row sm:items-center"
                  >

                    {item.imageUrl ? (

                      <img
                        src={
                          item.imageUrl
                        }
                        alt={
                          item.itemName
                        }
                        className="h-20 w-20 rounded-2xl object-cover"
                        loading="lazy"
                        decoding="async"
                      />

                    ) : (

                      <div className="flex h-20 w-20 items-center justify-center rounded-2xl bg-slate-100 text-xs text-slate-400">
                        No image
                      </div>

                    )}


                    <div className="flex-1">

                      <h3 className="font-semibold text-slate-900">
                        {
                          item.itemName
                        }
                      </h3>

                      {item.itemDescription && (

                        <p className="mt-1 text-sm text-slate-500">
                          {
                            item.itemDescription
                          }
                        </p>

                      )}

                    </div>


                    <div className="grid grid-cols-3 gap-6 text-sm sm:text-right">

                      <div>

                        <p className="text-xs text-slate-400">
                          Quantity
                        </p>

                        <p className="mt-1 font-semibold text-slate-800">
                          {
                            item.quantity
                          }
                        </p>

                      </div>


                      <div>

                        <p className="text-xs text-slate-400">
                          Unit Price
                        </p>

                        <p className="mt-1 font-semibold text-slate-800">
                          {
                            formatPrice(
                              item.unitPrice
                            )
                          }
                        </p>

                      </div>


                      <div>

                        <p className="text-xs text-slate-400">
                          Subtotal
                        </p>

                        <p className="mt-1 font-semibold text-slate-900">
                          {
                            formatPrice(
                              item.subtotal
                            )
                          }
                        </p>

                      </div>

                    </div>

                  </article>

                )
              )
            }

          </div>


          {/* Grand Total */}

          <div className="mt-8 flex items-center justify-between border-t border-slate-200 pt-6">

            <span className="text-lg font-semibold text-slate-900">
              Order Total
            </span>

            <span className="text-2xl font-bold text-indigo-600">
              {
                formatPrice(
                  order.totalAmount
                )
              }
            </span>

          </div>

        </section>

        {order.status === "DELIVERED" && (
          <section className="mt-8 rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">
            <h2 className="text-xl font-semibold text-slate-950">
              Review Restaurant Or Menu Items
            </h2>

            <div className="mt-5 grid gap-4 sm:grid-cols-[160px_1fr]">
              <label className="block sm:col-span-2">
                <span className="text-sm font-medium text-slate-700">
                  Review
                </span>
                <select
                  value={reviewTarget}
                  onChange={event =>
                    setReviewTarget(
                      event.target.value
                    )
                  }
                  className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3"
                >
                  <option value="restaurant">
                    Restaurant
                  </option>
                  {order.items
                    .filter(item =>
                      item.menuItemId
                    )
                    .map(item => (
                      <option
                        key={item.id}
                        value={item.menuItemId ?? ""}
                      >
                        {item.itemName}
                      </option>
                    ))}
                </select>
              </label>

              <label className="block">
                <span className="text-sm font-medium text-slate-700">
                  Rating
                </span>
                <select
                  value={reviewRating}
                  onChange={event =>
                    setReviewRating(
                      event.target.value
                    )
                  }
                  className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3"
                >
                  {[5, 4, 3, 2, 1].map(value => (
                    <option
                      key={value}
                      value={value}
                    >
                      {value}
                    </option>
                  ))}
                </select>
              </label>

              <label className="block">
                <span className="text-sm font-medium text-slate-700">
                  Comment
                </span>
                <textarea
                  rows={3}
                  value={reviewComment}
                  onChange={event =>
                    setReviewComment(
                      event.target.value
                    )
                  }
                  className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3"
                />
              </label>
            </div>

            <button
              type="button"
              disabled={submittingReview}
              onClick={handleReviewSubmit}
              className="mt-5 rounded-3xl bg-indigo-600 px-5 py-3 text-sm font-semibold text-white disabled:bg-slate-300"
            >
              {submittingReview
                ? "Submitting..."
                : "Submit Review"}
            </button>
          </section>
        )}

      </div>

    </main>
  );
}

export default OrderDetailPage;

