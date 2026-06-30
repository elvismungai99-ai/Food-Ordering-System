import { useState } from "react";

interface CartItem {
  id: number;
  name: string;
  price: number;
  quantity: number;
}

function CartPage() {
  const [cartItems, setCartItems] = useState<CartItem[]>([
    { id: 1, name: "Margherita Pizza", price: 12.99, quantity: 1 },
    { id: 2, name: "Chicken Burger", price: 8.5, quantity: 2 },
  ]);

  const updateQuantity = (id: number, delta: number) => {
    setCartItems((prev) =>
      prev
        .map((item) =>
          item.id === id ? { ...item, quantity: Math.max(1, item.quantity + delta) } : item
        )
        .filter((item) => item.quantity > 0)
    );
  };

  const removeItem = (id: number) => {
    setCartItems((prev) => prev.filter((item) => item.id !== id));
  };

  const total = cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0);

  return (
    <div className="min-h-screen bg-slate-100 p-8">
      <div className="max-w-3xl mx-auto">
        <h1 className="text-3xl font-semibold text-slate-950 mb-6">My Cart</h1>
        {cartItems.length === 0 ? (
          <div className="rounded-[24px] bg-white p-8 text-center border border-slate-200">
            <p className="text-slate-500">Your cart is empty.</p>
          </div>
        ) : (
          <div className="space-y-4">
            {cartItems.map((item) => (
              <div key={item.id} className="flex items-center justify-between rounded-[24px] bg-white p-5 border border-slate-200 shadow-sm">
                <div>
                  <p className="font-semibold text-slate-900">{item.name}</p>
                  <p className="text-sm text-slate-500">${item.price.toFixed(2)} each</p>
                </div>
                <div className="flex items-center gap-3">
                  <button onClick={() => updateQuantity(item.id, -1)} className="w-8 h-8 rounded-full bg-slate-100 text-slate-700 font-bold hover:bg-slate-200">-</button>
                  <span className="w-6 text-center">{item.quantity}</span>
                  <button onClick={() => updateQuantity(item.id, 1)} className="w-8 h-8 rounded-full bg-slate-100 text-slate-700 font-bold hover:bg-slate-200">+</button>
                  <button onClick={() => removeItem(item.id)} className="ml-4 text-sm font-semibold text-red-500 hover:text-red-600">Remove</button>
                </div>
              </div>
            ))}
            <div className="flex justify-between items-center rounded-[24px] bg-white p-5 border border-slate-200 shadow-sm mt-6">
              <p className="text-lg font-semibold text-slate-900">Total</p>
              <p className="text-lg font-semibold text-indigo-600">${total.toFixed(2)}</p>
            </div>
            <button className="mt-6 w-full rounded-3xl bg-indigo-600 px-5 py-3 text-sm font-semibold text-white shadow-lg shadow-indigo-500/15 transition hover:bg-indigo-700">
              Proceed to Checkout
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

export default CartPage;