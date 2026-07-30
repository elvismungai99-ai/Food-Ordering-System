import { useEffect, useState } from "react";
import {
  getAllCustomers,
  deleteCustomer,
  getAllRestaurants,
  getAllRiders,
  getRiderActivities,
  deleteRestaurant,
  updateRestaurantStatus,
  updateRiderStatus,
  Customer,
  DeliveryActivity,
  Restaurant,
  Rider,
  RiderStatus,
} from "../../services/AdminService";
import {
  getApiErrorMessage,
} from "../../utils/apiError";

function AdminDashboard() {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [riders, setRiders] = useState<Rider[]>([]);
  const [riderActivities, setRiderActivities] = useState<DeliveryActivity[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [deletingId, setDeletingId] = useState("");
  const [updatingRestaurantId, setUpdatingRestaurantId] = useState("");
  const [updatingRiderId, setUpdatingRiderId] = useState("");

  const loadData = async () => {
    setLoading(true);
    setError("");
    try {
      const [
        customersData,
        restaurantsData,
        ridersData,
        riderActivitiesData,
      ] = await Promise.all([
        getAllCustomers(),
        getAllRestaurants(),
        getAllRiders(),
        getRiderActivities(),
      ]);
      setCustomers(customersData);
      setRestaurants(restaurantsData);
      setRiders(ridersData);
      setRiderActivities(riderActivitiesData);
    } catch (err) {
      setError(
        getApiErrorMessage(err)
      );
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
      setDeletingId(id);
      setError("");
      await deleteCustomer(id);
      setCustomers((prev) => prev.filter((c) => c.id !== id));
    } catch (err) {
      setError(
        getApiErrorMessage(err)
      );
      console.error(err);
    } finally {
      setDeletingId("");
    }
  };

  const handleDeleteRestaurant = async (id: string, name: string) => {
    if (!window.confirm(`Delete restaurant "${name}"? This cannot be undone.`)) return;
    try {
      setDeletingId(id);
      setError("");
      await deleteRestaurant(id);
      setRestaurants((prev) => prev.filter((r) => r.id !== id));
    } catch (err) {
      setError(
        getApiErrorMessage(err)
      );
      console.error(err);
    } finally {
      setDeletingId("");
    }
  };

  const handleRestaurantStatus = async (
    id: string,
    status:
      | "PENDING_APPROVAL"
      | "APPROVED"
      | "SUSPENDED"
      | "REJECTED"
  ) => {
    try {
      setUpdatingRestaurantId(id);
      setError("");

      const updated =
        await updateRestaurantStatus(
          id,
          status
        );

      setRestaurants(current =>
        current.map(restaurant =>
          restaurant.id === id
            ? updated
            : restaurant
        )
      );
    } catch (err) {
      setError(
        getApiErrorMessage(err)
      );
      console.error(err);
    } finally {
      setUpdatingRestaurantId("");
    }
  };

  const handleRiderStatus = async (
    id: string,
    status: RiderStatus
  ) => {
    try {
      setUpdatingRiderId(id);
      setError("");

      const updated =
        await updateRiderStatus(
          id,
          status
        );

      setRiders(current =>
        current.map(rider =>
          rider.id === id
            ? updated
            : rider
        )
      );
    } catch (err) {
      setError(
        getApiErrorMessage(err)
      );
      console.error(err);
    } finally {
      setUpdatingRiderId("");
    }
  };

  const formatCurrency = (
    amount: number
  ) =>
    new Intl.NumberFormat(
      "en-KE",
      {
        style: "currency",
        currency: "KES",
      }
    ).format(amount || 0);

  const formatDate = (
    value?: string | null
  ) =>
    value
      ? new Date(value).toLocaleString("en-KE")
      : "-";

  const formatStatus = (
    status: string
  ) =>
    status.replaceAll(
      "_",
      " "
    );

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
                        disabled={deletingId === c.id}
                        onClick={() => handleDeleteCustomer(c.id, c.fullName)}
                        className="text-red-600 hover:text-red-800 font-medium disabled:opacity-50"
                      >
                        {deletingId === c.id
                          ? "Deleting..."
                          : "Delete"}
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
      <section className="mb-10">
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
                  <th className="px-4 py-3">Approval</th>
                  <th className="px-4 py-3"></th>
                </tr>
              </thead>
              <tbody>
                {restaurants.map((r) => (
                  <tr key={r.id} className="border-t border-slate-100">
                    <td className="px-4 py-3">{r.name}</td>
                    <td className="px-4 py-3">{r.address}</td>
                    <td className="px-4 py-3">{r.status}</td>
                    <td className="px-4 py-3">
                      <div className="flex flex-wrap gap-2">
                        {([
                          "APPROVED",
                          "REJECTED",
                          "SUSPENDED",
                        ] as const).map(status => (
                          <button
                            key={status}
                            type="button"
                            disabled={
                              updatingRestaurantId === r.id
                              || r.status === status
                            }
                            onClick={() =>
                              handleRestaurantStatus(
                                r.id,
                                status
                              )
                            }
                            className="rounded-full border border-slate-300 px-3 py-1 text-xs font-semibold text-slate-700 disabled:opacity-40"
                          >
                            {status.replace("_", " ")}
                          </button>
                        ))}
                      </div>
                    </td>
                    <td className="px-4 py-3 text-right">
                      <button
                        disabled={deletingId === r.id}
                        onClick={() => handleDeleteRestaurant(r.id, r.name)}
                        className="text-red-600 hover:text-red-800 font-medium disabled:opacity-50"
                      >
                        {deletingId === r.id
                          ? "Deleting..."
                          : "Delete"}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </section>

      {/* Riders */}
      <section className="mb-10">
        <h2 className="text-xl font-semibold text-slate-800 mb-4">
          Delivery Riders ({riders.length})
        </h2>
        <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden">
          {riders.length === 0 ? (
            <p className="p-4 text-slate-500">No riders found.</p>
          ) : (
            <table className="w-full text-sm text-left">
              <thead className="bg-slate-50 text-slate-600">
                <tr>
                  <th className="px-4 py-3">Name</th>
                  <th className="px-4 py-3">Contact</th>
                  <th className="px-4 py-3">Vehicle</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3">Availability</th>
                  <th className="px-4 py-3">Activity</th>
                  <th className="px-4 py-3">Approval</th>
                </tr>
              </thead>
              <tbody>
                {riders.map((rider) => (
                  <tr key={rider.id} className="border-t border-slate-100 align-top">
                    <td className="px-4 py-3">
                      <p className="font-medium text-slate-900">
                        {rider.fullName}
                      </p>
                      <p className="text-xs text-slate-500">
                        Joined {formatDate(rider.createdAt)}
                      </p>
                    </td>
                    <td className="px-4 py-3">
                      <p>{rider.email}</p>
                      <p className="text-xs text-slate-500">
                        {rider.phoneNumber}
                      </p>
                    </td>
                    <td className="px-4 py-3">
                      <p>{rider.vehicleType}</p>
                      <p className="text-xs text-slate-500">
                        {rider.licencePlate}
                      </p>
                    </td>
                    <td className="px-4 py-3">
                      {formatStatus(rider.status)}
                    </td>
                    <td className="px-4 py-3">
                      <p>{formatStatus(rider.operationalStatus)}</p>
                      <p className="text-xs text-slate-500">
                        {rider.online ? "Online" : "Offline"}
                      </p>
                    </td>
                    <td className="px-4 py-3">
                      <p>{rider.totalRejections} rejections</p>
                      <p className="text-xs text-slate-500">
                        Location {formatDate(rider.lastLocationUpdatedAt)}
                      </p>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex flex-wrap gap-2">
                        {([
                          "APPROVED",
                          "REJECTED",
                          "SUSPENDED",
                        ] as const).map(status => (
                          <button
                            key={status}
                            type="button"
                            disabled={
                              updatingRiderId === rider.id
                              || rider.status === status
                            }
                            onClick={() =>
                              handleRiderStatus(
                                rider.id,
                                status
                              )
                            }
                            className="rounded-full border border-slate-300 px-3 py-1 text-xs font-semibold text-slate-700 disabled:opacity-40"
                          >
                            {formatStatus(status)}
                          </button>
                        ))}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </section>

      {/* Rider Activity */}
      <section>
        <h2 className="text-xl font-semibold text-slate-800 mb-4">
          Rider Activity ({riderActivities.length})
        </h2>
        <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden">
          {riderActivities.length === 0 ? (
            <p className="p-4 text-slate-500">No rider activity found.</p>
          ) : (
            <table className="w-full text-sm text-left">
              <thead className="bg-slate-50 text-slate-600">
                <tr>
                  <th className="px-4 py-3">Requested</th>
                  <th className="px-4 py-3">Restaurant</th>
                  <th className="px-4 py-3">Customer</th>
                  <th className="px-4 py-3">Distance</th>
                  <th className="px-4 py-3">Payout</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3">Timeline</th>
                </tr>
              </thead>
              <tbody>
                {riderActivities.map((activity) => (
                  <tr key={activity.id} className="border-t border-slate-100 align-top">
                    <td className="px-4 py-3">
                      {formatDate(activity.requestedAt)}
                    </td>
                    <td className="px-4 py-3">
                      <p className="font-medium text-slate-900">
                        {activity.restaurantName}
                      </p>
                      <p className="text-xs text-slate-500">
                        {activity.restaurantAddress}
                      </p>
                    </td>
                    <td className="px-4 py-3">
                      {activity.customerAddress}
                    </td>
                    <td className="px-4 py-3">
                      {activity.distanceKm ?? "-"} km
                    </td>
                    <td className="px-4 py-3">
                      {formatCurrency(activity.estimatedPayout)}
                    </td>
                    <td className="px-4 py-3">
                      <p>{formatStatus(activity.status)}</p>
                      {activity.rejectionReason && (
                        <p className="text-xs text-red-600">
                          {activity.rejectionReason}
                        </p>
                      )}
                    </td>
                    <td className="px-4 py-3 text-xs text-slate-500">
                      <p>Responded: {formatDate(activity.respondedAt)}</p>
                      <p>Arrived: {formatDate(activity.arrivedAtRestaurantAt)}</p>
                      <p>Picked up: {formatDate(activity.pickedUpAt)}</p>
                      <p>Delivered: {formatDate(activity.deliveredAt)}</p>
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
