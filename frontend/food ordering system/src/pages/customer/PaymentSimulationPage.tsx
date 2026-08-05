import {
  useEffect,
  useState,
} from "react";

import axios from "axios";

import {
  useNavigate,
} from "react-router-dom";

import {
  placeOrder,
  type Order,
  type PaymentMethod,
} from "../../services/OrderService";

import {
  useCart,
} from "../../context/CartContext";

import {
  buildOpenRouteServiceMapUrl,
} from "../../utils/location";

function PaymentSimulationPage() {
  const navigate = useNavigate();

  const {
    cart,
    refreshCart,
  } = useCart();

  const [
    processing,
    setProcessing,
  ] = useState(false);

  const [
    error,
    setError,
  ] = useState("");

  const [
    completedOrder,
    setCompletedOrder,
  ] = useState<Order | null>(null);

  const [
    paymentMethod,
    setPaymentMethod,
  ] = useState<PaymentMethod>(
    "CASH_ON_DELIVERY"
  );

  const [
    mpesaPhoneNumber,
    setMpesaPhoneNumber,
  ] = useState("");

  const deliveryAddress =
    sessionStorage.getItem(
      "checkoutDeliveryAddress"
    );

  const storedDeliveryLatitude =
    sessionStorage.getItem(
      "checkoutDeliveryLatitude"
    );

  const storedDeliveryLongitude =
    sessionStorage.getItem(
      "checkoutDeliveryLongitude"
    );

  const deliveryLatitude =
    storedDeliveryLatitude !== null
      ? Number(storedDeliveryLatitude)
      : null;

  const deliveryLongitude =
    storedDeliveryLongitude !== null
      ? Number(storedDeliveryLongitude)
      : null;

  const hasDeliveryCoordinates =
    deliveryLatitude !== null
    && deliveryLongitude !== null
    && Number.isFinite(deliveryLatitude)
    && Number.isFinite(deliveryLongitude);

  useEffect(() => {
    if (!deliveryAddress) {
      navigate(
        "/customer/checkout"
      );
    }
  }, [
    deliveryAddress,
    navigate,
  ]);

  const handleSimulatePayment =
    async () => {

      if (!deliveryAddress) {
        return;
      }

      try {
        setProcessing(true);
        setError("");

        if (
          paymentMethod === "MPESA"
          && !mpesaPhoneNumber.trim()
        ) {
          setError(
            "Enter the M-Pesa phone number that will receive the STK push."
          );
          setProcessing(false);
          return;
        }

        const order =
          await placeOrder({
            deliveryAddress,
            deliveryLatitude:
              hasDeliveryCoordinates
                ? deliveryLatitude
                : null,
            deliveryLongitude:
              hasDeliveryCoordinates
                ? deliveryLongitude
                : null,
            paymentMethod,
            mpesaPhoneNumber:
              paymentMethod === "MPESA"
                ? mpesaPhoneNumber.trim()
                : undefined,
          });

        setCompletedOrder(
          order
        );

        await refreshCart();

        sessionStorage.removeItem(
          "checkoutDeliveryAddress"
        );
        sessionStorage.removeItem(
          "checkoutDeliveryLatitude"
        );
        sessionStorage.removeItem(
          "checkoutDeliveryLongitude"
        );

      } catch (requestError) {
        console.error(
          "Payment/order placement failed:",
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
              "Unable to complete payment."
            );
          }

        } else {
          setError(
            "Unable to complete payment."
          );
        }

      } finally {
        setProcessing(false);
      }
    };

  if (completedOrder) {
    return (
      <main className="min-h-screen bg-slate-100 p-6 md:p-8">
        <div className="mx-auto max-w-xl rounded-[24px] border border-slate-200 bg-white p-8 text-center shadow-sm">

          <div className="text-5xl">
            ✅
          </div>

          <h1 className="mt-5 text-2xl font-bold text-slate-950">
            Order received
          </h1>

          <p className="mt-2 text-slate-500">
            Your order has been placed and payment is being tracked.
          </p>

          <div className="mt-6 rounded-2xl bg-slate-50 p-5 text-left">

            <p className="text-sm text-slate-500">
              Restaurant
            </p>

            <p className="font-semibold text-slate-900">
              {completedOrder.restaurantName}
            </p>

            <p className="mt-4 text-sm text-slate-500">
              Payment method
            </p>

            <p className="font-semibold text-slate-900">
              {completedOrder.paymentMethod.replaceAll(
                "_",
                " "
              )}
            </p>

            <p className="mt-4 text-sm text-slate-500">
              Payment status
            </p>

            <p className="font-semibold text-slate-900">
              {completedOrder.paymentStatus}
            </p>

            <p className="mt-4 text-sm text-slate-500">
              Subtotal
            </p>

            <p className="font-semibold text-slate-900">
              KES {completedOrder.subtotalAmount}
            </p>

            <p className="mt-4 text-sm text-slate-500">
              Delivery + service
            </p>

            <p className="font-semibold text-slate-900">
              KES{" "}
              {completedOrder.deliveryFee
                + completedOrder.serviceFee}
            </p>

            <p className="mt-4 text-sm text-slate-500">
              Total
            </p>

            <p className="font-semibold text-slate-900">
              KES {completedOrder.totalAmount}
            </p>

            <p className="mt-4 text-sm text-slate-500">
              Payment reference
            </p>

            <p className="font-semibold text-slate-900">
              {completedOrder.paymentReference}
            </p>

          </div>

          <button
            type="button"
            onClick={() =>
              navigate(
                "/customer/orders"
              )
            }
            className="mt-6 w-full rounded-3xl bg-indigo-600 px-5 py-3 text-sm font-semibold text-white hover:bg-indigo-700"
          >
            View my orders
          </button>

        </div>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-slate-100 p-6 md:p-8">

      <div className="mx-auto max-w-xl">

        <h1 className="text-3xl font-bold text-slate-950">
          Payment
        </h1>

        <p className="mt-2 text-slate-500">
          Complete your simulated payment.
        </p>

        {error && (
          <div className="mt-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-red-700">
            {error}
          </div>
        )}

        <section className="mt-8 rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">

          <div className="flex items-center justify-between">

            <span className="text-slate-500">
              Amount to pay
            </span>

            <span className="text-2xl font-bold text-indigo-600">
              KES {cart?.totalAmount ?? 0}
            </span>

          </div>

          <div className="mt-6 rounded-2xl bg-slate-50 p-4">

            <p className="text-sm text-slate-500">
              Delivery address
            </p>

            <p className="mt-1 font-medium text-slate-900">
              {deliveryAddress}
            </p>

            {hasDeliveryCoordinates && (
              <a
                href={buildOpenRouteServiceMapUrl(
                  deliveryLatitude,
                  deliveryLongitude
                )}
                target="_blank"
                rel="noreferrer"
                className="mt-3 inline-flex text-sm font-semibold text-indigo-600 hover:text-indigo-700"
              >
                Open map
              </a>
            )}

          </div>

          <p className="mt-6 text-sm text-slate-500">
            Choose how the order should be paid. M-Pesa requires
            your Daraja credentials in the backend local config
            before it can contact Safaricom.
          </p>

          <div className="mt-5 grid gap-3">
            {[
              {
                value: "CASH_ON_DELIVERY",
                label: "Cash on delivery",
                text: "Accept the order now and collect payment at delivery.",
              },
              {
                value: "MPESA",
                label: "M-Pesa",
                text: "Send a Daraja STK push to the customer phone.",
              },
            ].map((method) => (
              <label
                key={method.value}
                className={`cursor-pointer rounded-2xl border p-4 ${
                  paymentMethod === method.value
                    ? "border-indigo-500 bg-indigo-50"
                    : "border-slate-200 bg-white"
                }`}
              >
                <span className="flex items-start gap-3">
                  <input
                    type="radio"
                    name="paymentMethod"
                    value={method.value}
                    checked={paymentMethod === method.value}
                    onChange={() =>
                      setPaymentMethod(
                        method.value as PaymentMethod
                      )
                    }
                    className="mt-1"
                  />
                  <span>
                    <span className="block font-semibold text-slate-900">
                      {method.label}
                    </span>
                    <span className="mt-1 block text-sm text-slate-500">
                      {method.text}
                    </span>
                  </span>
                </span>
              </label>
            ))}
          </div>

          {paymentMethod === "MPESA" && (
            <label className="mt-5 block">
              <span className="text-sm font-medium text-slate-700">
                M-Pesa phone number
              </span>
              <input
                type="tel"
                value={mpesaPhoneNumber}
                onChange={(event) =>
                  setMpesaPhoneNumber(
                    event.target.value
                  )
                }
                placeholder="2547XXXXXXXX"
                className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
              />
            </label>
          )}

          <button
            type="button"
            disabled={processing}
            onClick={handleSimulatePayment}
            className="mt-6 w-full rounded-3xl bg-indigo-600 px-5 py-3 text-sm font-semibold text-white hover:bg-indigo-700 disabled:cursor-not-allowed disabled:bg-slate-300"
          >
            {processing
              ? "Processing payment..."
              : paymentMethod === "MPESA"
                ? "Send M-Pesa STK push"
                : "Place order"}
          </button>

        </section>

      </div>
    </main>
  );
}

export default PaymentSimulationPage;
