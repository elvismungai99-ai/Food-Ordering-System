import { useState } from "react";

interface MenuItem {
  id: number;
  name: string;
  price: number;
  available: boolean;
}

function MenuPage() {
  const [menuItems, setMenuItems] = useState<MenuItem[]>([
    { id: 1, name: "Margherita Pizza", price: 12.99, available: true },
    { id: 2, name: "Chicken Burger", price: 8.5, available: true },
    { id: 3, name: "Veggie Wrap", price: 6.75, available: false },
  ]);

  const toggleAvailability = (id: number) => {
    setMenuItems((prev) =>
      prev.map((item) =>
        item.id === id ? { ...item, available: !item.available } : item
      )
    );
  };

  return (
    <div className="min-h-screen bg-slate-100 p-8">
      <div className="max-w-4xl mx-auto">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-3xl font-semibold text-slate-950">Menu</h1>
          <button className="rounded-3xl bg-indigo-600 px-5 py-2 text-sm font-semibold text-white hover:bg-indigo-700">
            + Add Item
          </button>
        </div>

        <div className="space-y-4">
          {menuItems.map((item) => (
            <div
              key={item.id}
              className="flex items-center justify-between rounded-[24px] bg-white p-5 border border-slate-200 shadow-sm"
            >
              <div>
                <p className="font-semibold text-slate-900">{item.name}</p>
                <p className="text-sm text-slate-500">${item.price.toFixed(2)}</p>
              </div>

              <div className="flex items-center gap-4">
                <span
                  className={`text-xs font-semibold px-3 py-1 rounded-full ${
                    item.available
                      ? "bg-teal-100 text-teal-700"
                      : "bg-slate-200 text-slate-500"
                  }`}
                >
                  {item.available ? "Available" : "Unavailable"}
                </span>
                <button
                  onClick={() => toggleAvailability(item.id)}
                  className="text-sm font-semibold text-indigo-600 hover:text-indigo-700"
                >
                  Toggle
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default MenuPage;