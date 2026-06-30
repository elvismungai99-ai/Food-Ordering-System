interface Restaurant {
  id: number;
  name: string;
  cuisine: string;
  rating: number;
  deliveryTime: string;
}

function RestaurantPage() {
  const restaurants: Restaurant[] = [
    { id: 1, name: "Mama's Kitchen", cuisine: "Italian", rating: 4.6, deliveryTime: "25-35 min" },
    { id: 2, name: "Burger Hub", cuisine: "American", rating: 4.3, deliveryTime: "20-30 min" },
    { id: 3, name: "Spice Route", cuisine: "Indian", rating: 4.8, deliveryTime: "30-40 min" },
    { id: 4, name: "Green Bowl", cuisine: "Healthy", rating: 4.5, deliveryTime: "15-25 min" },
  ];

  return (
    <div className="min-h-screen bg-slate-100 p-8">
      <div className="max-w-5xl mx-auto">
        <h1 className="text-3xl font-semibold text-slate-950 mb-6">Restaurants</h1>

        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {restaurants.map((restaurant) => (
            <div
              key={restaurant.id}
              className="rounded-[24px] bg-white p-6 border border-slate-200 shadow-sm hover:shadow-md transition cursor-pointer"
            >
              <p className="font-semibold text-slate-900 text-lg">{restaurant.name}</p>
              <p className="text-sm text-slate-500 mt-1">{restaurant.cuisine}</p>

              <div className="flex items-center justify-between mt-4">
                <span className="text-sm font-medium text-amber-600">
                  ★ {restaurant.rating.toFixed(1)}
                </span>
                <span className="text-xs text-slate-400">{restaurant.deliveryTime}</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default RestaurantPage;