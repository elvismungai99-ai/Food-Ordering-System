import {
  useEffect,
  useMemo,
  useState,
} from "react";

import {
  useNavigate,
} from "react-router-dom";

import {
  getRestaurantAnalytics,
  type RestaurantAnalytics,
} from "../../services/AnalyticsService";

import {
  getApiErrorMessage,
} from "../../utils/apiError";

function AnalyticsPage() {
  const navigate =
    useNavigate();

  const [
    analytics,
    setAnalytics,
  ] = useState<RestaurantAnalytics | null>(
    null
  );

  const [
    loading,
    setLoading,
  ] = useState(true);

  const [
    error,
    setError,
  ] = useState("");

  useEffect(() => {
    const loadAnalytics = async () => {
      try {
        setLoading(true);
        setError("");

        const data =
          await getRestaurantAnalytics();

        setAnalytics(data);
      } catch (requestError) {
        console.error(
          "Failed to load analytics:",
          requestError
        );

        setError(
          getApiErrorMessage(
            requestError
          )
        );
      } finally {
        setLoading(false);
      }
    };

    loadAnalytics();
  }, []);

  const maxDailySales =
    useMemo(() => {
      if (!analytics?.dailySales.length) {
        return 0;
      }

      return Math.max(
        ...analytics.dailySales.map(day =>
          Number(day.sales)
        )
      );
    }, [analytics]);

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

  const formatDate = (
    value: string
  ) => {
    return new Date(value).toLocaleDateString(
      "en-KE",
      {
        month: "short",
        day: "numeric",
      }
    );
  };

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-100">
        <p className="text-slate-500">
          Loading analytics...
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
              Dashboard Analytics
            </h1>

            <p className="mt-2 text-sm text-slate-500">
              {analytics?.restaurantName
                || "Restaurant performance"}
            </p>
          </div>

          <button
            type="button"
            onClick={() =>
              navigate(
                "/restaurant/dashboard"
              )
            }
            className="rounded-3xl border border-slate-300 bg-white px-5 py-2 text-sm font-semibold text-slate-700"
          >
            Dashboard
          </button>
        </header>

        {error && (
          <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}

        {!analytics ? (
          <section className="rounded-[24px] border border-slate-200 bg-white p-10 text-center">
            <p className="text-slate-500">
              No analytics data is available.
            </p>
          </section>
        ) : (
          <>
            <section className="grid gap-5 sm:grid-cols-2 xl:grid-cols-5">
              <MetricCard
                label="Daily Sales"
                value={formatCurrency(
                  analytics.todaySales
                )}
              />

              <MetricCard
                label="Average Order Value"
                value={formatCurrency(
                  analytics.averageOrderValue
                )}
              />

              <MetricCard
                label="Cancelled Orders"
                value={String(
                  analytics.cancelledOrders
                )}
                helper={`${analytics.cancellationRate.toFixed(1)}% cancellation rate`}
              />

              <MetricCard
                label="Completed Orders"
                value={String(
                  analytics.completedOrders
                )}
              />

              <MetricCard
                label="Total Sales"
                value={formatCurrency(
                  analytics.totalSales
                )}
              />
            </section>

            <section className="mt-8 grid gap-6 lg:grid-cols-[1.4fr_1fr]">
              <div className="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">
                <div className="mb-6 flex items-center justify-between gap-4">
                  <div>
                    <h2 className="text-xl font-semibold text-slate-950">
                      Daily Sales
                    </h2>

                    <p className="mt-1 text-sm text-slate-500">
                      Last 7 days of paid, non-cancelled orders.
                    </p>
                  </div>
                </div>

                <div className="space-y-4">
                  {analytics.dailySales.map(day => {
                    const width =
                      maxDailySales > 0
                        ? Math.max(
                            6,
                            Number(day.sales)
                            / maxDailySales
                            * 100
                          )
                        : 0;

                    return (
                      <div
                        key={day.date}
                        className="grid gap-3 sm:grid-cols-[90px_1fr_120px]"
                      >
                        <span className="text-sm font-medium text-slate-600">
                          {formatDate(day.date)}
                        </span>

                        <div className="h-8 overflow-hidden rounded-full bg-slate-100">
                          <div
                            className="h-full rounded-full bg-indigo-600"
                            style={{
                              width: `${width}%`,
                            }}
                          />
                        </div>

                        <span className="text-sm font-semibold text-slate-800 sm:text-right">
                          {formatCurrency(day.sales)}
                        </span>
                      </div>
                    );
                  })}
                </div>
              </div>

              <div className="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">
                <h2 className="text-xl font-semibold text-slate-950">
                  Restaurant Performance
                </h2>

                <div className="mt-6 space-y-4">
                  <PerformanceRow
                    label="Total Orders"
                    value={analytics.totalOrders}
                  />

                  <PerformanceRow
                    label="Active Orders"
                    value={analytics.activeOrders}
                  />

                  <PerformanceRow
                    label="Completed Orders"
                    value={analytics.completedOrders}
                  />

                  <PerformanceRow
                    label="Cancelled Orders"
                    value={analytics.cancelledOrders}
                  />

                  <PerformanceRow
                    label="Cancellation Rate"
                    value={`${analytics.cancellationRate.toFixed(1)}%`}
                  />
                </div>
              </div>
            </section>

            <section className="mt-8 rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">
              <h2 className="text-xl font-semibold text-slate-950">
                Popular Menu Items
              </h2>

              {analytics.popularMenuItems.length === 0 ? (
                <p className="mt-5 text-sm text-slate-500">
                  No ordered menu items yet.
                </p>
              ) : (
                <div className="mt-5 overflow-x-auto">
                  <table className="w-full min-w-[620px] text-left text-sm">
                    <thead>
                      <tr className="border-b border-slate-200 text-xs uppercase tracking-wide text-slate-400">
                        <th className="py-3 font-semibold">
                          Menu Item
                        </th>
                        <th className="py-3 font-semibold">
                          Quantity Sold
                        </th>
                        <th className="py-3 font-semibold">
                          Revenue
                        </th>
                      </tr>
                    </thead>

                    <tbody>
                      {analytics.popularMenuItems.map(item => (
                        <tr
                          key={item.menuItemId}
                          className="border-b border-slate-100 last:border-b-0"
                        >
                          <td className="py-4 font-medium text-slate-900">
                            {item.itemName}
                          </td>
                          <td className="py-4 text-slate-600">
                            {item.quantitySold}
                          </td>
                          <td className="py-4 font-semibold text-slate-900">
                            {formatCurrency(item.revenue)}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </section>
          </>
        )}
      </div>
    </main>
  );
}

function MetricCard({
  label,
  value,
  helper,
}: {
  label: string;
  value: string;
  helper?: string;
}) {
  return (
    <article className="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">
        {label}
      </p>

      <p className="mt-3 text-2xl font-semibold text-slate-950">
        {value}
      </p>

      {helper && (
        <p className="mt-2 text-xs text-slate-500">
          {helper}
        </p>
      )}
    </article>
  );
}

function PerformanceRow({
  label,
  value,
}: {
  label: string;
  value: string | number;
}) {
  return (
    <div className="flex items-center justify-between gap-4 border-b border-slate-100 pb-4 last:border-b-0 last:pb-0">
      <span className="text-sm text-slate-500">
        {label}
      </span>

      <span className="font-semibold text-slate-900">
        {value}
      </span>
    </div>
  );
}

export default AnalyticsPage;
