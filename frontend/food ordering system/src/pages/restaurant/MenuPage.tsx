import { useEffect, useState } from "react";
import api from "../../api/axios";
import {
  getMenuByRestaurant,
  createMenuItem,
  deleteMenuItem,
  updateMenuItem,
  MenuItem,
} from "../../services/MenuItemService";

function MenuPage() {
  const [restaurantId, setRestaurantId] = useState<string | null>(null);
  const [menuItems, setMenuItems] = useState<MenuItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    price: "",
    category: "",
    imageUrl: "",
  });

  const authHeader = () => ({
    headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
  });

  const loadData = async () => {
    setLoading(true);
    setError("");
    try {
      const restaurantResponse = await api.get("/restaurants/me", authHeader());
      const myRestaurantId = restaurantResponse.data.id;
      setRestaurantId(myRestaurantId);

      const items = await getMenuByRestaurant(myRestaurantId);
      setMenuItems(items);
    } catch (err) {
      setError("Failed to load your restaurant's menu.");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleAddItem = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const newItem = await createMenuItem({
        name: formData.name,
        description: formData.description,
        price: parseFloat(formData.price),
        category: formData.category,
        imageUrl: formData.imageUrl,
        available: true,
      });
      setMenuItems((prev) => [...prev, newItem]);
      setFormData({ name: "", description: "", price: "", category: "", imageUrl: "" });
      setShowForm(false);
    } catch (err) {
      alert("Failed to add menu item.");
      console.error(err);
    }
  };

  const toggleAvailability = async (item: MenuItem) => {
    try {
      const updated = await updateMenuItem(item.id, { ...item, available: !item.available });
      setMenuItems((prev) => prev.map((i) => (i.id === item.id ? updated : i)));
    } catch (err) {
      alert("Failed to update item.");
      console.error(err);
    }
  };

  const handleDelete = async (id: string, name: string) => {
    if (!window.confirm(`Delete "${name}"? This cannot be undone.`)) return;
    try {
      await deleteMenuItem(id);
      setMenuItems((prev) => prev.filter((i) => i.id !== id));
    } catch (err) {
      alert("Failed to delete item.");
      console.error(err);
    }
  };

  if (loading) return <div className="p-8 text-slate-500">Loading menu...</div>;
  if (error) return <div className="p-8 text-red-600">{error}</div>;

  return (
    <div className="min-h-screen bg-slate-100 p-8">
      <div className="max-w-4xl mx-auto">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-3xl font-semibold text-slate-950">Menu</h1>
          <button
            onClick={() => setShowForm((prev) => !prev)}
            className="rounded-3xl bg-indigo-600 px-5 py-2 text-sm font-semibold text-white hover:bg-indigo-700"
          >
            {showForm ? "Cancel" : "+ Add Item"}
          </button>
        </div>

        {showForm && (
          <form
            onSubmit={handleAddItem}
            className="mb-6 rounded-[24px] bg-white p-6 border border-slate-200 shadow-sm space-y-4"
          >
            <input
              className="w-full rounded-xl border border-slate-300 px-4 py-2"
              placeholder="Item name"
              required
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            />
            <textarea
              className="w-full rounded-xl border border-slate-300 px-4 py-2"
              placeholder="Description"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            />
            <input
              className="w-full rounded-xl border border-slate-300 px-4 py-2"
              placeholder="Price"
              type="number"
              step="0.01"
              required
              value={formData.price}
              onChange={(e) => setFormData({ ...formData, price: e.target.value })}
            />
            <input
              className="w-full rounded-xl border border-slate-300 px-4 py-2"
              placeholder="Category (e.g. Mains, Drinks)"
              required
              value={formData.category}
              onChange={(e) => setFormData({ ...formData, category: e.target.value })}
            />
            <input
              className="w-full rounded-xl border border-slate-300 px-4 py-2"
              placeholder="Image URL (optional)"
              value={formData.imageUrl}
              onChange={(e) => setFormData({ ...formData, imageUrl: e.target.value })}
            />
            <button
              type="submit"
              className="w-full rounded-3xl bg-indigo-600 px-5 py-2 text-sm font-semibold text-white hover:bg-indigo-700"
            >
              Save Item
            </button>
          </form>
        )}

        <div className="space-y-4">
          {menuItems.length === 0 ? (
            <p className="text-slate-500">No menu items yet. Add your first one above.</p>
          ) : (
            menuItems.map((item) => (
              <div
                key={item.id}
                className="flex items-center justify-between rounded-[24px] bg-white p-5 border border-slate-200 shadow-sm"
              >
                <div>
                  <p className="font-semibold text-slate-900">{item.name}</p>
                  <p className="text-sm text-slate-500">
                    KES {item.price.toFixed(2)} · {item.category}
                  </p>
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
                    onClick={() => toggleAvailability(item)}
                    className="text-sm font-semibold text-indigo-600 hover:text-indigo-700"
                  >
                    Toggle
                  </button>
                  <button
                    onClick={() => handleDelete(item.id, item.name)}
                    className="text-sm font-semibold text-red-600 hover:text-red-700"
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}

export default MenuPage;