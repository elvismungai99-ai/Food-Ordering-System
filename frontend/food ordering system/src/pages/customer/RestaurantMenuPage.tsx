import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getMenuByRestaurant, MenuItem } from "../../services/MenuItemService";

function RestaurantMenuPage() {
  const { restaurantId } = useParams<{ restaurantId: string }>();
  const navigate = useNavigate();
  const [menuItems, setMenuItems] = useState<MenuItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

    useEffect(() => {
    if (!restaurantId) {
        setError("no selected restaurants.");
        setLoading(false);
        return;
    }
    const loadMenu = async () => {
      setLoading(true);
      setError("");
      try {
        const items = await getMenuByRestaurant(restaurantId);
        setMenuItems(items);
      } catch (err) {
        setError("Failed to load menu. Please try again.");
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    loadMenu();
  }, [restaurantId]);
  
  if (loading) {
    return (
      <div className="min-h-screen bg-slate-100 flex items-center justify-center">
        <p className="text-slate-500">Loading menu...</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-100 p-8">
      <div className="max-w-4xl mx-auto">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-3xl font-semibold text-slate-950">Menu</h1>
          <button
            onClick={() => navigate("/customer/restaurants")}
            className="rounded-3xl border border-slate-300 px-5 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50 transition"
          >
            ← Back
          </button>
        </div>

        {error && (
          <div className="mb-6 rounded-2xl bg-red-50 border border-red-200 px-4 py-3 text-sm text-red-600">
            {error}
          </div>
        )}

        {menuItems.length === 0 ? (
          <div className="rounded-[24px] bg-white p-8 text-center border border-slate-200">
            <p className="text-slate-500">No menu items available yet. Check back soon!</p>
          </div>
        ) : (
          <div className="grid gap-6 sm:grid-cols-2">
            {menuItems
              .filter((item) => item.available)
              .map((item) => (
                <div
                  key={item.id}
                  className="rounded-[24px] bg-white p-6 border border-slate-200 shadow-sm"
                >
                  {item.imageUrl && (
                    <img
                      src={item.imageUrl}
                      alt={item.name}
                      className="w-full h-40 object-cover rounded-2xl mb-4"
                    />
                  )}
                  <div className="flex items-start justify-between mb-2">
                    <p className="font-semibold text-slate-900 text-lg">{item.name}</p>
                    <p className="font-semibold text-indigo-600">KES {item.price.toFixed(2)}</p>
                  </div>
                  <p className="text-xs uppercase tracking-wide text-slate-400 mb-2">
                    {item.category}
                  </p>
                  <p className="text-sm text-slate-500">{item.description}</p>
                </div>
              ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default RestaurantMenuPage;