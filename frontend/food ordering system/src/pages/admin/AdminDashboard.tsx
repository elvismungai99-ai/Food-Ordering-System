import { useEffect, useState } from "react";
import {
  getAllCustomers,
  deleteCustomer,
  getAllRestaurants,
  deleteRestaurant,
  Customer,
  Restaurant,
} from "../../services/AdminService";

function AdminDashboard() {
  console.log("AdminDashboard component rendered!");
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadData = async () => {
    setLoading(true);
    setError("");
    try {
      const [customersData, restaurantsData] = await Promise.all([
        getAllCustomers(),
        getAllRestaurants(),
      ]);
      setCustomers(customersData);
      setRestaurants(restaurantsData);
    } catch (err) {
      setError("Failed to load admin data.");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleDeleteCustomer = async (id: string, name: string) => {
    if (!window.confirm(`Delete customer "${name}"? This cannot be undone.`)) return;
    try {
      await deleteCustomer(id);
      setCustomers((prev) => prev.filter((c) => c.id !== id));
    } catch (err) {
      alert("Failed to delete customer.");
      console.error(err);
    }
  };

  const handleDeleteRestaurant = async (id: string, name: string) => {
    if (!window.confirm(`Delete restaurant "${name}"? This cannot be undone.`)) return;
    try {
      await deleteRestaurant(id);
      setRestaurants((prev) => prev.filter((r) => r.id !== id));
    } catch (err) {
      alert("Failed to delete restaurant.");
      console.error(err);
    }
  };

  if (loading) return <div className="p-8 text-slate-500">Loading admin dashboard...</div>;
  if (error) return <div className="p-8 text-red-600">{error}</div>;

  return (
    <div className="min-h-screen bg-slate-100 px-6 py-10">
      <h1 className="text-3xl font-semibold text-slate-950 mb-8">Super Admin Dashboard</h1>

      {/* Customers */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold text-slate-800 mb-4">
          Customers ({customers.length})
        </h2>
        <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden">
          {customers.length === 0 ? (
            <p className="p-4 text-slate-500">No customers found.</p>
          ) : (
            <table className="w-full text-sm text-left">
              <thead className="bg-slate-50 text-slate-600">
                <tr>
                  <th className="px-4 py-3">Name</th>
                  <th className="px-4 py-3">Email</th>
                  <th className="px-4 py-3">Joined</th>
                  <th className="px-4 py-3"></th>
                </tr>
              </thead>
              <tbody>
                {customers.map((c) => (
                  <tr key={c.id} className="border-t border-slate-100">
                    <td className="px-4 py-3">{c.fullName}</td>
                    <td className="px-4 py-3">{c.email}</td>
                    <td className="px-4 py-3">{new Date(c.createdAt).toLocaleDateString()}</td>
                    <td className="px-4 py-3 text-right">
                      <button
                        onClick={() => handleDeleteCustomer(c.id, c.fullName)}
                        className="text-red-600 hover:text-red-800 font-medium"
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </section>

      {/* Restaurants */}
      <section>
        <h2 className="text-xl font-semibold text-slate-800 mb-4">
          Restaurants ({restaurants.length})
        </h2>
        <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden">
          {restaurants.length === 0 ? (
            <p className="p-4 text-slate-500">No restaurants found.</p>
          ) : (
            <table className="w-full text-sm text-left">
              <thead className="bg-slate-50 text-slate-600">
                <tr>
                  <th className="px-4 py-3">Name</th>
                  <th className="px-4 py-3">Address</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3"></th>
                </tr>
              </thead>
              <tbody>
                {restaurants.map((r) => (
                  <tr key={r.id} className="border-t border-slate-100">
                    <td className="px-4 py-3">{r.name}</td>
                    <td className="px-4 py-3">{r.address}</td>
                    <td className="px-4 py-3">{r.status}</td>
                    <td className="px-4 py-3 text-right">
                      <button
                        onClick={() => handleDeleteRestaurant(r.id, r.name)}
                        className="text-red-600 hover:text-red-800 font-medium"
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </section>
    </div>
  );
}

export default AdminDashboard;