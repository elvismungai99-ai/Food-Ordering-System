import {
  Fragment,
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
  retryOrderPayment,
  type Order,
  type OrderStatus,
} from "../../services/OrderService";

import { useOrderTracking } from "../../hooks/useOrderTracking";
import { LiveTrackingMap } from "../../components/customer/LiveTrackingMap";
import { createReview } from "../../services/ReviewService";
import { buildGoogleMapsPlaceUrl } from "../../utils/location";

function OrderDetailPage() {
  const navigate = useNavigate();

  const { orderId } = useParams<{
    orderId: string;
  }>();

  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const [reviewRating, setReviewRating] = useState("5");
  const [reviewTarget, setReviewTarget] = useState("restaurant");
  const [reviewComment, setReviewComment] = useState("");
  const [submittingReview, setSubmittingReview] = useState(false);

  // Retry payment state
  const [retryingPayment, setRetryingPayment] = useState(false);
  const [retryPhone, setRetryPhone] = useState("");
  const [showRetryModal, setShowRetryModal] = useState(false);

  /*
   * Live tracking: polls the tracking
   * endpoint while the order is active.
   */
  const {
    tracking,
    trackingError,
    isRefreshing,
  } = useOrderTracking(order);

  // Auto-synchronize live order status and payment status when tracking snapshot arrives
  useEffect(() => {
    if (tracking && order) {
      if (
        tracking.status !== order.status ||
        (tracking.paymentStatus && tracking.paymentStatus !== order.paymentStatus)
      ) {
        setOrder((prev) =>
          prev
            ? {
                ...prev,
                status: tracking.status,
                paymentStatus: tracking.paymentStatus || prev.paymentStatus,
              }
            : null
        );
      }
    }
  }, [tracking, order]);

  useEffect(() => {
    const loadOrder = async () => {
      if (!orderId) {
        setError("Order ID is missing.");
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        setError("");

        const data = await getCustomerOrder(orderId);
        setOrder(data);
      } catch (requestError) {
        console.error("Failed to load order:", requestError);
        setError("Unable to load the order details.");
      } finally {
        setLoading(false);
      }
    };

    loadOrder();
  }, [orderId]);

  const formatPrice = (value: number) => {
    return new Intl.NumberFormat("en-KE", {
      style: "currency",
      currency: "KES",
    }).format(value);
  };

  const formatStatus = (status: OrderStatus) => {
    return status.replaceAll("_", " ");
  };

  const getStatusBadgeClass = (status: OrderStatus) => {
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

  const formatDate = (date: string) => {
    return new Date(date).toLocaleString("en-KE", {
      dateStyle: "medium",
      timeStyle: "short",
    });
  };

  const handleCancelOrder = async () => {
    if (!order) return;

    const reason = window.prompt("Enter the cancellation reason");
    if (!reason?.trim()) return;

    try {
      setError("");
      const updatedOrder = await cancelCustomerOrder(order.id, reason.trim());
      setOrder(updatedOrder);
      setSuccessMessage("Order cancelled successfully.");
    } catch (requestError) {
      console.error("Failed to cancel order:", requestError);
      setError("Unable to cancel this order.");
    }
  };

  const handleRetryPaymentSubmit = async () => {
    if (!order || !retryPhone.trim()) {
      setError("Please enter your M-Pesa phone number.");
      return;
    }

    try {
      setRetryingPayment(true);
      setError("");
      const updated = await retryOrderPayment(order.id, {
        paymentMethod: "MPESA",
        mpesaPhoneNumber: retryPhone.trim(),
      });
      setOrder(updated);
      setShowRetryModal(false);
      setSuccessMessage("M-Pesa STK PIN prompt resent to " + retryPhone.trim());
    } catch (err) {
      console.error("Failed to retry payment:", err);
      setError("Unable to re-send M-Pesa payment prompt. Please try again.");
    } finally {
      setRetryingPayment(false);
    }
  };

  const handleReviewSubmit = async () => {
    if (!order) return;

    try {
      setSubmittingReview(true);
      setError("");
      setSuccessMessage("");

      await createReview({
        orderId: order.id,
        menuItemId: reviewTarget === "restaurant" ? null : reviewTarget,
        rating: Number(reviewRating),
        comment: reviewComment.trim(),
      });

      setReviewComment("");
      setSuccessMessage("Review submitted successfully.");
    } catch (requestError) {
      console.error("Failed to submit review:", requestError);
      setError("Unable to submit this review.");
    } finally {
      setSubmittingReview(false);
    }
  };

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-100">
        <p className="text-slate-500">Loading order details...</p>
      </div>
    );
  }

  if (!order) {
    return (
      <div className="min-h-screen bg-slate-100 p-8">
        <div className="mx-auto max-w-3xl rounded-2xl border border-red-200 bg-red-50 p-6">
          <p className="text-red-700">{error || "Order not found."}</p>
          <button
            type="button"
            onClick={() => navigate("/customer/orders")}
            className="mt-5 rounded-3xl bg-indigo-600 px-5 py-2 text-sm font-semibold text-white"
          >
            Back to Orders
          </button>
        </div>
      </div>
    );
  }

  const mapUrl =
    order.deliveryLatitude != null && order.deliveryLongitude != null
      ? buildGoogleMapsPlaceUrl(order.deliveryLatitude, order.deliveryLongitude)
      : null;

  const TRACKING_STEPS: {
    status: OrderStatus;
    label: string;
  }[] = [
    { status: "CONFIRMED", label: "Confirmed" },
    { status: "PREPARING", label: "Preparing" },
    { status: "READY_FOR_PICKUP", label: "Ready" },
    { status: "OUT_FOR_DELIVERY", label: "On the way" },
    { status: "DELIVERED", label: "Delivered" },
  ];

  const isTrackable = [
    "CONFIRMED",
    "PREPARING",
    "READY_FOR_PICKUP",
    "OUT_FOR_DELIVERY",
  ].includes(order.status);

  const currentStepIndex = TRACKING_STEPS.findIndex(
    (step) => step.status === order.status
  );

  const formatVehicleType = (vehicleType?: string | null) => {
    switch (vehicleType) {
      case "BICYCLE":
        return "Bicycle";
      case "MOTORCYCLE":
        return "Motorcycle";
      case "CAR":
        return "Car";
      default:
        return "Vehicle";
    }
  };

  return (
    <main className="min-h-screen bg-slate-100 p-6 md:p-8">
      <div className="mx-auto max-w-5xl">
        {/* Header */}
        <div className="mb-8 flex flex-wrap items-start justify-between gap-4">
          <div>
            <button
              type="button"
              onClick={() => navigate("/customer/orders")}
              className="mb-4 text-sm font-semibold text-indigo-600 hover:underline inline-flex items-center gap-1"
            >
              ← Back to Orders
            </button>

            <h1 className="text-3xl font-bold text-slate-950">Order Details</h1>
            <p className="mt-1 text-slate-500 font-mono text-xs">
              Order ID: #{order.id}
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <span
              className={`rounded-full px-4 py-2 text-sm font-bold uppercase tracking-wider ${getStatusBadgeClass(
                order.status
              )}`}
            >
              {formatStatus(order.status)}
            </span>

            {order.status === "PENDING" && (
              <button
                type="button"
                onClick={handleCancelOrder}
                className="rounded-3xl border border-red-300 px-5 py-2 text-sm font-semibold text-red-600 hover:bg-red-50 transition"
              >
                Cancel Order
              </button>
            )}
          </div>
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

        {/* Live Order ETA Alert if active */}
        {isTrackable && (
          <div className="mb-8 rounded-2xl border border-indigo-200 bg-indigo-50/80 p-5 shadow-sm flex flex-wrap items-center justify-between gap-4">
            <div>
              <h3 className="font-bold text-indigo-950">
                {order.status === "OUT_FOR_DELIVERY"
                  ? "Rider is heading to your doorstep!"
                  : order.status === "PREPARING"
                  ? "Kitchen is preparing your meal fresh"
                  : "Order is confirmed and queued"}
              </h3>
              <p className="text-xs text-indigo-700 mt-0.5">
                Estimated Delivery:{" "}
                <span className="font-bold text-indigo-900">
                  ~{tracking?.estimatedDeliveryMinutes ?? 25} minutes
                </span>
              </p>
            </div>

            <div className="text-xs font-semibold text-indigo-600 bg-white px-3 py-1.5 rounded-full border border-indigo-100">
              Live status synchronized in real time
            </div>
          </div>
        )}

        {/* Order Summary Card */}
        <section className="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
            <div>
              <p className="text-xs uppercase tracking-wide text-slate-400 font-semibold">
                Restaurant
              </p>
              <p className="mt-1 font-bold text-slate-900">{order.restaurantName}</p>
            </div>

            <div>
              <p className="text-xs uppercase tracking-wide text-slate-400 font-semibold">
                Order Placed
              </p>
              <p className="mt-1 font-medium text-slate-700">{formatDate(order.createdAt)}</p>
            </div>

            <div>
              <p className="text-xs uppercase tracking-wide text-slate-400 font-semibold">
                Payment Status
              </p>
              <div className="mt-1 flex items-center gap-2">
                <span
                  className={`font-bold text-xs px-2.5 py-0.5 rounded-full ${
                    order.paymentStatus === "PAID"
                      ? "bg-emerald-100 text-emerald-800"
                      : "bg-amber-100 text-amber-800"
                  }`}
                >
                  {order.paymentStatus}
                </span>

                {order.paymentStatus !== "PAID" && order.status !== "CANCELLED" && (
                  <button
                    type="button"
                    onClick={() => setShowRetryModal(true)}
                    className="text-xs font-bold text-indigo-600 hover:underline"
                  >
                    Retry PIN
                  </button>
                )}
              </div>
            </div>

            <div>
              <p className="text-xs uppercase tracking-wide text-slate-400 font-semibold">
                Total Amount
              </p>
              <p className="mt-1 text-xl font-bold text-indigo-600">
                {formatPrice(order.totalAmount)}
              </p>
            </div>
          </div>

          <div className="mt-6 border-t border-slate-100 pt-5">
            <p className="text-xs uppercase tracking-wide text-slate-400 font-semibold">
              Delivery Destination
            </p>
            <p className="mt-1 text-sm text-slate-700">{order.deliveryAddress}</p>
            {mapUrl && (
              <a
                href={mapUrl}
                target="_blank"
                rel="noreferrer"
                className="mt-2 inline-flex text-xs font-semibold text-indigo-600 hover:underline"
              >
                Open destination in Google Maps ↗
              </a>
            )}
          </div>

          {order.paymentReference && (
            <div className="mt-4">
              <p className="text-xs uppercase tracking-wide text-slate-400 font-semibold">
                Payment Reference
              </p>
              <p className="mt-1 font-mono text-xs text-slate-700">
                {order.paymentReference}
              </p>
            </div>
          )}
        </section>

        {/* Live Tracking Map & Rider Details */}
        {isTrackable && (
          <section className="mt-8 rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <h2 className="text-xl font-bold text-slate-950">
                Live Tracking & Progress
              </h2>
              <span className="text-xs text-slate-400">
                {isRefreshing ? "Updating..." : "Auto-refreshes every 10s"}
              </span>
            </div>

            {trackingError && (
              <div className="mt-4 rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-700">
                {trackingError}
              </div>
            )}

            {/* Status Stepper */}
            <div className="mt-6 flex items-start overflow-x-auto pb-2">
              {TRACKING_STEPS.map((step, index) => {
                const reached = index <= currentStepIndex;
                const done = index < currentStepIndex;

                return (
                  <Fragment key={step.status}>
                    {index > 0 && (
                      <div
                        className={`mt-3.5 h-1 min-w-8 flex-1 transition-colors ${
                          reached ? "bg-indigo-600" : "bg-slate-200"
                        }`}
                      />
                    )}

                    <div className="flex w-16 flex-col items-center gap-1.5 flex-shrink-0">
                      <span
                        className={`flex h-8 w-8 items-center justify-center rounded-full text-xs font-bold transition ${
                          reached
                            ? "bg-indigo-600 text-white shadow-md shadow-indigo-600/30"
                            : "bg-slate-200 text-slate-500"
                        }`}
                      >
                        {done ? "✓" : index + 1}
                      </span>

                      <span
                        className={`text-center text-[11px] font-semibold leading-tight ${
                          reached ? "text-indigo-700 font-bold" : "text-slate-400"
                        }`}
                      >
                        {step.label}
                      </span>
                    </div>
                  </Fragment>
                );
              })}
            </div>

            {/* Google Map */}
            <div className="mt-6 rounded-2xl overflow-hidden border border-slate-200">
              <LiveTrackingMap
                restaurantLatitude={tracking?.restaurantLatitude}
                restaurantLongitude={tracking?.restaurantLongitude}
                destinationLatitude={order.deliveryLatitude}
                destinationLongitude={order.deliveryLongitude}
                riderLatitude={tracking?.riderLatitude}
                riderLongitude={tracking?.riderLongitude}
              />
            </div>

            {/* Assigned Rider Card */}
            {tracking?.riderAssigned ? (
              <div className="mt-5 flex flex-wrap items-center gap-4 rounded-2xl border border-indigo-100 bg-indigo-50/80 p-4">
                <div className="flex-1">
                  <p className="font-bold text-slate-900">{tracking.riderName}</p>
                  <p className="text-xs text-slate-600">
                    {formatVehicleType(tracking.vehicleType)}
                    {tracking.licencePlate ? ` · Plate: ${tracking.licencePlate}` : ""}
                  </p>
                  {tracking.riderLocationUpdatedAt && (
                    <p className="mt-1 text-[11px] text-slate-400">
                      Last GPS ping:{" "}
                      {new Date(tracking.riderLocationUpdatedAt).toLocaleTimeString("en-KE")}
                    </p>
                  )}
                </div>

                {tracking.riderPhoneNumber && (
                  <a
                    href={`tel:${tracking.riderPhoneNumber}`}
                    className="rounded-2xl bg-indigo-600 px-4 py-2.5 text-xs font-bold text-white shadow-md hover:bg-indigo-700 transition"
                  >
                    Call Rider
                  </a>
                )}
              </div>
            ) : (
              <p className="mt-5 rounded-2xl bg-slate-50 px-4 py-3 text-xs text-slate-500 text-center border border-slate-100">
                Rider will be dispatched as soon as the kitchen completes preparation...
              </p>
            )}
          </section>
        )}

        {/* Customized Items Ordered */}
        <section className="mt-8 rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">
          <h2 className="text-xl font-bold text-slate-950 mb-6">Items in This Order</h2>

          <div className="space-y-5">
            {order.items.map((item) => (
              <article
                key={item.id}
                className="flex flex-col gap-4 border-b border-slate-100 pb-5 last:border-b-0 last:pb-0 sm:flex-row sm:items-center"
              >
                {item.imageUrl ? (
                  <img
                    src={item.imageUrl}
                    alt={item.itemName}
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
                  <h3 className="font-bold text-slate-900 text-base">{item.itemName}</h3>

                  {/* Customization Details */}
                  <div className="mt-1 flex flex-wrap gap-1.5">
                    {item.selectedSize && (
                      <span className="rounded-md bg-indigo-50 px-2 py-0.5 text-xs font-semibold text-indigo-700">
                        Size: {item.selectedSize}
                      </span>
                    )}
                    {item.selectedAddOns && item.selectedAddOns.length > 0 && (
                      <span className="rounded-md bg-emerald-50 px-2 py-0.5 text-xs font-semibold text-emerald-700">
                        +{item.selectedAddOns.join(", ")}
                      </span>
                    )}
                    {item.removalRequests && item.removalRequests.length > 0 && (
                      <span className="rounded-md bg-rose-50 px-2 py-0.5 text-xs font-semibold text-rose-700">
                        {item.removalRequests.join(", ")}
                      </span>
                    )}
                  </div>

                  {item.specialInstructions && (
                    <p className="mt-1 text-xs italic text-slate-500">
                      Kitchen Note: "{item.specialInstructions}"
                    </p>
                  )}
                </div>

                <div className="grid grid-cols-3 gap-6 text-sm sm:text-right">
                  <div>
                    <p className="text-xs text-slate-400">Quantity</p>
                    <p className="mt-1 font-semibold text-slate-800">{item.quantity}</p>
                  </div>

                  <div>
                    <p className="text-xs text-slate-400">Unit Price</p>
                    <p className="mt-1 font-semibold text-slate-800">
                      {formatPrice(item.unitPrice)}
                    </p>
                  </div>

                  <div>
                    <p className="text-xs text-slate-400">Subtotal</p>
                    <p className="mt-1 font-bold text-slate-900">
                      {formatPrice(item.subtotal)}
                    </p>
                  </div>
                </div>
              </article>
            ))}
          </div>

          {/* Itemized Order Receipt Breakdown */}
          <div className="mt-8 border-t border-slate-200 pt-6 space-y-2 text-sm text-slate-600">
            <div className="flex justify-between">
              <span>Items Subtotal:</span>
              <span className="font-semibold text-slate-900">{formatPrice(order.subtotalAmount)}</span>
            </div>

            <div className="flex justify-between">
              <span>Delivery Fee:</span>
              <span>{formatPrice(order.deliveryFee)}</span>
            </div>

            <div className="flex justify-between">
              <span>Service Fee:</span>
              <span>{formatPrice(order.serviceFee)}</span>
            </div>

            {order.discountAmount > 0 && (
              <div className="flex justify-between text-emerald-600 font-semibold">
                <span>Discount:</span>
                <span>-{formatPrice(order.discountAmount)}</span>
              </div>
            )}

            <div className="border-t border-slate-200 pt-3 mt-3 flex items-center justify-between text-lg font-bold text-slate-950">
              <span>Total Paid:</span>
              <span className="text-2xl text-indigo-600">{formatPrice(order.totalAmount)}</span>
            </div>
          </div>
        </section>

        {/* Customer Review Section */}
        {order.status === "DELIVERED" && (
          <section className="mt-8 rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">
            <h2 className="text-xl font-bold text-slate-950">Review Your Order</h2>
            <p className="mt-1 text-xs text-slate-500">
              Share your feedback about the food quality and delivery service.
            </p>

            <div className="mt-5 grid gap-4 sm:grid-cols-[180px_1fr]">
              <label className="block sm:col-span-2">
                <span className="text-xs font-bold text-slate-700 uppercase tracking-wider">
                  Review Target
                </span>
                <select
                  value={reviewTarget}
                  onChange={(e) => setReviewTarget(e.target.value)}
                  className="mt-1.5 w-full rounded-2xl border border-slate-300 p-3 text-sm"
                >
                  <option value="restaurant">Entire Restaurant ({order.restaurantName})</option>
                  {order.items
                    .filter((item) => item.menuItemId)
                    .map((item) => (
                      <option key={item.id} value={item.menuItemId ?? ""}>
                        Dish: {item.itemName}
                      </option>
                    ))}
                </select>
              </label>

              <label className="block">
                <span className="text-xs font-bold text-slate-700 uppercase tracking-wider">
                  Rating
                </span>
                <select
                  value={reviewRating}
                  onChange={(e) => setReviewRating(e.target.value)}
                  className="mt-1.5 w-full rounded-2xl border border-slate-300 p-3 text-sm font-bold"
                >
                  {[5, 4, 3, 2, 1].map((val) => (
                    <option key={val} value={val}>
                      {val} Star{val > 1 ? "s" : ""}
                    </option>
                  ))}
                </select>
              </label>

              <label className="block">
                <span className="text-xs font-bold text-slate-700 uppercase tracking-wider">
                  Feedback Notes
                </span>
                <textarea
                  rows={2}
                  value={reviewComment}
                  onChange={(e) => setReviewComment(e.target.value)}
                  placeholder="How was the taste, packaging, and temperature?"
                  className="mt-1.5 w-full rounded-2xl border border-slate-300 p-3 text-sm outline-none focus:border-indigo-500"
                />
              </label>
            </div>

            <button
              type="button"
              disabled={submittingReview}
              onClick={handleReviewSubmit}
              className="mt-4 rounded-3xl bg-indigo-600 px-6 py-3 text-sm font-bold text-white hover:bg-indigo-700 disabled:bg-slate-300 shadow-md transition"
            >
              {submittingReview ? "Submitting Review..." : "Submit Feedback"}
            </button>
          </section>
        )}

        {/* Retry Payment Modal */}
        {showRetryModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm animate-fadeIn">
            <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-2xl border border-slate-200">
              <h3 className="text-lg font-bold text-slate-900 mb-2">Retry M-Pesa Payment</h3>
              <p className="text-xs text-slate-600 mb-4">
                Enter the phone number to receive a new M-Pesa STK push for{" "}
                <span className="font-bold text-indigo-600">{formatPrice(order.totalAmount)}</span>.
              </p>

              <input
                type="tel"
                value={retryPhone}
                onChange={(e) => setRetryPhone(e.target.value)}
                placeholder="e.g. 0712345678"
                className="w-full rounded-xl border border-slate-300 p-3 text-sm outline-none focus:border-indigo-500 mb-4"
              />

              <div className="flex gap-3">
                <button
                  type="button"
                  onClick={() => setShowRetryModal(false)}
                  className="flex-1 rounded-xl border border-slate-300 py-2.5 text-xs font-semibold text-slate-700 hover:bg-slate-50"
                >
                  Cancel
                </button>
                <button
                  type="button"
                  disabled={retryingPayment || !retryPhone.trim()}
                  onClick={handleRetryPaymentSubmit}
                  className="flex-1 rounded-xl bg-emerald-600 py-2.5 text-xs font-bold text-white hover:bg-emerald-700 disabled:bg-slate-300 shadow-md"
                >
                  {retryingPayment ? "Sending..." : "Send M-Pesa Push"}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </main>
  );
}

export default OrderDetailPage;
