import React, { useState, useMemo } from "react";
import type { MenuItem } from "../../services/MenuItemService";

export interface CustomizationOption {
  size?: string;
  addOns: string[];
  removalRequests: string[];
  specialInstructions: string;
  extraPrice: number;
  quantity: number;
}

interface CustomizeItemModalProps {
  item: MenuItem | null;
  isOpen: boolean;
  onClose: () => void;
  onAddToCart: (item: MenuItem, customization: CustomizationOption) => void;
}

const SIZE_OPTIONS = [
  { name: "Regular", priceDelta: 0 },
  { name: "Medium (+KES 100)", priceDelta: 100 },
  { name: "Large (+KES 200)", priceDelta: 200 },
];

const DEFAULT_ADDONS = [
  { name: "Extra Cheese", price: 50 },
  { name: "Crispy Bacon", price: 100 },
  { name: "Fresh Avocado", price: 80 },
  { name: "Double Patty / Meat", price: 150 },
  { name: "Garlic Mayo Sauce", price: 30 },
  { name: "Spicy Peri-Peri Dip", price: 30 },
];

const COMMON_REMOVALS = [
  "No Onions",
  "No Pickles",
  "No Tomato",
  "No Mayonnaise",
  "No Chili / Mild Only",
  "No Mustard",
];

