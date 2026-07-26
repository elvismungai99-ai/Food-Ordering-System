import {
  useEffect,
  useState,
} from "react";

import {
  useNavigate,
} from "react-router-dom";

import {
  getMyRestaurant,
  type Restaurant,
} from "../../services/RestaurantService";

import {
  getApiErrorMessage,
} from "../../utils/apiError";

function RestaurantProfilePage() {
  const navigate =
    useNavigate();

  const [
    restaurant,
    setRestaurant,
  ] = useState<Restaurant | null>(
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
    const loadRestaurant = async () => {
      try {
        setLoading(true);
        setError("");

        const data =
          await getMyRestaurant();

        setRestaurant(data);

        if (data.id) {
          localStorage.setItem(
            "restaurantId",
            data.id
          );
        }
      } catch (requestError) {
        console.error(
          "Failed to load restaurant profile:",
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

    loadRestaurant();
  }, []);

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-100">
        <p className="text-slate-500">
          Loading restaurant details...
        </p>
      </div>
    );
  }

  return (
    <main className="min-h-screen bg-slate-100 p-6 md:p-8">
      <div className="max-w-5xl mx-auto">
        <div className="mb-8 flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 className="text-3xl font-semibold text-slate-950">
              Restaurant Details
            </h1>

            <p className="mt-2 text-slate-500">
              View the restaurant profile linked to your owner account.
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
        </div>

        {error && (
          <div className="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}

        {!error && !restaurant && (
          <div className="rounded-[24px] border border-slate-200 bg-white p-10 text-center">
            <p className="text-slate-500">
              No restaurant profile was found for this owner account.
            </p>
          </div>
        )}

        {restaurant && (
          <section className="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">
            <div className="flex flex-wrap items-start justify-between gap-4">
              <div>
                <h2 className="text-2xl font-semibold text-slate-950">
                  {restaurant.name}
                </h2>

                <p className="mt-2 max-w-3xl text-sm text-slate-500">
                  {
                    restaurant.description
                    || "No description provided."
                  }
                </p>
              </div>

              <span
                className={
                  `rounded-full px-4 py-2 text-sm font-semibold ${
                    restaurant.status === "OPEN"
                      ? "bg-green-100 text-green-700"
                      : "bg-slate-100 text-slate-600"
                  }`
                }
              >
                {restaurant.status}
              </span>
            </div>

            <div className="mt-8 grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
              <div>
                <p className="text-xs uppercase tracking-wide text-slate-400">
                  Category
                </p>

                <p className="mt-1 font-medium text-slate-800">
                  {
                    restaurant.category
                    || "Not specified"
                  }
                </p>
              </div>

              <div>
                <p className="text-xs uppercase tracking-wide text-slate-400">
                  Address
                </p>

                <p className="mt-1 font-medium text-slate-800">
                  {restaurant.address}
                </p>
              </div>

              <div>
                <p className="text-xs uppercase tracking-wide text-slate-400">
                  Opening Time
                </p>

                <p className="mt-1 font-medium text-slate-800">
                  {restaurant.openingTime}
                </p>
              </div>

              <div>
                <p className="text-xs uppercase tracking-wide text-slate-400">
                  Closing Time
                </p>

                <p className="mt-1 font-medium text-slate-800">
                  {restaurant.closingTime}
                </p>
              </div>
            </div>
          </section>
        )}
      </div>
    </main>
  );
}

export default RestaurantProfilePage;
