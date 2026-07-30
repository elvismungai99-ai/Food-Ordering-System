import {
  useEffect,
  useState,
} from "react";

import {
  useNavigate,
} from "react-router-dom";

import {
  acceptDeliveryRequest,
  confirmDelivery,
  confirmPickup,
  getRiderDashboard,
  markArrivedAtRestaurant,
  rejectDeliveryRequest,
  updateRiderAvailability,
  updateRiderLocation,
  type DeliveryRequest,
  type RiderDashboard,
  type RiderOperationalStatus,
} from "../../services/RiderService";

import {
  buildOpenRouteServiceMapUrl,
  buildOpenRouteServiceRouteUrl,
} from "../../utils/location";

import {
  getApiErrorMessage,
} from "../../utils/apiError";

function RiderDashboardPage() {
  const navigate =
    useNavigate();

  const [
    dashboard,
    setDashboard,
  ] = useState<RiderDashboard | null>(null);

  const [
    loading,
    setLoading,
  ] = useState(true);

  const [
    error,
    setError,
  ] = useState("");

  const loadDashboard = async () => {
    try {
      setLoading(true);
      setError("");

      setDashboard(
        await getRiderDashboard()
      );
    } catch (requestError) {
      console.error(
        "Failed to load rider dashboard:",
        requestError
      );
      setError(
        getApiErrorMessage(requestError)
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboard();
  }, []);

  useEffect(() => {
    if (!dashboard?.rider.online) {
      return;
    }

    const refreshLocation = () => {
      if (!navigator.geolocation) {
        setError(
          "Your browser does not support location sharing."
        );
        return;
      }

      navigator.geolocation.getCurrentPosition(
        async position => {
          try {
            const rider =
              await updateRiderLocation(
                position.coords.latitude,
                position.coords.longitude
              );

            setDashboard(current =>
              current
                ? {
                    ...current,
                    rider,
                  }
                : current
            );
          } catch (requestError) {
            setError(
              getApiErrorMessage(requestError)
            );
          }
        },
        () => {
          setError(
            "Location permission is required to stay online for assignments."
          );
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 60000,
        }
      );
    };

    refreshLocation();

    const intervalId =
      window.setInterval(
        refreshLocation,
        120000
      );

    return () =>
      window.clearInterval(intervalId);
  }, [dashboard?.rider.online]);

  const formatCurrency = (
    amount: number
  ) => {
    return new Intl.NumberFormat(
      "en-KE",
      {
        style: "currency",
        currency: "KES",
      }
    ).format(amount || 0);
  };

  const updateRequest = (
    updatedRequest: DeliveryRequest
  ) => {
    setDashboard(current => {
      if (!current) {
        return current;
      }

      return {
        ...current,
        deliveryRequests:
          current.deliveryRequests.map(request =>
            request.id === updatedRequest.id
              ? updatedRequest
              : request
          ),
      };
    });
  };

  const handleAvailability = async (
    operationalStatus: RiderOperationalStatus,
    online: boolean
  ) => {
    try {
      setError("");

      const rider =
        await updateRiderAvailability(
          operationalStatus,
          online
        );

      setDashboard(current =>
        current
          ? {
              ...current,
              rider,
            }
          : current
      );
    } catch (requestError) {
      setError(
        getApiErrorMessage(requestError)
      );
    }
  };

  const handleOnlineToggle = async () => {
    if (!dashboard) {
      return;
    }

    const nextOnline =
      !dashboard.rider.online;

    if (!nextOnline) {
      await handleAvailability(
        dashboard.rider.operationalStatus,
        false
      );
      return;
    }

    if (!navigator.geolocation) {
      setError(
        "Your browser does not support location sharing."
      );
      return;
    }

    navigator.geolocation.getCurrentPosition(
      async position => {
        try {
          setError("");

          const riderWithLocation =
            await updateRiderLocation(
              position.coords.latitude,
              position.coords.longitude
            );

          const rider =
            await updateRiderAvailability(
              "OPEN",
              true
            );

          setDashboard(current =>
            current
              ? {
                  ...current,
                  rider: {
                    ...rider,
                    currentLatitude:
                      riderWithLocation.currentLatitude,
                    currentLongitude:
                      riderWithLocation.currentLongitude,
                    lastLocationUpdatedAt:
                      riderWithLocation.lastLocationUpdatedAt,
                  },
                }
              : current
          );
        } catch (requestError) {
          setError(
            getApiErrorMessage(requestError)
          );
        }
      },
      () => {
        setError(
          "Location permission is required before going online."
        );
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
      }
    );
  };

  const handleReject = async (
    request: DeliveryRequest
  ) => {
    const reason =
      window.prompt(
        "Enter reason for rejecting this delivery"
      );

    if (!reason?.trim()) {
      return;
    }

    updateRequest(
      await rejectDeliveryRequest(
        request.id,
        reason.trim()
      )
    );

    await loadDashboard();
  };

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("userId");
    localStorage.removeItem("firstName");
    navigate("/login");
  };

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-100">
        <p className="text-slate-500">
          Loading rider dashboard...
        </p>
      </div>
    );
  }

  return (
    <main className="min-h-screen bg-slate-100 p-6 md:p-8">
      <div className="mx-auto max-w-7xl">
        <header className="mb-8 flex flex-wrap items-start justify-between gap-4">
          <div>
            <h1 className="text-3xl font-semibold text-slate-950">
              Rider Dashboard
            </h1>
            <p className="mt-2 text-sm text-slate-500">
              {dashboard?.rider.fullName}
            </p>
          </div>

          <button
            type="button"
            onClick={logout}
            className="rounded-3xl border border-slate-300 bg-white px-5 py-2 text-sm font-semibold text-slate-700"
          >
            Logout
          </button>
        </header>

        {error && (
          <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}

        {dashboard && (
          <>
            <section className="grid gap-5 md:grid-cols-4">
              <MetricCard
                label="Total Earnings"
                value={formatCurrency(
                  dashboard.totalEarnings
                )}
              />
              <MetricCard
                label="Pending Payout"
                value={formatCurrency(
                  dashboard.pendingPayout
                )}
              />
              <MetricCard
                label="Completed"
                value={String(
                  dashboard.completedDeliveries
                )}
              />
              <MetricCard
                label="Rejections"
                value={String(
                  dashboard.rejectedRequests
                )}
              />
            </section>

            <section className="mt-8 rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">
              <div className="flex flex-wrap items-center justify-between gap-4">
                <div>
                  <h2 className="text-xl font-semibold text-slate-950">
                    Availability
                  </h2>
                  <p className="mt-1 text-sm text-slate-500">
                    Choose whether you are operating and online for assignments.
                  </p>
                </div>

                <div className="flex flex-wrap gap-3">
                  <button
                    type="button"
                    onClick={() =>
                      handleAvailability(
                        "OPEN",
                        dashboard.rider.online
                      )
                    }
                    className={`rounded-3xl px-5 py-2 text-sm font-semibold ${
                      dashboard.rider.operationalStatus
                      === "OPEN"
                        ? "bg-green-600 text-white"
                        : "bg-slate-100 text-slate-700"
                    }`}
                  >
                    Open
                  </button>
                  <button
                    type="button"
                    onClick={() =>
                      handleAvailability(
                        "CLOSED",
                        false
                      )
                    }
                    className={`rounded-3xl px-5 py-2 text-sm font-semibold ${
                      dashboard.rider.operationalStatus
                      === "CLOSED"
                        ? "bg-red-600 text-white"
                        : "bg-slate-100 text-slate-700"
                    }`}
                  >
                    Closed
                  </button>
                  <button
                    type="button"
                    onClick={handleOnlineToggle}
                    className={`rounded-3xl px-5 py-2 text-sm font-semibold ${
                      dashboard.rider.online
                        ? "bg-indigo-600 text-white"
                        : "bg-slate-100 text-slate-700"
                    }`}
                  >
                    {dashboard.rider.online
                      ? "Online"
                      : "Offline"}
                  </button>
                </div>
              </div>

              {dashboard.rider.lastLocationUpdatedAt && (
                <p className="mt-4 text-sm text-slate-500">
                  Last location update: {new Date(
                    dashboard.rider.lastLocationUpdatedAt
                  ).toLocaleString("en-KE")}
                </p>
              )}
            </section>

            <section className="mt-8 space-y-5">
              <h2 className="text-xl font-semibold text-slate-950">
                Delivery Requests
              </h2>

              {dashboard.deliveryRequests.length === 0 ? (
                <div className="rounded-[24px] border border-slate-200 bg-white p-10 text-center text-slate-500">
                  No delivery requests yet.
                </div>
              ) : (
                dashboard.deliveryRequests.map(request => (
                  <DeliveryRequestCard
                    key={request.id}
                    request={request}
                    formatCurrency={formatCurrency}
                    onAccept={async () =>
                      updateRequest(
                        await acceptDeliveryRequest(
                          request.id
                        )
                      )
                    }
                    onReject={() =>
                      handleReject(request)
                    }
                    onArrived={async () =>
                      updateRequest(
                        await markArrivedAtRestaurant(
                          request.id
                        )
                      )
                    }
                    onPickup={async () =>
                      updateRequest(
                        await confirmPickup(
                          request.id
                        )
                      )
                    }
                    onDelivered={async () =>
                      updateRequest(
                        await confirmDelivery(
                          request.id
                        )
                      )
                    }
                  />
                ))
              )}
            </section>
          </>
        )}
      </div>
    </main>
  );
}

