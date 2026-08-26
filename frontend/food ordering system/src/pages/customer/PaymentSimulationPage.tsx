import {
  useEffect,
  useState,
  useRef,
  useCallback,
} from "react";

import axios from "axios";
import { useNavigate } from "react-router-dom";

import {
  placeOrder,
  retryOrderPayment,
  getOrderTracking,
  type Order,
  type PaymentMethod,
} from "../../services/OrderService";

import { useCart } from "../../context/CartContext";
import { buildGoogleMapsPlaceUrl } from "../../utils/location";

type PaymentFlowState =
  | "IDLE"
  | "INITIATING"
  | "AWAITING_PIN"
  | "PAID_SUCCESS"
  | "FAILED"
  | "TIMEOUT";

function PaymentSimulationPage() {
  const navigate = useNavigate();
  const { cart, refreshCart } = useCart();

  const [flowState, setFlowState] = useState<PaymentFlowState>("IDLE");
  const [error, setError] = useState("");
  const [activeOrder, setActiveOrder] = useState<Order | null>(null);
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>("MPESA");
  const [mpesaPhoneNumber, setMpesaPhoneNumber] = useState("");
  const [countdown, setCountdown] = useState(60);

  const pollTimerRef = useRef<number | null>(null);
  const countdownIntervalRef = useRef<number | null>(null);

  const deliveryAddress = sessionStorage.getItem("checkoutDeliveryAddress");
  const storedDeliveryLatitude = sessionStorage.getItem("checkoutDeliveryLatitude");
  const storedDeliveryLongitude = sessionStorage.getItem("checkoutDeliveryLongitude");
  const storedPricingRaw = sessionStorage.getItem("checkoutPricing");

  const storedPricing = storedPricingRaw
    ? JSON.parse(storedPricingRaw)
    : {
        subtotal: cart?.totalAmount ?? 0,
        deliveryFee: 150,
        serviceFee: 35,
        discountAmount: 0,
        finalTotal: (cart?.totalAmount ?? 0) + 185,
      };

  const deliveryLatitude =
    storedDeliveryLatitude !== null ? Number(storedDeliveryLatitude) : null;
  const deliveryLongitude =
    storedDeliveryLongitude !== null ? Number(storedDeliveryLongitude) : null;

  const hasDeliveryCoordinates =
    deliveryLatitude !== null &&
    deliveryLongitude !== null &&
    Number.isFinite(deliveryLatitude) &&
    Number.isFinite(deliveryLongitude);

  useEffect(() => {
    if (!deliveryAddress) {
      navigate("/customer/checkout");
    }
  }, [deliveryAddress, navigate]);

  // Clean up polling and intervals on unmount
  useEffect(() => {
    return () => {
      if (pollTimerRef.current) clearInterval(pollTimerRef.current);
      if (countdownIntervalRef.current) clearInterval(countdownIntervalRef.current);
    };
  }, []);

  const formatPrice = (amount: number) => {
    return new Intl.NumberFormat("en-KE", {
      style: "currency",
      currency: "KES",
    }).format(amount);
  };

  const startMpesaPolling = useCallback((orderId: string) => {
    setFlowState("AWAITING_PIN");
    setCountdown(60);

    // Countdown interval
    if (countdownIntervalRef.current) clearInterval(countdownIntervalRef.current);
    countdownIntervalRef.current = window.setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          if (countdownIntervalRef.current) clearInterval(countdownIntervalRef.current);
          if (pollTimerRef.current) clearInterval(pollTimerRef.current);
          setFlowState("TIMEOUT");
          setError("M-Pesa payment prompt timed out. Please retry or choose Cash on Delivery.");
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    // Status poll interval
    if (pollTimerRef.current) clearInterval(pollTimerRef.current);
    pollTimerRef.current = window.setInterval(async () => {
      try {
        const tracking = await getOrderTracking(orderId);
        if (tracking.paymentStatus === "PAID") {
          if (pollTimerRef.current) clearInterval(pollTimerRef.current);
          if (countdownIntervalRef.current) clearInterval(countdownIntervalRef.current);
          setFlowState("PAID_SUCCESS");
          await refreshCart();
        } else if (tracking.paymentStatus === "FAILED") {
          if (pollTimerRef.current) clearInterval(pollTimerRef.current);
          if (countdownIntervalRef.current) clearInterval(countdownIntervalRef.current);
          setFlowState("FAILED");
          setError("M-Pesa transaction was cancelled or rejected on the phone.");
        }
      } catch (pollErr) {
        console.warn("Tracking poll error:", pollErr);
      }
    }, 3000);
  }, [refreshCart]);

  const handleInitiatePayment = async () => {
    if (!deliveryAddress) return;

    try {
      setFlowState("INITIATING");
      setError("");

      if (paymentMethod === "MPESA" && !mpesaPhoneNumber.trim()) {
        setError("Please enter your M-Pesa phone number (e.g. 0712345678).");
        setFlowState("IDLE");
        return;
      }

      const order = await placeOrder({
        deliveryAddress,
        deliveryLatitude: hasDeliveryCoordinates ? deliveryLatitude : null,
        deliveryLongitude: hasDeliveryCoordinates ? deliveryLongitude : null,
        paymentMethod,
        mpesaPhoneNumber: paymentMethod === "MPESA" ? mpesaPhoneNumber.trim() : undefined,
      });

      setActiveOrder(order);

      // Clean up checkout storage
      sessionStorage.removeItem("checkoutDeliveryAddress");
      sessionStorage.removeItem("checkoutDeliveryLatitude");
      sessionStorage.removeItem("checkoutDeliveryLongitude");
      sessionStorage.removeItem("checkoutPricing");

      if (paymentMethod === "CASH_ON_DELIVERY") {
        setFlowState("PAID_SUCCESS");
        await refreshCart();
      } else {
        startMpesaPolling(order.id);
      }
    } catch (requestError) {
      console.error("Order placement failed:", requestError);
      setFlowState("FAILED");

      if (axios.isAxiosError(requestError)) {
        const msg = requestError.response?.data?.message || requestError.response?.data || "Unable to place order.";
        setError(typeof msg === "string" ? msg : "Payment initiation failed.");
      } else {
        setError("Unable to complete order.");
      }
    }
  };

  const handleRetryMpesa = async () => {
    if (!activeOrder) return;
    try {
      setFlowState("INITIATING");
      setError("");

      const updated = await retryOrderPayment(activeOrder.id, {
        paymentMethod,
        mpesaPhoneNumber: mpesaPhoneNumber.trim(),
      });

      setActiveOrder(updated);
      startMpesaPolling(updated.id);
    } catch (err) {
      setFlowState("FAILED");
      setError("Failed to resend M-Pesa prompt. Please verify your phone number.");
    }
  };

  // State: Awaiting M-Pesa STK PIN
  if (flowState === "AWAITING_PIN") {
    return (
      <main className="min-h-screen bg-slate-100 p-6 flex items-center justify-center">
        <div className="w-full max-w-md rounded-3xl bg-white p-8 shadow-xl text-center border border-slate-200 animate-fadeIn">
          <div className="mx-auto flex h-20 w-20 items-center justify-center rounded-full bg-emerald-100 text-emerald-600 animate-pulse">
            <svg className="w-10 h-10" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z" />
            </svg>
          </div>

          <h2 className="mt-5 text-2xl font-bold text-slate-900">
            Check Your Phone
          </h2>

          <p className="mt-2 text-sm text-slate-600">
            An M-Pesa STK PIN prompt has been sent to{" "}
            <span className="font-bold text-slate-900">{mpesaPhoneNumber}</span> for{" "}
            <span className="font-bold text-emerald-600">{formatPrice(storedPricing.finalTotal)}</span>.
          </p>

          {/* Countdown Ring */}
          <div className="my-6 flex flex-col items-center justify-center">
            <div className="text-4xl font-extrabold text-indigo-600 font-mono">
              00:{countdown < 10 ? `0${countdown}` : countdown}
            </div>
            <span className="text-xs text-slate-400 mt-1">Awaiting confirmation...</span>
          </div>

          <div className="rounded-xl bg-slate-50 p-3 text-xs text-slate-500 text-left space-y-1">
            <p>1. Unlock your phone</p>
            <p>2. Enter your M-Pesa PIN and press OK</p>
            <p>3. This screen will update automatically once verified</p>
          </div>

          <div className="mt-6 flex gap-3">
            <button
              type="button"
              onClick={() => {
                if (pollTimerRef.current) clearInterval(pollTimerRef.current);
                if (countdownIntervalRef.current) clearInterval(countdownIntervalRef.current);
                setFlowState("IDLE");
              }}
              className="w-full rounded-2xl border border-slate-300 py-3 text-sm font-semibold text-slate-700 hover:bg-slate-50 transition"
            >
              Cancel / Change Method
            </button>
          </div>
        </div>
      </main>
    );
  }

  // State: Payment Confirmed / Order Placed Successfully
  if (flowState === "PAID_SUCCESS" && activeOrder) {
    return (
      <main className="min-h-screen bg-slate-100 p-6 md:p-8 flex items-center justify-center">
        <div className="w-full max-w-lg rounded-[28px] border border-slate-200 bg-white p-8 text-center shadow-xl animate-fadeIn">
          <div className="mx-auto flex h-20 w-20 items-center justify-center rounded-full bg-emerald-100 text-4xl text-emerald-600">
            ✓
          </div>

          <h1 className="mt-5 text-2xl font-bold text-slate-950">
            {activeOrder.paymentMethod === "MPESA" ? "Payment Confirmed!" : "Order Confirmed!"}
          </h1>

          <p className="mt-2 text-sm text-slate-500">
            Your order has been sent to <span className="font-semibold text-slate-800">{activeOrder.restaurantName}</span>.
          </p>

          <div className="mt-6 rounded-2xl bg-slate-50 p-5 text-left text-sm space-y-2.5 border border-slate-100">
            <div className="flex justify-between">
              <span className="text-slate-500">Order ID:</span>
              <span className="font-mono font-semibold text-slate-900">{activeOrder.id.slice(0, 8)}...</span>
            </div>

            <div className="flex justify-between">
              <span className="text-slate-500">Payment Method:</span>
              <span className="font-semibold text-slate-900">
                {activeOrder.paymentMethod === "MPESA" ? "M-Pesa" : "Cash on Delivery"}
              </span>
            </div>

            <div className="flex justify-between">
              <span className="text-slate-500">Payment Status:</span>
              <span className={`font-bold px-2 py-0.5 rounded-full text-xs ${
                activeOrder.paymentStatus === "PAID"
                  ? "bg-emerald-100 text-emerald-800"
                  : "bg-amber-100 text-amber-800"
              }`}>
                {activeOrder.paymentStatus}
              </span>
            </div>

            <div className="flex justify-between border-t border-slate-200 pt-2 font-bold text-slate-950">
              <span>Total Paid:</span>
              <span className="text-indigo-600">{formatPrice(activeOrder.totalAmount)}</span>
            </div>
          </div>

          <div className="mt-6 flex flex-col gap-3">
            <button
              type="button"
              onClick={() => navigate(`/customer/orders/${activeOrder.id}`)}
              className="w-full rounded-2xl bg-indigo-600 py-3.5 text-sm font-bold text-white shadow-lg shadow-indigo-600/30 hover:bg-indigo-700 transition"
            >
              Track Live Order →
            </button>

            <button
              type="button"
              onClick={() => navigate("/customer/orders")}
              className="w-full rounded-2xl border border-slate-300 py-3 text-sm font-semibold text-slate-700 hover:bg-slate-50 transition"
            >
              View All Orders
            </button>
          </div>
        </div>
      </main>
    );
  }

  // Default State: Select Payment & Pay
  return (
    <main className="min-h-screen bg-slate-100 p-6 md:p-8">
      <div className="mx-auto max-w-xl">
        <h1 className="text-3xl font-bold text-slate-950">Payment</h1>
        <p className="mt-2 text-slate-500">
          Choose your payment method and complete your order.
        </p>

        {error && (
          <div className="mt-6 rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-red-700 flex items-start justify-between gap-2">
            <span>{error}</span>
            {(flowState === "TIMEOUT" || flowState === "FAILED") && activeOrder && (
              <button
                type="button"
                onClick={handleRetryMpesa}
                className="font-bold underline text-red-800 whitespace-nowrap"
              >
                Retry PIN
              </button>
            )}
          </div>
        )}

        <section className="mt-6 rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">
          {/* Order Total Overview */}
          <div className="flex items-center justify-between pb-4 border-b border-slate-100">
            <span className="text-slate-500 font-medium">Final Amount to Pay</span>
            <span className="text-2xl font-bold text-indigo-600">
              {formatPrice(storedPricing.finalTotal)}
            </span>
          </div>

          {/* Itemized receipt mini-breakdown */}
          <div className="my-4 rounded-2xl bg-slate-50 p-4 text-xs text-slate-600 space-y-1.5 border border-slate-100">
            <div className="flex justify-between">
              <span>Items Subtotal:</span>
              <span className="font-medium text-slate-900">{formatPrice(storedPricing.subtotal)}</span>
            </div>
            <div className="flex justify-between">
              <span>Delivery Fee:</span>
              <span className="font-medium text-slate-900">{formatPrice(storedPricing.deliveryFee)}</span>
            </div>
            <div className="flex justify-between">
              <span>Service Fee:</span>
              <span className="font-medium text-slate-900">{formatPrice(storedPricing.serviceFee)}</span>
            </div>
            {storedPricing.discountAmount > 0 && (
              <div className="flex justify-between text-emerald-700">
                <span>Discount:</span>
                <span>-{formatPrice(storedPricing.discountAmount)}</span>
              </div>
            )}
          </div>

          {/* Delivery destination address */}
          <div className="mb-6 rounded-2xl bg-slate-50 p-4 text-xs text-slate-700 border border-slate-100">
            <p className="font-bold text-slate-800 mb-1">Delivery Address</p>
            <p>{deliveryAddress}</p>
            {hasDeliveryCoordinates && (
              <a
                href={buildGoogleMapsPlaceUrl(deliveryLatitude, deliveryLongitude)}
                target="_blank"
                rel="noreferrer"
                className="mt-2 inline-flex font-semibold text-indigo-600 hover:underline"
              >
                View on map ↗
              </a>
            )}
          </div>

          <h3 className="text-sm font-bold uppercase tracking-wider text-slate-700 mb-3">
            Select Payment Method
          </h3>

          <div className="grid gap-3">
            {[
              {
                value: "MPESA",
                label: "M-Pesa STK Push (Recommended)",
                text: "Instant prompt on your Safaricom phone to enter PIN securely.",
              },
              {
                value: "CASH_ON_DELIVERY",
                label: "Cash on Delivery",
                text: "Pay with cash or M-Pesa directly to the delivery rider.",
              },
            ].map((method) => (
              <label
                key={method.value}
                className={`cursor-pointer rounded-2xl border p-4 transition ${
                  paymentMethod === method.value
                    ? "border-indigo-600 bg-indigo-50/60 shadow-sm"
                    : "border-slate-200 bg-white hover:bg-slate-50"
                }`}
              >
                <span className="flex items-start gap-3">
                  <input
                    type="radio"
                    name="paymentMethod"
                    value={method.value}
                    checked={paymentMethod === method.value}
                    onChange={() => setPaymentMethod(method.value as PaymentMethod)}
                    className="mt-1 text-indigo-600 focus:ring-indigo-500"
                  />
                  <span>
                    <span className="block font-bold text-slate-900 text-sm">
                      {method.label}
                    </span>
                    <span className="mt-0.5 block text-xs text-slate-500">
                      {method.text}
                    </span>
                  </span>
                </span>
              </label>
            ))}
          </div>

          {paymentMethod === "MPESA" && (
            <label className="mt-5 block">
              <span className="text-xs font-bold text-slate-700 uppercase tracking-wider">
                M-Pesa Phone Number <span className="text-red-500">*</span>
              </span>
              <input
                type="tel"
                value={mpesaPhoneNumber}
                onChange={(e) => setMpesaPhoneNumber(e.target.value)}
                placeholder="e.g. 0712345678 or 254712345678"
                className="mt-1.5 w-full rounded-2xl border border-slate-300 px-4 py-3 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
              />
            </label>
          )}

          <div className="mt-6 flex gap-3">
            <button
              type="button"
              onClick={() => navigate("/customer/checkout")}
              className="rounded-3xl border border-slate-300 px-5 py-3 text-sm font-semibold text-slate-700 hover:bg-slate-50 transition"
            >
              ← Back
            </button>

            <button
              type="button"
              disabled={flowState === "INITIATING"}
              onClick={handleInitiatePayment}
              className="flex-1 rounded-3xl bg-indigo-600 px-5 py-3 text-sm font-bold text-white shadow-lg shadow-indigo-600/30 hover:bg-indigo-700 disabled:cursor-not-allowed disabled:bg-slate-300 transition"
            >
              {flowState === "INITIATING"
                ? "Sending Payment Request..."
                : paymentMethod === "MPESA"
                ? `Pay ${formatPrice(storedPricing.finalTotal)} via M-Pesa`
                : "Confirm & Place Order"}
            </button>
          </div>
        </section>
      </div>
    </main>
  );
}

export default PaymentSimulationPage;
