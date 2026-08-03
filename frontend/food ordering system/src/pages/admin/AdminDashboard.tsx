import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  getAllCustomers,
  deleteCustomer,
  getAllOwners,
  getAllRestaurants,
  getAllRiders,
  getCustomerActivities,
  getOwnerActivities,
  getRiderActivitiesById,
  deleteRestaurant,
  updateRestaurantStatus,
  updateRiderStatus,
  Customer,
  DeliveryActivity,
  OrderActivity,
  Restaurant,
  Rider,
  RiderStatus,
} from "../../services/AdminService";
import { getApiErrorMessage } from "../../utils/apiError";

type AdminSection = "customers" | "owners" | "riders" | "restaurants";

const sections: {
  id: AdminSection;
  label: string;
}[] = [
  { id: "customers", label: "Customers" },
  { id: "owners", label: "Restaurant Owners" },
  { id: "riders", label: "Riders" },
  { id: "restaurants", label: "Restaurants" },
];

function AdminDashboard() {
  const navigate = useNavigate();

  const [activeSection, setActiveSection] =
    useState<AdminSection>("customers");
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [owners, setOwners] = useState<Customer[]>([]);
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [riders, setRiders] = useState<Rider[]>([]);
  const [selectedCustomer, setSelectedCustomer] =
    useState<Customer | null>(null);
  const [selectedOwner, setSelectedOwner] =
    useState<Customer | null>(null);
  const [selectedRider, setSelectedRider] =
    useState<Rider | null>(null);
  const [orderActivities, setOrderActivities] =
    useState<OrderActivity[]>([]);
  const [deliveryActivities, setDeliveryActivities] =
    useState<DeliveryActivity[]>([]);
  const [loading, setLoading] = useState(true);
  const [activityLoading, setActivityLoading] = useState(false);
  const [error, setError] = useState("");
  const [activityError, setActivityError] = useState("");
  const [deletingId, setDeletingId] = useState("");
  const [updatingRestaurantId, setUpdatingRestaurantId] = useState("");
  const [updatingRiderId, setUpdatingRiderId] = useState("");

  const selectedOwnerRestaurant = useMemo(
    () =>
      selectedOwner
        ? restaurants.find(
            (restaurant) => restaurant.ownerId === selectedOwner.id
          )
        : undefined,
    [restaurants, selectedOwner]
  );

  const loadData = async () => {
    setLoading(true);
    setError("");

    try {
      const [
        customersData,
        ownersData,
        restaurantsData,
        ridersData,
      ] = await Promise.all([
        getAllCustomers(),
        getAllOwners(),
        getAllRestaurants(),
        getAllRiders(),
      ]);

      setCustomers(customersData);
      setOwners(ownersData);
      setRestaurants(restaurantsData);
      setRiders(ridersData);
    } catch (err) {
      setError(getApiErrorMessage(err));
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const resetActivityPanel = () => {
    setSelectedCustomer(null);
    setSelectedOwner(null);
    setSelectedRider(null);
    setOrderActivities([]);
    setDeliveryActivities([]);
    setActivityError("");
  };

  const openSection = (section: AdminSection) => {
    setActiveSection(section);
    resetActivityPanel();
  };

  const loadCustomerActivities = async (customer: Customer) => {
    setSelectedCustomer(customer);
    setSelectedOwner(null);
    setSelectedRider(null);
    setDeliveryActivities([]);
    setActivityLoading(true);
    setActivityError("");

    try {
      setOrderActivities(await getCustomerActivities(customer.id));
    } catch (err) {
      setOrderActivities([]);
      setActivityError(getApiErrorMessage(err));
    } finally {
      setActivityLoading(false);
    }
  };

  const loadOwnerActivities = async (owner: Customer) => {
    setSelectedOwner(owner);
    setSelectedCustomer(null);
    setSelectedRider(null);
    setDeliveryActivities([]);
    setActivityLoading(true);
    setActivityError("");

    try {
      setOrderActivities(await getOwnerActivities(owner.id));
    } catch (err) {
      setOrderActivities([]);
      setActivityError(getApiErrorMessage(err));
    } finally {
      setActivityLoading(false);
    }
  };

  const loadRiderActivities = async (rider: Rider) => {
    setSelectedRider(rider);
    setSelectedCustomer(null);
    setSelectedOwner(null);
    setOrderActivities([]);
    setActivityLoading(true);
    setActivityError("");

    try {
      setDeliveryActivities(await getRiderActivitiesById(rider.id));
    } catch (err) {
      setDeliveryActivities([]);
      setActivityError(getApiErrorMessage(err));
    } finally {
      setActivityLoading(false);
    }
  };

  const handleDeleteCustomer = async (id: string, name: string) => {
    if (!window.confirm(`Delete customer "${name}"? This cannot be undone.`)) {
      return;
    }

    try {
      setDeletingId(id);
      setError("");
      await deleteCustomer(id);
      setCustomers((current) =>
        current.filter((customer) => customer.id !== id)
      );

      if (selectedCustomer?.id === id) {
        resetActivityPanel();
      }
    } catch (err) {
      setError(getApiErrorMessage(err));
      console.error(err);
    } finally {
      setDeletingId("");
    }
  };

  const handleDeleteRestaurant = async (id: string, name: string) => {
    if (!window.confirm(`Delete restaurant "${name}"? This cannot be undone.`)) {
      return;
    }

    try {
      setDeletingId(id);
      setError("");
      await deleteRestaurant(id);
      setRestaurants((current) =>
        current.filter((restaurant) => restaurant.id !== id)
      );
    } catch (err) {
      setError(getApiErrorMessage(err));
      console.error(err);
    } finally {
      setDeletingId("");
    }
  };

  const handleRestaurantStatus = async (
    id: string,
    status: "PENDING_APPROVAL" | "APPROVED" | "SUSPENDED" | "REJECTED"
  ) => {
    try {
      setUpdatingRestaurantId(id);
      setError("");

      const updated = await updateRestaurantStatus(id, status);

      setRestaurants((current) =>
        current.map((restaurant) =>
          restaurant.id === id ? updated : restaurant
        )
      );
    } catch (err) {
      setError(getApiErrorMessage(err));
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

      const updated = await updateRiderStatus(id, status);

      setRiders((current) =>
        current.map((rider) => (rider.id === id ? updated : rider))
      );

      if (selectedRider?.id === id) {
        setSelectedRider(updated);
      }
    } catch (err) {
      setError(getApiErrorMessage(err));
      console.error(err);
    } finally {
      setUpdatingRiderId("");
    }
  };

  const formatCurrency = (amount?: number | null) =>
    new Intl.NumberFormat("en-KE", {
      style: "currency",
      currency: "KES",
    }).format(amount || 0);

  const formatDate = (value?: string | null) =>
    value ? new Date(value).toLocaleString("en-KE") : "-";

  const formatStatus = (status?: string | null) =>
    status ? status.replaceAll("_", " ") : "-";

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("userId");
    localStorage.removeItem("role");
    localStorage.removeItem("firstName");
    localStorage.removeItem("restaurantId");
    navigate("/login");
  };

  return (
    <div className="min-h-screen bg-slate-100">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-7xl flex-wrap items-center justify-between gap-4 px-4 py-5 sm:px-6 lg:px-8">
          <div>
            <p className="text-sm font-bold uppercase text-emerald-700">
              Platform control
            </p>
            <h1 className="text-3xl font-black text-slate-950">
              Super Admin Dashboard
            </h1>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <button
              type="button"
              onClick={() => navigate("/")}
              className="rounded-lg border border-slate-300 bg-white px-5 py-2 text-sm font-semibold text-slate-700 shadow-sm hover:bg-slate-50"
            >
              Home
            </button>

            <button
              type="button"
              onClick={logout}
              className="rounded-lg border border-red-200 bg-white px-5 py-2 text-sm font-semibold text-red-700 shadow-sm hover:bg-red-50"
            >
              Logout
            </button>
          </div>
        </div>

        <nav className="mx-auto flex max-w-7xl gap-2 overflow-x-auto px-4 pb-4 sm:px-6 lg:px-8">
          {sections.map((section) => (
            <button
              key={section.id}
              type="button"
              onClick={() => openSection(section.id)}
              className={`rounded-full px-5 py-2 text-sm font-bold transition ${
                activeSection === section.id
                  ? "bg-emerald-600 text-white shadow-sm"
                  : "bg-slate-100 text-slate-700 hover:bg-slate-200"
              }`}
            >
              {section.label}
            </button>
          ))}
        </nav>
      </header>

      <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        {loading && (
          <div className="rounded-lg border border-slate-200 bg-white p-6 text-slate-500 shadow-sm">
            Loading admin dashboard...
          </div>
        )}

        {!loading && error && (
          <div className="rounded-lg border border-red-200 bg-white p-6 text-red-700 shadow-sm">
            {error}
          </div>
        )}

        {!loading && !error && (
          <div className="grid gap-6 lg:grid-cols-[minmax(0,1.25fr)_minmax(360px,0.75fr)]">
            <section>
              {activeSection === "customers" && (
                <CustomerTable
                  customers={customers}
                  deletingId={deletingId}
                  onSelect={loadCustomerActivities}
                  onDelete={handleDeleteCustomer}
                  selectedId={selectedCustomer?.id}
                />
              )}

              {activeSection === "owners" && (
                <OwnerTable
                  owners={owners}
                  restaurants={restaurants}
                  onSelect={loadOwnerActivities}
                  selectedId={selectedOwner?.id}
                />
              )}

              {activeSection === "riders" && (
                <RiderTable
                  riders={riders}
                  selectedId={selectedRider?.id}
                  updatingRiderId={updatingRiderId}
                  onSelect={loadRiderActivities}
                  onStatusChange={handleRiderStatus}
                  formatDate={formatDate}
                  formatStatus={formatStatus}
                />
              )}

              {activeSection === "restaurants" && (
                <RestaurantTable
                  restaurants={restaurants}
                  deletingId={deletingId}
                  updatingRestaurantId={updatingRestaurantId}
                  onDelete={handleDeleteRestaurant}
                  onStatusChange={handleRestaurantStatus}
                  formatStatus={formatStatus}
                />
              )}
            </section>

            <aside className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
              <ActivityPanel
                activeSection={activeSection}
                selectedCustomer={selectedCustomer}
                selectedOwner={selectedOwner}
                selectedOwnerRestaurant={selectedOwnerRestaurant}
                selectedRider={selectedRider}
                orderActivities={orderActivities}
                deliveryActivities={deliveryActivities}
                loading={activityLoading}
                error={activityError}
                formatCurrency={formatCurrency}
                formatDate={formatDate}
                formatStatus={formatStatus}
              />
            </aside>
          </div>
        )}
      </main>
    </div>
  );
}

function CustomerTable({
  customers,
  deletingId,
  onSelect,
  onDelete,
  selectedId,
}: {
  customers: Customer[];
  deletingId: string;
  onSelect: (customer: Customer) => void;
  onDelete: (id: string, name: string) => void;
  selectedId?: string;
}) {
  return (
    <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
      <TableHeader title="Customers" count={customers.length} />
      {customers.length === 0 ? (
        <EmptyState text="No customers found." />
      ) : (
        <table className="w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-600">
            <tr>
              <th className="px-4 py-3">Name</th>
              <th className="px-4 py-3">Email</th>
              <th className="px-4 py-3">Joined</th>
              <th className="px-4 py-3 text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            {customers.map((customer) => (
              <tr
                key={customer.id}
                className={`border-t border-slate-100 ${
                  selectedId === customer.id ? "bg-emerald-50" : ""
                }`}
              >
                <td className="px-4 py-3 font-medium text-slate-900">
                  {customer.fullName}
                </td>
                <td className="px-4 py-3">{customer.email}</td>
                <td className="px-4 py-3">
                  {new Date(customer.createdAt).toLocaleDateString()}
                </td>
                <td className="px-4 py-3">
                  <div className="flex justify-end gap-3">
                    <button
                      type="button"
                      onClick={() => onSelect(customer)}
                      className="font-semibold text-emerald-700 hover:text-emerald-900"
                    >
                      View activity
                    </button>
                    <button
                      type="button"
                      disabled={deletingId === customer.id}
                      onClick={() =>
                        onDelete(customer.id, customer.fullName)
                      }
                      className="font-semibold text-red-600 hover:text-red-800 disabled:opacity-50"
                    >
                      {deletingId === customer.id ? "Deleting..." : "Delete"}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}

function OwnerTable({
  owners,
  restaurants,
  onSelect,
  selectedId,
}: {
  owners: Customer[];
  restaurants: Restaurant[];
  onSelect: (owner: Customer) => void;
  selectedId?: string;
}) {
  return (
    <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
      <TableHeader title="Restaurant Owners" count={owners.length} />
      {owners.length === 0 ? (
        <EmptyState text="No restaurant owners found." />
      ) : (
        <table className="w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-600">
            <tr>
              <th className="px-4 py-3">Owner</th>
              <th className="px-4 py-3">Restaurant</th>
              <th className="px-4 py-3">Email</th>
              <th className="px-4 py-3 text-right">Activity</th>
            </tr>
          </thead>
          <tbody>
            {owners.map((owner) => {
              const restaurant = restaurants.find(
                (item) => item.ownerId === owner.id
              );

              return (
                <tr
                  key={owner.id}
                  className={`border-t border-slate-100 ${
                    selectedId === owner.id ? "bg-emerald-50" : ""
                  }`}
                >
                  <td className="px-4 py-3 font-medium text-slate-900">
                    {owner.fullName}
                  </td>
                  <td className="px-4 py-3">
                    {restaurant?.name || "No restaurant profile"}
                  </td>
                  <td className="px-4 py-3">{owner.email}</td>
                  <td className="px-4 py-3 text-right">
                    <button
                      type="button"
                      onClick={() => onSelect(owner)}
                      className="font-semibold text-emerald-700 hover:text-emerald-900"
                    >
                      View activity
                    </button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      )}
    </section>
  );
}

function RiderTable({
  riders,
  selectedId,
  updatingRiderId,
  onSelect,
  onStatusChange,
  formatDate,
  formatStatus,
}: {
  riders: Rider[];
  selectedId?: string;
  updatingRiderId: string;
  onSelect: (rider: Rider) => void;
  onStatusChange: (id: string, status: RiderStatus) => void;
  formatDate: (value?: string | null) => string;
  formatStatus: (status?: string | null) => string;
}) {
  return (
    <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
      <TableHeader title="Riders" count={riders.length} />
      {riders.length === 0 ? (
        <EmptyState text="No riders found." />
      ) : (
        <table className="w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-600">
            <tr>
              <th className="px-4 py-3">Rider</th>
              <th className="px-4 py-3">Vehicle</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">Availability</th>
              <th className="px-4 py-3">Approval</th>
              <th className="px-4 py-3 text-right">Activity</th>
            </tr>
          </thead>
          <tbody>
            {riders.map((rider) => (
              <tr
                key={rider.id}
                className={`border-t border-slate-100 align-top ${
                  selectedId === rider.id ? "bg-emerald-50" : ""
                }`}
              >
                <td className="px-4 py-3">
                  <p className="font-medium text-slate-900">
                    {rider.fullName}
                  </p>
                  <p className="text-xs text-slate-500">{rider.email}</p>
                  <p className="text-xs text-slate-500">
                    Joined {formatDate(rider.createdAt)}
                  </p>
                </td>
                <td className="px-4 py-3">
                  <p>{rider.vehicleType}</p>
                  <p className="text-xs text-slate-500">
                    {rider.licencePlate}
                  </p>
                </td>
                <td className="px-4 py-3">{formatStatus(rider.status)}</td>
                <td className="px-4 py-3">
                  <p>{formatStatus(rider.operationalStatus)}</p>
                  <p className="text-xs text-slate-500">
                    {rider.online ? "Online" : "Offline"}
                  </p>
                </td>
                <td className="px-4 py-3">
                  <div className="flex flex-wrap gap-2">
                    {(["APPROVED", "REJECTED", "SUSPENDED"] as const).map(
                      (status) => (
                        <button
                          key={status}
                          type="button"
                          disabled={
                            updatingRiderId === rider.id ||
                            rider.status === status
                          }
                          onClick={() => onStatusChange(rider.id, status)}
                          className="rounded-full border border-slate-300 px-3 py-1 text-xs font-semibold text-slate-700 disabled:opacity-40"
                        >
                          {formatStatus(status)}
                        </button>
                      )
                    )}
                  </div>
                </td>
                <td className="px-4 py-3 text-right">
                  <button
                    type="button"
                    onClick={() => onSelect(rider)}
                    className="font-semibold text-emerald-700 hover:text-emerald-900"
                  >
                    View activity
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}

function RestaurantTable({
  restaurants,
  deletingId,
  updatingRestaurantId,
  onDelete,
  onStatusChange,
  formatStatus,
}: {
  restaurants: Restaurant[];
  deletingId: string;
  updatingRestaurantId: string;
  onDelete: (id: string, name: string) => void;
  onStatusChange: (
    id: string,
    status: "PENDING_APPROVAL" | "APPROVED" | "SUSPENDED" | "REJECTED"
  ) => void;
  formatStatus: (status?: string | null) => string;
}) {
  return (
    <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
      <TableHeader title="Restaurants" count={restaurants.length} />
      {restaurants.length === 0 ? (
        <EmptyState text="No restaurants found." />
      ) : (
        <table className="w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-600">
            <tr>
              <th className="px-4 py-3">Restaurant</th>
              <th className="px-4 py-3">Address</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">Approval</th>
              <th className="px-4 py-3 text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            {restaurants.map((restaurant) => (
              <tr key={restaurant.id} className="border-t border-slate-100">
                <td className="px-4 py-3">
                  <p className="font-medium text-slate-900">
                    {restaurant.name}
                  </p>
                  <p className="text-xs text-slate-500">
                    {restaurant.category || "Uncategorized"}
                  </p>
                </td>
                <td className="px-4 py-3">{restaurant.address}</td>
                <td className="px-4 py-3">
                  {formatStatus(restaurant.status)}
                </td>
                <td className="px-4 py-3">
                  <div className="flex flex-wrap gap-2">
                    {(["APPROVED", "REJECTED", "SUSPENDED"] as const).map(
                      (status) => (
                        <button
                          key={status}
                          type="button"
                          disabled={
                            updatingRestaurantId === restaurant.id ||
                            restaurant.status === status
                          }
                          onClick={() =>
                            onStatusChange(restaurant.id, status)
                          }
                          className="rounded-full border border-slate-300 px-3 py-1 text-xs font-semibold text-slate-700 disabled:opacity-40"
                        >
                          {formatStatus(status)}
                        </button>
                      )
                    )}
                  </div>
                </td>
                <td className="px-4 py-3 text-right">
                  <button
                    type="button"
                    disabled={deletingId === restaurant.id}
                    onClick={() =>
                      onDelete(restaurant.id, restaurant.name)
                    }
                    className="font-semibold text-red-600 hover:text-red-800 disabled:opacity-50"
                  >
                    {deletingId === restaurant.id ? "Deleting..." : "Delete"}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}

function ActivityPanel({
  activeSection,
  selectedCustomer,
  selectedOwner,
  selectedOwnerRestaurant,
  selectedRider,
  orderActivities,
  deliveryActivities,
  loading,
  error,
  formatCurrency,
  formatDate,
  formatStatus,
}: {
  activeSection: AdminSection;
  selectedCustomer: Customer | null;
  selectedOwner: Customer | null;
  selectedOwnerRestaurant?: Restaurant;
  selectedRider: Rider | null;
  orderActivities: OrderActivity[];
  deliveryActivities: DeliveryActivity[];
  loading: boolean;
  error: string;
  formatCurrency: (amount?: number | null) => string;
  formatDate: (value?: string | null) => string;
  formatStatus: (status?: string | null) => string;
}) {
  const selectedName =
    selectedCustomer?.fullName ||
    selectedOwner?.fullName ||
    selectedRider?.fullName;

  if (activeSection === "restaurants") {
    return (
      <>
        <h2 className="text-xl font-black text-slate-950">
          Restaurant Controls
        </h2>
        <p className="mt-3 text-sm leading-6 text-slate-600">
          Approve, suspend, reject, or remove restaurant profiles from the table.
          Owner activity is available from the Restaurant Owners tab.
        </p>
      </>
    );
  }

  if (!selectedName) {
    return (
      <>
        <h2 className="text-xl font-black text-slate-950">
          Activity Details
        </h2>
        <p className="mt-3 text-sm leading-6 text-slate-600">
          Select a customer, restaurant owner, or rider to see their activity.
        </p>
      </>
    );
  }

  return (
    <>
      <div className="border-b border-slate-200 pb-4">
        <p className="text-sm font-bold uppercase text-emerald-700">
          Selected profile
        </p>
        <h2 className="mt-1 text-xl font-black text-slate-950">
          {selectedName}
        </h2>
        {selectedOwner && (
          <p className="mt-1 text-sm text-slate-500">
            Restaurant: {selectedOwnerRestaurant?.name || "No restaurant profile"}
          </p>
        )}
      </div>

      {loading && (
        <p className="mt-5 text-sm text-slate-500">Loading activity...</p>
      )}

      {!loading && error && (
        <p className="mt-5 rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-700">
          {error}
        </p>
      )}

      {!loading && !error && selectedRider && (
        <DeliveryActivityList
          activities={deliveryActivities}
          formatCurrency={formatCurrency}
          formatDate={formatDate}
          formatStatus={formatStatus}
        />
      )}

      {!loading && !error && !selectedRider && (
        <OrderActivityList
          activities={orderActivities}
          formatCurrency={formatCurrency}
          formatDate={formatDate}
          formatStatus={formatStatus}
        />
      )}
    </>
  );
}

function OrderActivityList({
  activities,
  formatCurrency,
  formatDate,
  formatStatus,
}: {
  activities: OrderActivity[];
  formatCurrency: (amount?: number | null) => string;
  formatDate: (value?: string | null) => string;
  formatStatus: (status?: string | null) => string;
}) {
  if (activities.length === 0) {
    return <EmptyState text="No order activity found for this user." />;
  }

  return (
    <div className="mt-5 space-y-3">
      {activities.map((order) => (
        <article
          key={order.id}
          className="rounded-xl border border-slate-200 bg-slate-50 p-4"
        >
          <div className="flex flex-wrap justify-between gap-3">
            <div>
              <p className="font-bold text-slate-950">
                {order.restaurantName}
              </p>
              <p className="text-xs text-slate-500">
                {formatDate(order.createdAt)}
              </p>
            </div>
            <p className="rounded-full bg-white px-3 py-1 text-xs font-bold text-slate-700">
              {formatStatus(order.status)}
            </p>
          </div>
          <p className="mt-3 text-sm text-slate-600">
            {order.deliveryAddress}
          </p>
          <div className="mt-3 flex flex-wrap gap-3 text-sm">
            <span className="font-bold text-slate-950">
              {formatCurrency(order.totalAmount)}
            </span>
            <span className="text-slate-500">
              {formatStatus(order.paymentStatus)}
            </span>
            <span className="text-slate-500">
              {order.items?.length || 0} items
            </span>
          </div>
          {order.cancellationReason && (
            <p className="mt-2 text-xs text-red-600">
              Cancelled: {order.cancellationReason}
            </p>
          )}
        </article>
      ))}
    </div>
  );
}

function DeliveryActivityList({
  activities,
  formatCurrency,
  formatDate,
  formatStatus,
}: {
  activities: DeliveryActivity[];
  formatCurrency: (amount?: number | null) => string;
  formatDate: (value?: string | null) => string;
  formatStatus: (status?: string | null) => string;
}) {
  if (activities.length === 0) {
    return <EmptyState text="No rider activity found for this rider." />;
  }

  return (
    <div className="mt-5 space-y-3">
      {activities.map((activity) => (
        <article
          key={activity.id}
          className="rounded-xl border border-slate-200 bg-slate-50 p-4"
        >
          <div className="flex flex-wrap justify-between gap-3">
            <div>
              <p className="font-bold text-slate-950">
                {activity.restaurantName}
              </p>
              <p className="text-xs text-slate-500">
                {formatDate(activity.requestedAt)}
              </p>
            </div>
            <p className="rounded-full bg-white px-3 py-1 text-xs font-bold text-slate-700">
              {formatStatus(activity.status)}
            </p>
          </div>
          <p className="mt-3 text-sm text-slate-600">
            Customer: {activity.customerAddress}
          </p>
          <div className="mt-3 grid grid-cols-2 gap-2 text-xs text-slate-500">
            <p>Distance: {activity.distanceKm ?? "-"} km</p>
            <p>Payout: {formatCurrency(activity.estimatedPayout)}</p>
            <p>Arrived: {formatDate(activity.arrivedAtRestaurantAt)}</p>
            <p>Delivered: {formatDate(activity.deliveredAt)}</p>
          </div>
          {activity.rejectionReason && (
            <p className="mt-2 text-xs text-red-600">
              Rejected: {activity.rejectionReason}
            </p>
          )}
        </article>
      ))}
    </div>
  );
}

function TableHeader({
  title,
  count,
}: {
  title: string;
  count: number;
}) {
  return (
    <div className="flex items-center justify-between border-b border-slate-200 px-4 py-4">
      <h2 className="text-xl font-black text-slate-950">{title}</h2>
      <span className="rounded-full bg-slate-100 px-3 py-1 text-sm font-bold text-slate-600">
        {count}
      </span>
    </div>
  );
}

function EmptyState({ text }: { text: string }) {
  return <p className="p-4 text-sm text-slate-500">{text}</p>;
}

export default AdminDashboard;
