function RestaurantDashboard() {
  const firstName = localStorage.getItem("firstName") || "Restaurant Admin";

  return (
    <div className="min-h-screen bg-slate-100 p-8">
      <div className="max-w-5xl mx-auto">
        <h1 className="text-3xl font-semibold text-slate-950">
          Welcome, {firstName}!
        </h1>
        <p className="mt-2 text-slate-500">This is your restaurant dashboard.</p>
      </div>
    </div>
  );
}

export default RestaurantDashboard;
