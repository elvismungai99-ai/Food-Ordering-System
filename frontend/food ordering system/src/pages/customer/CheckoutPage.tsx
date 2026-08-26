import {
  type FormEvent,
  useState,
} from "react";

import { useNavigate } from "react-router-dom";

import { useCart } from "../../context/CartContext";
import { reverseGeocodeLocation } from "../../utils/location";

function CheckoutPage() {
  const navigate = useNavigate();

  const { cart } = useCart();

  const [deliveryAddress, setDeliveryAddress] =
    useState("");

  const [
    deliveryLatitude,
    setDeliveryLatitude,
  ] = useState<number | null>(null);

  const [
    deliveryLongitude,
    setDeliveryLongitude,
  ] = useState<number | null>(null);

  const [
    locating,
    setLocating,
  ] = useState(false);

  const [
    locationMessage,
    setLocationMessage,
  ] = useState("");

  const [error, setError] =
    useState("");

  const handleUseCurrentLocation =
    async () => {
      setError("");
      setLocationMessage("");

      if (!navigator.geolocation) {
        setError(
          "Your browser does not support live location."
        );
        return;
      }

      try {
        setLocating(true);

        const position =
          await new Promise<GeolocationPosition>(
            (resolve, reject) => {
              navigator.geolocation.getCurrentPosition(
                resolve,
                reject,
                {
                  enableHighAccuracy: true,
                  timeout: 15000,
                  maximumAge: 0,
                }
              );
            }
          );

        const latitude =
          Number(
            position.coords.latitude.toFixed(
              6
            )
          );

        const longitude =
          Number(
            position.coords.longitude.toFixed(
              6
            )
          );

        setDeliveryLatitude(latitude);
        setDeliveryLongitude(longitude);

        // Resolve readable street address and auto-fill delivery address
        const resolvedAddress =
          await reverseGeocodeLocation(
            latitude,
            longitude
          );

        setDeliveryAddress(resolvedAddress);
        setLocationMessage(
          "Live location detected and delivery address filled! You can edit or add building details if needed."
        );

      } catch (locationError) {
        console.error(
          "Unable to get current location:",
          locationError
        );

        setDeliveryLatitude(null);
        setDeliveryLongitude(null);
        setError(
          "Unable to detect your current location. Please allow location access or enter your address manually."
        );

      } finally {
        setLocating(false);
      }
    };

  const handleContinueToPayment = (
    event: FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    setError("");

    if (!cart || cart.items.length === 0) {
      setError("Your cart is empty.");
      return;
    }

    if (cart.hasPriceChanges) {
      setError(
        "Please accept the updated prices before checkout."
      );
      return;
    }

    if (cart.hasUnavailableItems) {
      setError(
        "Please remove unavailable items before checkout."
      );
      return;
    }

    if (!deliveryAddress.trim()) {
      setError(
        "Delivery address is required."
      );
      return;
    }

    sessionStorage.setItem(
      "checkoutDeliveryAddress",
      deliveryAddress.trim()
    );

    if (
      deliveryLatitude !== null
      && deliveryLongitude !== null
    ) {
      sessionStorage.setItem(
        "checkoutDeliveryLatitude",
        String(deliveryLatitude)
      );
      sessionStorage.setItem(
        "checkoutDeliveryLongitude",
        String(deliveryLongitude)
      );
    } else {
      sessionStorage.removeItem(
        "checkoutDeliveryLatitude"
      );
      sessionStorage.removeItem(
        "checkoutDeliveryLongitude"
      );
    }

    navigate("/customer/payment");
  };

  if (!cart) {
    return (
      <div className="min-h-screen bg-slate-100 p-8">
        <p className="text-center text-slate-500">
          Loading checkout...
        </p>
      </div>
    );
  }

  return (
    <main className="min-h-screen bg-slate-100 p-6 md:p-8">
      <div className="mx-auto max-w-3xl">

        <div className="mb-8">
          <h1 className="text-3xl font-bold text-slate-950">
            Checkout
          </h1>

          <p className="mt-2 text-slate-500">
            Confirm your delivery details before payment.
          </p>
        </div>

        {error && (
          <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-red-700">
            {error}
          </div>
        )}

        <section className="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">

          <div className="mb-6">
            <p className="text-sm text-slate-500">
              Items
            </p>

            <p className="text-xl font-semibold text-slate-900">
              {cart.totalItems}
            </p>
          </div>

          <div className="mb-6">
            <p className="text-sm text-slate-500">
              Total amount
            </p>

            <p className="text-2xl font-bold text-indigo-600">
              KES {cart.totalAmount}
            </p>
          </div>

          <form
            onSubmit={handleContinueToPayment}
          >
            <div className="mb-4 flex flex-wrap items-center gap-3">
              <button
                type="button"
                disabled={locating}
                onClick={handleUseCurrentLocation}
                className="rounded-3xl border border-indigo-200 bg-indigo-50 px-5 py-3 text-sm font-semibold text-indigo-700 hover:bg-indigo-100 disabled:cursor-not-allowed disabled:border-slate-200 disabled:bg-slate-100 disabled:text-slate-400"
              >
                {locating
                  ? "Detecting location..."
                  : "Use my current location"}
              </button>

              {deliveryLatitude !== null
                && deliveryLongitude !== null && (
                  <span className="text-sm text-slate-500">
                    {deliveryLatitude},{" "}
                    {deliveryLongitude}
                  </span>
                )}
            </div>

            {locationMessage && (
              <div className="mb-4 rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
                {locationMessage}
              </div>
            )}

            <label className="block">

              <span className="text-sm font-medium text-slate-700">
                Delivery address
              </span>

              <textarea
                required
                rows={4}
                value={deliveryAddress}
                onChange={(event) =>
                  setDeliveryAddress(
                    event.target.value
                  )
                }
                placeholder="Enter your delivery address"
                className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
              />

            </label>

            <div className="mt-6 flex gap-3">

              <button
                type="button"
                onClick={() =>
                  navigate(
                    "/customer/cart"
                  )
                }
                className="rounded-3xl border border-slate-300 px-5 py-3 text-sm font-semibold text-slate-700"
              >
                Back to cart
              </button>

              <button
                type="submit"
                className="flex-1 rounded-3xl bg-indigo-600 px-5 py-3 text-sm font-semibold text-white hover:bg-indigo-700"
              >
                Continue to payment
              </button>

            </div>
          </form>

        </section>
      </div>
    </main>
  );
}

export default CheckoutPage;