function DeliveryRequestCard({
  request,
  formatCurrency,
  onAccept,
  onReject,
  onArrived,
  onPickup,
  onDelivered,
}: {
  request: DeliveryRequest;
  formatCurrency: (value: number) => string;
  onAccept: () => void;
  onReject: () => void;
  onArrived: () => void;
  onPickup: () => void;
  onDelivered: () => void;
}) {
  const customerMapUrl =
    request.customerLatitude != null
    && request.customerLongitude != null
      ? buildOpenRouteServiceMapUrl(
          request.customerLatitude,
          request.customerLongitude
        )
      : null;

  const routeUrl =
    request.restaurantLatitude != null
    && request.restaurantLongitude != null
    && request.customerLatitude != null
    && request.customerLongitude != null
      ? buildOpenRouteServiceRouteUrl(
          request.restaurantLatitude,
          request.restaurantLongitude,
          request.customerLatitude,
          request.customerLongitude
        )
      : null;

  return (
    <article className="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h3 className="text-lg font-semibold text-slate-950">
            {request.restaurantName}
          </h3>
          <p className="mt-1 text-sm text-slate-500">
            {request.status}
          </p>
        </div>

        <span className="rounded-full bg-indigo-100 px-4 py-2 text-sm font-semibold text-indigo-700">
          {formatCurrency(request.estimatedPayout)}
        </span>
      </div>

      <div className="mt-5 grid gap-4 md:grid-cols-2">
        <LocationBlock
          label="Restaurant"
          value={request.restaurantAddress}
        />
        <LocationBlock
          label="Customer"
          value={request.customerAddress}
        />
      </div>

      <div className="mt-5 flex flex-wrap gap-3 text-sm">
        {request.distanceKm != null && (
          <span className="rounded-full bg-slate-100 px-4 py-2 font-medium text-slate-700">
            {request.distanceKm} km
          </span>
        )}

        {customerMapUrl && (
          <a
            href={customerMapUrl}
            target="_blank"
            rel="noreferrer"
            className="rounded-full bg-slate-100 px-4 py-2 font-semibold text-indigo-600"
          >
            Customer Map
          </a>
        )}

        {routeUrl && (
          <a
            href={routeUrl}
            target="_blank"
            rel="noreferrer"
            className="rounded-full bg-slate-100 px-4 py-2 font-semibold text-indigo-600"
          >
            Navigate Route
          </a>
        )}
      </div>

      <div className="mt-6 flex flex-wrap gap-3">
        {request.status === "REQUESTED" && (
          <>
            <ActionButton
              label="Accept"
              onClick={onAccept}
            />
            <ActionButton
              label="Reject"
              tone="danger"
              onClick={onReject}
            />
          </>
        )}

        {request.status === "ACCEPTED" && (
          <ActionButton
            label="Arrived At Restaurant"
            onClick={onArrived}
          />
        )}

        {request.status
          === "ARRIVED_AT_RESTAURANT" && (
          <ActionButton
            label="Confirm Pickup"
            onClick={onPickup}
          />
        )}

        {request.status === "PICKED_UP" && (
          <ActionButton
            label="Confirm Delivery"
            onClick={onDelivered}
          />
        )}
      </div>
    </article>
  );
}

function LocationBlock({
  label,
  value,
}: {
  label: string;
  value: string;
}) {
  return (
    <div>
      <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">
        {label}
      </p>
      <p className="mt-2 text-sm text-slate-700">
        {value}
      </p>
    </div>
  );
}

function ActionButton({
  label,
  onClick,
  tone = "primary",
}: {
  label: string;
  onClick: () => void;
  tone?: "primary" | "danger";
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`rounded-3xl px-5 py-2 text-sm font-semibold text-white ${
        tone === "danger"
          ? "bg-red-600"
          : "bg-indigo-600"
      }`}
    >
      {label}
    </button>
  );
}

function MetricCard({
  label,
  value,
}: {
  label: string;
  value: string;
}) {
  return (
    <article className="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">
        {label}
      </p>
      <p className="mt-3 text-2xl font-semibold text-slate-950">
        {value}
      </p>
    </article>
  );
}

export default RiderDashboardPage;
