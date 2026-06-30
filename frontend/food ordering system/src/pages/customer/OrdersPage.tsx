interface Order {
  id: number;
  restaurant: string;
  items: string[];
  total: number;
  status: "Pending" | "Preparing" | "Delivered" | "Cancelled";
  date: string;
}

function OrdersPage() {
  const orders: Order[] = [
    {
      id: 101,
      restaurant: "Mama's Kitchen",
      items: ["Margherita Pizza", "Garlic Bread"],
      total: 18.49,
      status: "Delivered",
      date: "2026-06-25",
    },
    {
      id: 102,
      restaurant: "Burger Hub",
      items: ["Chicken Burger", "Fries"],
      total: 11.0,
      status: "Preparing",
      date: "2026-06-29",
    },
  ];

  const statusColor: Record<Order["status"], string> = {
    Pending: "bg-yellow-100 text-yellow-700",
    Preparing: "bg-indigo-100 text-indigo-700",
    Delivered: "bg-teal-100 text-teal-700",
    Cancelled: "bg-red-100 text-red-700",
  };

  return (
    <div className="min-h-screen bg-slate-100 p-8">
      <div className="max-w-3xl mx-auto">
        <h1 className="text-3xl font-semibold text-slate-950 mb-6">My Orders</h1>

        {orders.length === 0 ? (
          <div className="rounded-[24px] bg-white p-8 text-center border border-slate-200">
            <p className="text-slate-500">You have no orders yet.</p>
          </div>
        ) : (
          <div className="space-y-4">
            {orders.map((order) => (
              <div
                key={order.id}
                className="rounded-[24px] bg-white p-5 border border-slate-200 shadow-sm"
              >
                <div className="flex items-center justify-between mb-2">
                  <p className="font-semibold text-slate-900">{order.restaurant}</p>
                  <span
                    className={`text-xs font-semibold px-3 py-1 rounded-full ${statusColor[order.status]}`}
                  >
                    {order.status}
                  </span>
                </div>
                <p className="text-sm text-slate-500">{order.items.join(", ")}</p>
                <div className="flex items-center justify-between mt-3">
                  <p className="text-xs text-slate-400">{order.date}</p>
                  <p className="font-semibold text-slate-900">${order.total.toFixed(2)}</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default OrdersPage;