export const CustomizeItemModal: React.FC<CustomizeItemModalProps> = ({
  item,
  isOpen,
  onClose,
  onAddToCart,
}) => {
  const [selectedSize, setSelectedSize] = useState("Regular");
  const [selectedAddOns, setSelectedAddOns] = useState<string[]>([]);
  const [selectedRemovals, setSelectedRemovals] = useState<string[]>([]);
  const [specialInstructions, setSpecialInstructions] = useState("");
  const [quantity, setQuantity] = useState(1);

  // Compute live extra price per single item
  const extraPrice = useMemo(() => {
    let extra = 0;
    const sizeObj = SIZE_OPTIONS.find((s) => s.name === selectedSize);
    if (sizeObj) extra += sizeObj.priceDelta;

    for (const addOnName of selectedAddOns) {
      const found = DEFAULT_ADDONS.find((a) => a.name === addOnName);
      if (found) extra += found.price;
      else extra += 50; // default for dynamic custom add-ons
    }
    return extra;
  }, [selectedSize, selectedAddOns]);

  if (!isOpen || !item) return null;

  const basePrice = item.price || 0;
  const unitPrice = basePrice + extraPrice;
  const totalPrice = unitPrice * quantity;

  const handleToggleAddOn = (name: string) => {
    setSelectedAddOns((prev) =>
      prev.includes(name) ? prev.filter((i) => i !== name) : [...prev, name]
    );
  };

  const handleToggleRemoval = (name: string) => {
    setSelectedRemovals((prev) =>
      prev.includes(name) ? prev.filter((i) => i !== name) : [...prev, name]
    );
  };

  const handleConfirm = () => {
    onAddToCart(item, {
      size: selectedSize,
      addOns: selectedAddOns,
      removalRequests: selectedRemovals,
      specialInstructions: specialInstructions.trim(),
      extraPrice,
      quantity,
    });
    // Reset and close
    setSelectedSize("Regular");
    setSelectedAddOns([]);
    setSelectedRemovals([]);
    setSpecialInstructions("");
    setQuantity(1);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm animate-fadeIn">
      <div
        className="relative flex max-h-[90vh] w-full max-w-lg flex-col overflow-hidden rounded-2xl bg-white shadow-2xl border border-slate-200"
        role="dialog"
        aria-modal="true"
      >
        {/* Header */}
        <div className="flex items-center justify-between border-b border-slate-100 px-6 py-4">
          <div>
            <h3 className="text-xl font-bold text-slate-900">{item.name}</h3>
            <p className="text-sm font-medium text-emerald-600">
              Base Price: KES {basePrice.toLocaleString()}
            </p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded-full p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600 transition"
          >
            ✕
          </button>
        </div>

        {/* Scrollable Customization Body */}
        <div className="overflow-y-auto px-6 py-5 space-y-6 flex-1">
          {/* Item image if available */}
          {item.imageUrl && (
            <div className="h-40 w-full overflow-hidden rounded-xl bg-slate-100">
              <img
                src={item.imageUrl}
                alt={item.name}
                className="h-full w-full object-cover"
              />
            </div>
          )}

          {/* 1. Size Selection */}
          <div>
            <h4 className="text-sm font-bold uppercase tracking-wider text-slate-700 mb-3">
              1. Choose Size
            </h4>
            <div className="grid grid-cols-3 gap-2">
              {SIZE_OPTIONS.map((opt) => (
                <button
                  key={opt.name}
                  type="button"
                  onClick={() => setSelectedSize(opt.name)}
                  className={`rounded-xl border p-3 text-center transition ${
                    selectedSize === opt.name
                      ? "border-emerald-600 bg-emerald-50 text-emerald-900 font-bold shadow-sm"
                      : "border-slate-200 hover:border-slate-300 text-slate-700 bg-white"
                  }`}
                >
                  <div className="text-xs font-semibold">{opt.name}</div>
                </button>
              ))}
            </div>
          </div>

          {/* 2. Extra Add-ons / Toppings */}
          <div>
            <h4 className="text-sm font-bold uppercase tracking-wider text-slate-700 mb-3">
              2. Extra Toppings & Add-ons
            </h4>
            <div className="space-y-2">
              {DEFAULT_ADDONS.map((addOn) => {
                const checked = selectedAddOns.includes(addOn.name);
                return (
                  <label
                    key={addOn.name}
                    className={`flex items-center justify-between p-3 rounded-xl border cursor-pointer transition ${
                      checked
                        ? "border-emerald-500 bg-emerald-50/50"
                        : "border-slate-200 hover:bg-slate-50"
                    }`}
                  >
                    <div className="flex items-center gap-3">
                      <input
                        type="checkbox"
                        checked={checked}
                        onChange={() => handleToggleAddOn(addOn.name)}
                        className="h-4 w-4 rounded text-emerald-600 focus:ring-emerald-500"
                      />
                      <span className="text-sm font-medium text-slate-800">
                        {addOn.name}
                      </span>
                    </div>
                    <span className="text-xs font-bold text-emerald-700 bg-emerald-100 px-2 py-0.5 rounded-full">
                      +KES {addOn.price}
                    </span>
                  </label>
                );
              })}
            </div>
          </div>

          {/* 3. Removal Requests */}
          <div>
            <h4 className="text-sm font-bold uppercase tracking-wider text-slate-700 mb-3">
              3. Ingredients to Remove
            </h4>
            <div className="grid grid-cols-2 gap-2">
              {COMMON_REMOVALS.map((rem) => {
                const checked = selectedRemovals.includes(rem);
                return (
                  <label
                    key={rem}
                    className={`flex items-center gap-2 p-2.5 rounded-lg border text-xs cursor-pointer transition ${
                      checked
                        ? "border-rose-400 bg-rose-50 text-rose-800 font-semibold"
                        : "border-slate-200 hover:bg-slate-50 text-slate-700"
                    }`}
                  >
                    <input
                      type="checkbox"
                      checked={checked}
                      onChange={() => handleToggleRemoval(rem)}
                      className="h-3.5 w-3.5 rounded text-rose-600 focus:ring-rose-500"
                    />
                    <span>{rem}</span>
                  </label>
                );
              })}
            </div>
          </div>

          {/* 4. Special Preparation Instructions */}
          <div>
            <h4 className="text-sm font-bold uppercase tracking-wider text-slate-700 mb-2">
              4. Special Preparation Notes
            </h4>
            <textarea
              rows={2}
              value={specialInstructions}
              onChange={(e) => setSpecialInstructions(e.target.value)}
              placeholder="e.g. Extra napkins, sauce on the side, well done..."
              className="w-full rounded-xl border border-slate-200 p-3 text-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500"
            />
          </div>
        </div>

        {/* Footer with Quantity & Add to Cart */}
        <div className="border-t border-slate-100 bg-slate-50 px-6 py-4 flex items-center justify-between gap-4">
          <div className="flex items-center gap-2 border border-slate-200 rounded-xl bg-white p-1">
            <button
              type="button"
              onClick={() => setQuantity((q) => Math.max(1, q - 1))}
              className="h-8 w-8 rounded-lg text-slate-600 font-bold hover:bg-slate-100 flex items-center justify-center transition"
            >
              -
            </button>
            <span className="w-8 text-center text-sm font-bold text-slate-900">
              {quantity}
            </span>
            <button
              type="button"
              onClick={() => setQuantity((q) => Math.min(99, q + 1))}
              className="h-8 w-8 rounded-lg text-slate-600 font-bold hover:bg-slate-100 flex items-center justify-center transition"
            >
              +
            </button>
          </div>

          <button
            type="button"
            onClick={handleConfirm}
            className="flex-1 rounded-xl bg-emerald-600 py-3 px-4 text-center font-bold text-white shadow-lg shadow-emerald-600/30 hover:bg-emerald-700 transition"
          >
            Add to Cart • KES {totalPrice.toLocaleString()}
          </button>
        </div>
      </div>
    </div>
  );
};

