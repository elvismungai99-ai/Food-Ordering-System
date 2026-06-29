function CustomerDashboard() {
  const firstName = localStorage.getItem("firstName") || "Customer";

  return (
    <div className="min-h-screen bg-slate-100 p-8">
      <div className="max-w-5xl mx-auto">
        <div className="mb-8">
          <h1 className="text-3xl font-semibold text-slate-950">
            Welcome, {firstName}!
          </h1>
          <p className="mt-2 text-slate-500">This is your customer dashboard.</p>
        </div>

        <div className="grid gap-6 sm:grid-cols-3">
          <div className="rounded-[24px] bg-white p-6 shadow-sm border border-slate-200">
            <p className="text-sm uppercase tracking-widest text-indigo-600 font-medium">Restaurants</p>
            <p className="mt-2 text-slate-700">Browse available restaurants and menus.</p>
          </div>
          <div className="rounded-[24px] bg-white p-6 shadow-sm border border-slate-200">
            <p className="text-sm uppercase tracking-widest text-teal-600 font-medium">My Orders</p>
            <p className="mt-2 text-slate-700">Track and view your order history.</p>
          </div>
          <div className="rounded-[24px] bg-white p-6 shadow-sm border border-slate-200">
            <p className="text-sm uppercase tracking-widest text-slate-600 font-medium">My Cart</p>
            <p className="mt-2 text-slate-700">View and manage your current cart.</p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default CustomerDashboard;