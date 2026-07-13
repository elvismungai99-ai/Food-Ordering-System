import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import api from "../../api/axios";

interface RestaurantData {
  id?: string;
  name: string;
  description: string;
  address: string;
  openingTime: string;
  closingTime: string;
  status?: string;
}

function RestaurantDashboard() {
  const navigate = useNavigate();
  const firstName = localStorage.getItem("firstName") || "Admin";
  const [restaurant, setRestaurant] = useState<RestaurantData | null>(null);
  const [formData, setFormData] = useState<RestaurantData>({
    name: "",
    description: "",
    address: "",
    openingTime: "08:00:00",
    closingTime: "22:00:00",
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [isEditing, setIsEditing] = useState(false);

  useEffect(() => {
    fetchMyRestaurant();
  }, []);

  const fetchMyRestaurant = async () => {
    try {
      const response = await api.get("/restaurants/me");
      setRestaurant(response.data);
      setFormData({
        name: response.data.name || "",
        description: response.data.description || "",
        address: response.data.address || "",
        openingTime: response.data.openingTime || "08:00:00",
        closingTime: response.data.closingTime || "22:00:00",
      });
    } catch (err: any) {
      if (err.response?.status === 404) {
        setRestaurant(null);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>)=> {
    e.preventDefault();
    setSaving(true);
    setMessage("");
    setError("");

    try {
      if (restaurant) {
        await api.put("/restaurants/me", formData);
        setMessage("Restaurant updated successfully!");
        setIsEditing(false);
      } else {
        await api.post("/restaurants", formData);
        setMessage("Restaurant created successfully!");
      }
      fetchMyRestaurant();
    } catch (err: any) {
      setError(err.response?.data || "Something went wrong. Please try again.");
    } finally {
      setSaving(false);
    }
  };

  const handleLogout = () => {
    localStorage.clear();
    navigate("/login");
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-100 flex items-center justify-center">
        <p className="text-slate-500">Loading...</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-100 p-8">
      <div className="max-w-3xl mx-auto">

        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-3xl font-semibold text-slate-950">
              Welcome, {firstName}!
            </h1>
            <p className="mt-1 text-slate-500">Restaurant Admin Dashboard</p>
          </div>
          <button
            onClick={handleLogout}
            className="rounded-3xl bg-red-500 px-5 py-2 text-sm font-semibold text-white hover:bg-red-600 transition"
          >
            Logout
          </button>
        </div>

        {/* Success/Error Messages */}
        {message && (
          <div className="mb-6 rounded-2xl bg-teal-50 border border-teal-200 px-4 py-3 text-sm text-teal-700">
            {message}
          </div>
        )}
        {error && (
          <div className="mb-6 rounded-2xl bg-red-50 border border-red-200 px-4 py-3 text-sm text-red-600">
            {error}
          </div>
        )}

        {/* Restaurant exists — show details or edit form */}
        {restaurant && !isEditing ? (
          <div className="rounded-[24px] bg-white p-6 border border-slate-200 shadow-sm">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-semibold text-slate-900">
                {restaurant.name}
              </h2>
              <span className="text-xs font-semibold px-3 py-1 rounded-full bg-teal-100 text-teal-700">
                {restaurant.status}
              </span>
            </div>
            <p className="text-slate-600 mb-2">{restaurant.description}</p>
            <p className="text-sm text-slate-500 mb-1">
               {restaurant.address}
            </p>
            <p className="text-sm text-slate-500 mb-6">
               {restaurant.openingTime} — {restaurant.closingTime}
            </p>
            <div className="flex gap-3">
              <button
                onClick={() => setIsEditing(true)}
                className="rounded-3xl bg-indigo-600 px-5 py-2 text-sm font-semibold text-white hover:bg-indigo-700 transition"
              >
                Edit Restaurant
              </button>
              <button
                onClick={() => navigate("/restaurant/menu")}
                className="rounded-3xl border border-indigo-600 px-5 py-2 text-sm font-semibold text-indigo-600 hover:bg-indigo-50 transition"
              >
                Manage Menu
              </button>
            </div>
          </div>
        ) : (
          /* Create or Edit Form */
          <div className="rounded-[24px] bg-white p-6 border border-slate-200 shadow-sm">
            <h2 className="text-xl font-semibold text-slate-900 mb-6">
              {restaurant ? "Edit Your Restaurant" : "Create Your Restaurant"}
            </h2>

            <form className="grid gap-5" onSubmit={handleSubmit}>
              <label className="block text-sm font-medium text-slate-700">
                Restaurant Name
                <input
                  className="mt-2 w-full rounded-3xl border border-slate-300 bg-slate-50 px-4 py-3 text-slate-900 outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
                  placeholder="e.g. Mama's Kitchen"
                  required
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                />
              </label>

              <label className="block text-sm font-medium text-slate-700">
                Description
                <textarea
                  className="mt-2 w-full rounded-[20px] border border-slate-300 bg-slate-50 px-4 py-3 text-slate-900 outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
                  placeholder="Describe your restaurant..."
                  rows={3}
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                />
              </label>

              <label className="block text-sm font-medium text-slate-700">
                Address
                <input
                  className="mt-2 w-full rounded-3xl border border-slate-300 bg-slate-50 px-4 py-3 text-slate-900 outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
                  placeholder="e.g. 123 Main St, Nairobi"
                  required
                  value={formData.address}
                  onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                />
              </label>

              <div className="grid gap-5 sm:grid-cols-2">
                <label className="block text-sm font-medium text-slate-700">
                  Opening Time
                  <input
                    type="time"
                    className="mt-2 w-full rounded-3xl border border-slate-300 bg-slate-50 px-4 py-3 text-slate-900 outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
                    value={formData.openingTime}
                    onChange={(e) => setFormData({ ...formData, openingTime: e.target.value })}
                  />
                </label>

                <label className="block text-sm font-medium text-slate-700">
                  Closing Time
                  <input
                    type="time"
                    className="mt-2 w-full rounded-3xl border border-slate-300 bg-slate-50 px-4 py-3 text-slate-900 outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
                    value={formData.closingTime}
                    onChange={(e) => setFormData({ ...formData, closingTime: e.target.value })}
                  />
                </label>
              </div>

              <div className="flex gap-3">
                <button
                  type="submit"
                  disabled={saving}
                  className="rounded-3xl bg-indigo-600 px-5 py-3 text-sm font-semibold text-white shadow-lg shadow-indigo-500/15 transition hover:bg-indigo-700 disabled:opacity-60"
                >
                  {saving ? "Saving..." : restaurant ? "Update Restaurant" : "Create Restaurant"}
                </button>

                {restaurant && (
                  <button
                    type="button"
                    onClick={() => setIsEditing(false)}
                    className="rounded-3xl border border-slate-300 px-5 py-3 text-sm font-semibold text-slate-700 hover:bg-slate-50 transition"
                  >
                    Cancel
                  </button>
                )}
              </div>
            </form>
          </div>
        )}
      </div>
    </div>
  );
}

export default RestaurantDashboard;