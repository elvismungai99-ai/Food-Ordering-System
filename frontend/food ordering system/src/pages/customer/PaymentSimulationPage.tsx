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
} from "../../services/OrderService";

import {
  useCart,
} from "../../context/CartContext";

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

  const deliveryAddress =
    sessionStorage.getItem(
      "checkoutDeliveryAddress"
    );

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

        const order =
          await placeOrder({
            deliveryAddress,
          });

        setCompletedOrder(
          order
        );

        await refreshCart();

        sessionStorage.removeItem(
          "checkoutDeliveryAddress"
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
            Payment successful
          </h1>

          <p className="mt-2 text-slate-500">
            Your order has been placed successfully.
          </p>

          <div className="mt-6 rounded-2xl bg-slate-50 p-5 text-left">

            <p className="text-sm text-slate-500">
              Restaurant
            </p>

            <p className="font-semibold text-slate-900">
              {completedOrder.restaurantName}
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

          </div>

          <p className="mt-6 text-sm text-slate-500">
            This is a simulated payment for development purposes.
            No real money will be charged.
          </p>

          <button
            type="button"
            disabled={processing}
            onClick={handleSimulatePayment}
            className="mt-6 w-full rounded-3xl bg-indigo-600 px-5 py-3 text-sm font-semibold text-white hover:bg-indigo-700 disabled:cursor-not-allowed disabled:bg-slate-300"
          >
            {processing
              ? "Processing payment..."
              : "Pay now"}
          </button>

        </section>

      </div>
    </main>
  );
}

export default PaymentSimulationPage;