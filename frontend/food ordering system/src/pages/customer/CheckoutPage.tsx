import {
  type FormEvent,
  useEffect,
  useState,
} from "react";

import { useNavigate } from "react-router-dom";

import CustomerHeader from "../../components/customer/CustomerHeader";
import { useCart } from "../../context/CartContext";
import { reverseGeocodeLocation } from "../../utils/location";
import { getSavedAddresses, type SavedAddress } from "../../services/UserService";

const CHECKOUT_DRAFT_KEY = "checkoutDraft_v1";

function CheckoutPage() {
  const navigate = useNavigate();

  const { cart } = useCart();

  const [deliveryAddress, setDeliveryAddress] = useState("");
  const [deliveryLatitude, setDeliveryLatitude] = useState<number | null>(null);
  const [deliveryLongitude, setDeliveryLongitude] = useState<number | null>(null);
  const [deliveryInstructions, setDeliveryInstructions] = useState("");

  const [savedAddresses, setSavedAddresses] = useState<SavedAddress[]>([]);
  const [selectedAddressId, setSelectedAddressId] = useState<string>("");

  const [locating, setLocating] = useState(false);
  const [locationMessage, setLocationMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    // Load draft if previously stored
    try {
      const savedDraft = localStorage.getItem(CHECKOUT_DRAFT_KEY);
      if (savedDraft) {
        const parsed = JSON.parse(savedDraft);
        if (parsed.deliveryAddress) setDeliveryAddress(parsed.deliveryAddress);
        if (parsed.deliveryInstructions) setDeliveryInstructions(parsed.deliveryInstructions);
        if (parsed.deliveryLatitude) setDeliveryLatitude(parsed.deliveryLatitude);
        if (parsed.deliveryLongitude) setDeliveryLongitude(parsed.deliveryLongitude);
        if (parsed.selectedAddressId) setSelectedAddressId(parsed.selectedAddressId);
      }
    } catch (e) {
      console.warn("Could not parse draft:", e);
    }

    // Load saved customer delivery addresses
    const loadAddresses = async () => {
      try {
        const list = await getSavedAddresses();
        setSavedAddresses(list);

        // Auto-select default address if no draft was present
        const savedDraft = localStorage.getItem(CHECKOUT_DRAFT_KEY);
        if (!savedDraft) {
          const defaultAddr = list.find((a) => a.default) || list[0];
          if (defaultAddr) {
            applyAddress(defaultAddr);
          }
        }
      } catch (err) {
        console.warn("Could not load saved addresses:", err);
      }
    };

    loadAddresses();
  }, []);

  // Save draft whenever address or notes change
  useEffect(() => {
    if (deliveryAddress || deliveryInstructions) {
      localStorage.setItem(
        CHECKOUT_DRAFT_KEY,
        JSON.stringify({
          deliveryAddress,
          deliveryInstructions,
          deliveryLatitude,
          deliveryLongitude,
          selectedAddressId,
        })
      );
    }
  }, [deliveryAddress, deliveryInstructions, deliveryLatitude, deliveryLongitude, selectedAddressId]);

  const applyAddress = (addr: SavedAddress) => {
    setSelectedAddressId(addr.id);
    let full = addr.address;
    if (addr.buildingName) full += `, ${addr.buildingName}`;
    if (addr.apartmentNumber) full += ` (Apt ${addr.apartmentNumber})`;
    if (addr.landmarks) full += ` [Near ${addr.landmarks}]`;

    setDeliveryAddress(full);
    if (addr.deliveryInstructions) {
      setDeliveryInstructions(addr.deliveryInstructions);
    }
    if (addr.latitude && addr.longitude) {
      setDeliveryLatitude(addr.latitude);
      setDeliveryLongitude(addr.longitude);
    }
  };

  const handleSelectSavedAddress = (id: string) => {
    const addr = savedAddresses.find((a) => a.id === id);
    if (addr) {
      applyAddress(addr);
      setLocationMessage(`Applied saved address: "${addr.label}"`);
    } else {
      setSelectedAddressId("");
    }
  };

  const handleUseCurrentLocation = async () => {
    setError("");
    setLocationMessage("");

    if (!navigator.geolocation) {
      setError("Your browser does not support live location.");
      return;
    }

    try {
      setLocating(true);

      const position = await new Promise<GeolocationPosition>((resolve, reject) => {
        navigator.geolocation.getCurrentPosition(resolve, reject, {
          enableHighAccuracy: true,
          timeout: 15000,
          maximumAge: 0,
        });
      });

      const latitude = Number(position.coords.latitude.toFixed(6));
      const longitude = Number(position.coords.longitude.toFixed(6));

      setDeliveryLatitude(latitude);
      setDeliveryLongitude(longitude);

      const resolvedAddress = await reverseGeocodeLocation(latitude, longitude);
      setDeliveryAddress(resolvedAddress);
      setSelectedAddressId("");
      setLocationMessage(
        "Live location detected and delivery address filled! You can edit or add building details."
      );
    } catch (locationError) {
      console.error("Unable to get current location:", locationError);
      setDeliveryLatitude(null);
      setDeliveryLongitude(null);
      setError(
        "Unable to detect your current location. Please allow location access or enter your address manually."
      );
    } finally {
      setLocating(false);
    }
  };

  const formatPrice = (amount: number) => {
    return new Intl.NumberFormat("en-KE", {
      style: "currency",
      currency: "KES",
    }).format(amount);
  };

  const subtotal = cart?.totalAmount ?? 0;
  const deliveryFee = cart?.deliveryFee ?? 150;
  const serviceFee = cart?.serviceFee ?? 35;
  const discountAmount = cart?.discountAmount ?? 0;
  const finalTotal = cart?.finalTotalAmount ?? (subtotal > 0 ? subtotal + deliveryFee + serviceFee - discountAmount : 0);

  const handleContinueToPayment = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    setError("");

    if (!cart || cart.items.length === 0) {
      setError("Your cart is empty.");
      return;
    }

    if (cart.hasPriceChanges) {
      setError("Please accept the updated prices before checkout.");
      return;
    }

    if (cart.hasUnavailableItems) {
      setError("Please remove unavailable items before checkout.");
      return;
    }

    if (!deliveryAddress.trim()) {
      setError("Delivery address is required.");
      return;
    }

    const fullDeliveryInfo = deliveryInstructions.trim()
      ? `${deliveryAddress.trim()} (Instructions: ${deliveryInstructions.trim()})`
      : deliveryAddress.trim();

    sessionStorage.setItem("checkoutDeliveryAddress", fullDeliveryInfo);

    if (deliveryLatitude !== null && deliveryLongitude !== null) {
      sessionStorage.setItem("checkoutDeliveryLatitude", String(deliveryLatitude));
      sessionStorage.setItem("checkoutDeliveryLongitude", String(deliveryLongitude));
    } else {
      sessionStorage.removeItem("checkoutDeliveryLatitude");
      sessionStorage.removeItem("checkoutDeliveryLongitude");
    }

    sessionStorage.setItem(
      "checkoutPricing",
      JSON.stringify({
        subtotal,
        deliveryFee,
        serviceFee,
        discountAmount,
        finalTotal,
      })
    );

    navigate("/customer/payment");
  };

  if (!cart) {
    return (
      <div className="min-h-screen bg-slate-100">
        <CustomerHeader />
        <div className="flex h-96 items-center justify-center text-slate-500">
          Loading checkout...
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-100">
      <CustomerHeader />

      <main className="p-6 md:p-8">
        <div className="mx-auto max-w-4xl">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-slate-950">Checkout</h1>
          <p className="mt-2 text-slate-500">
            Confirm your delivery location and review your itemized total before payment.
          </p>
        </div>

        {error && (
          <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-red-700">
            {error}
          </div>
        )}

        <div className="grid gap-8 lg:grid-cols-[1fr_380px]">
          {/* Left Column: Delivery Location Form */}
          <section className="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">
            <h2 className="text-xl font-bold text-slate-900 mb-4">
              1. Delivery Destination
            </h2>

            {/* Saved addresses selector */}
            {savedAddresses.length > 0 && (
              <div className="mb-6 rounded-2xl border border-slate-100 bg-slate-50 p-4">
                <label className="block text-xs font-bold uppercase tracking-wider text-slate-600 mb-2">
                  Choose from Saved Addresses
                </label>
                <div className="flex flex-wrap gap-2">
                  {savedAddresses.map((addr) => (
                    <button
                      key={addr.id}
                      type="button"
                      onClick={() => handleSelectSavedAddress(addr.id)}
                      className={`flex items-center gap-1.5 rounded-xl border px-3 py-2 text-xs font-semibold transition ${
                        selectedAddressId === addr.id
                          ? "border-emerald-600 bg-emerald-50 text-emerald-900 shadow-sm"
                          : "border-slate-200 bg-white text-slate-700 hover:border-slate-300"
                      }`}
                    >
                      <span>{addr.label}</span>
                      {addr.default && (
                        <span className="ml-1 rounded-full bg-emerald-200 px-1.5 py-0.2 text-[10px] text-emerald-800">
                          Default
                        </span>
                      )}
                    </button>
                  ))}
                </div>
              </div>
            )}

            <form onSubmit={handleContinueToPayment} className="space-y-4">
              <div className="flex flex-wrap items-center gap-3">
                <button
                  type="button"
                  disabled={locating}
                  onClick={handleUseCurrentLocation}
                  className="rounded-3xl border border-indigo-200 bg-indigo-50 px-5 py-2.5 text-xs font-bold text-indigo-700 hover:bg-indigo-100 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400 transition"
                >
                  {locating ? "Detecting GPS location..." : "Use Live GPS Location"}
                </button>

                {deliveryLatitude !== null && deliveryLongitude !== null && (
                  <span className="text-xs font-mono text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded-lg border border-emerald-200">
                    GPS: {deliveryLatitude}, {deliveryLongitude}
                  </span>
                )}
              </div>

              {locationMessage && (
                <div className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-2.5 text-xs text-emerald-800">
                  {locationMessage}
                </div>
              )}

              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Street / Area Address <span className="text-red-500">*</span>
                </label>
                <textarea
                  required
                  rows={3}
                  value={deliveryAddress}
                  onChange={(e) => {
                    setDeliveryAddress(e.target.value);
                    setSelectedAddressId("");
                  }}
                  placeholder="e.g. Westlands Commercial Center, Ring Road Parklands"
                  className="w-full rounded-xl border border-slate-300 p-3 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Delivery Notes / Apartment / Gate (Optional)
                </label>
                <input
                  type="text"
                  value={deliveryInstructions}
                  onChange={(e) => setDeliveryInstructions(e.target.value)}
                  placeholder="e.g. Apt 4B, 3rd Floor, Leave at door"
                  className="w-full rounded-xl border border-slate-300 p-3 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
                />
              </div>

              <div className="pt-4 flex gap-3">
                <button
                  type="button"
                  onClick={() => navigate("/customer/cart")}
                  className="rounded-3xl border border-slate-300 px-5 py-3 text-sm font-semibold text-slate-700 hover:bg-slate-50 transition"
                >
                  ← Back to cart
                </button>

                <button
                  type="submit"
                  className="flex-1 rounded-3xl bg-indigo-600 px-5 py-3 text-sm font-semibold text-white shadow-lg shadow-indigo-600/30 hover:bg-indigo-700 transition"
                >
                  Continue to Payment →
                </button>
              </div>
            </form>
          </section>

          {/* Right Column: Complete Itemized Total Breakdown */}
          <aside className="h-fit rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">
            <h2 className="text-lg font-bold text-slate-900 mb-4 pb-3 border-b border-slate-100">
              Receipt Breakdown
            </h2>

            {/* Cart Items List */}
            <div className="space-y-3 mb-5 max-h-56 overflow-y-auto pr-1">
              {cart.items.map((item) => (
                <div key={item.id} className="flex items-start justify-between text-xs pb-2 border-b border-slate-50">
                  <div className="pr-2">
                    <span className="font-semibold text-slate-900">{item.quantity}x {item.name}</span>
                    {item.selectedSize && (
                      <span className="text-slate-500 ml-1">({item.selectedSize})</span>
                    )}
                    {item.selectedAddOns && item.selectedAddOns.length > 0 && (
                      <p className="text-[11px] text-emerald-700 mt-0.5">+{item.selectedAddOns.join(", ")}</p>
                    )}
                  </div>
                  <span className="font-semibold text-slate-800 whitespace-nowrap">
                    {formatPrice(item.currentSubtotal)}
                  </span>
                </div>
              ))}
            </div>

            {/* Price Line Items */}
            <div className="space-y-2.5 text-sm text-slate-600 border-t border-slate-100 pt-3">
              <div className="flex justify-between">
                <span>Items Subtotal ({cart.totalItems})</span>
                <span className="font-semibold text-slate-900">{formatPrice(subtotal)}</span>
              </div>

              <div className="flex justify-between">
                <span>Delivery Fee</span>
                <span>{formatPrice(deliveryFee)}</span>
              </div>

              <div className="flex justify-between">
                <span>Service Fee</span>
                <span>{formatPrice(serviceFee)}</span>
              </div>

              {discountAmount > 0 && (
                <div className="flex justify-between text-emerald-600 font-semibold">
                  <span>Discount</span>
                  <span>-{formatPrice(discountAmount)}</span>
                </div>
              )}

              <div className="border-t border-slate-200 pt-3 mt-3">
                <div className="flex justify-between text-lg font-bold text-slate-950">
                  <span>Final Total</span>
                  <span className="text-indigo-600">{formatPrice(finalTotal)}</span>
                </div>
                <p className="mt-1 text-[11px] text-slate-400">
                  Pay securely with M-Pesa or Cash on Delivery
                </p>
              </div>
            </div>
          </aside>
        </div>
      </div>
    </main>
  </div>
  );
}

export default CheckoutPage;
