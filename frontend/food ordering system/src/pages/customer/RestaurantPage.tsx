import {
  useCallback,
  useEffect,
  useState,
} from "react";

import { useNavigate } from "react-router-dom";

import api from "../../api/axios";
import CustomerHeader from "../../components/customer/CustomerHeader";
import {
  getApiErrorMessage,
} from "../../utils/apiError";

interface Restaurant {
  id: string;
  name: string;
  description?: string | null;
  address?: string | null;
  openingTime?: string | null;
  closingTime?: string | null;
  status?: string | null;
  category?: string | null;
  openNow?: boolean;
  averageRating?: number;
  reviewCount?: number;
}

function RestaurantPage() {
  const navigate = useNavigate();

  const [restaurants, setRestaurants] =
    useState<Restaurant[]>([]);

  const [categories, setCategories] =
    useState<string[]>([]);

  const [searchTerm, setSearchTerm] =
    useState("");

  const [
    selectedCategory,
    setSelectedCategory,
  ] = useState("");

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState("");

  const fetchCategories = useCallback(
    async () => {
      try {
        const response = await api.get<string[]>(
          "/restaurants/categories"
        );

        setCategories(
          Array.isArray(response.data)
            ? response.data
            : []
        );
      } catch (requestError) {
        console.error(
          "Failed to load restaurant categories",
          requestError
        );

        setCategories([]);
      }
    },
    []
  );

  const fetchRestaurants = useCallback(
    async () => {
      try {
        setLoading(true);
        setError("");

        const response = await api.get<
          Restaurant[]
        >("/restaurants", {
          params: {
            search:
              searchTerm.trim() || undefined,
            category:
              selectedCategory || undefined,
          },
        });

        if (Array.isArray(response.data)) {
          setRestaurants(response.data);
        } else {
          setRestaurants([]);

          setError(
            "The restaurant response has an invalid format."
          );
        }
      } catch (requestError) {
        console.error(
          "Failed to load restaurants",
          requestError
        );

        setRestaurants([]);

        setError(
          getApiErrorMessage(
            requestError
          )
        );
      } finally {
        setLoading(false);
      }
    },
    [searchTerm, selectedCategory]
  );

  useEffect(() => {
    fetchCategories();
  }, [fetchCategories]);

  useEffect(() => {
    const timeoutId = window.setTimeout(
      () => {
        fetchRestaurants();
      },
      300
    );

    return () => {
      window.clearTimeout(timeoutId);
    };
  }, [fetchRestaurants]);

  const openRestaurantMenu = (
    restaurantId: string
  ) => {
    navigate(
      `/customer/restaurants/${restaurantId}/menu`
    );
  };

  const formatTime = (
    time?: string | null
  ): string => {
    if (!time) {
      return "Not specified";
    }

    return time.slice(0, 5);
  };

  const normalizeStatus = (
    status?: string | null
  ): string => {
    return status?.trim().toUpperCase() || "UNKNOWN";
  };

  return (
    <div className="food-page">
      <CustomerHeader />

      <main className="mx-auto max-w-6xl px-6 py-10">
        <header className="mb-8 flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 className="text-3xl font-black text-slate-950">
              Restaurants
            </h1>

            <p className="mt-2 text-slate-500">
              Browse available restaurants and
              order food.
            </p>
          </div>

          <button
            type="button"
            onClick={() =>
              navigate("/customer/dashboard")
            }
            className="food-button-secondary px-5 py-2 text-sm"
          >
            ← Back to dashboard
          </button>
        </header>

        <section className="mb-8 flex flex-col gap-3 sm:flex-row">
          <input
            type="search"
            value={searchTerm}
            onChange={(event) =>
              setSearchTerm(event.target.value)
            }
            placeholder="Search by name, address or description..."
            className="food-input flex-1 px-5 py-3"
          />

          <select
            value={selectedCategory}
            onChange={(event) =>
              setSelectedCategory(
                event.target.value
              )
            }
            className="food-input px-5 py-3"
          >
            <option value="">
              All Categories
            </option>

            {categories.map((category) => (
              <option
                key={category}
                value={category}
              >
                {category}
              </option>
            ))}
          </select>
        </section>

        {error && (
          <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}

            <button
              type="button"
              onClick={fetchRestaurants}
              className="ml-3 font-semibold underline"
            >
              Retry
            </button>
          </div>
        )}

        {loading ? (
          <section className="food-card p-12 text-center">
            <p className="text-slate-500">
              Loading restaurants...
            </p>
          </section>
        ) : restaurants.length === 0 ? (
          <section className="food-card p-12 text-center">
            <h2 className="mt-5 text-xl font-semibold text-slate-900">
              No restaurants found
            </h2>

            <p className="mt-2 text-slate-500">
              No restaurants match the current
              search and category filters.
            </p>

            {(searchTerm ||
              selectedCategory) && (
              <button
                type="button"
                onClick={() => {
                  setSearchTerm("");
                  setSelectedCategory("");
                }}
                className="food-button-primary mt-6 px-6 py-3 text-sm"
              >
                Clear filters
              </button>
            )}
          </section>
        ) : (
          <>
            <p className="mb-5 text-sm text-slate-500">
              {restaurants.length} restaurant
              {restaurants.length === 1
                ? ""
                : "s"}{" "}
              found
            </p>

            <section className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
              {restaurants.map(
                (restaurant) => {
                  const status =
                    restaurant.openNow
                      ? "OPEN"
                      : "CLOSED";

                  return (
                    <article
                      key={restaurant.id}
                      role="button"
                      tabIndex={0}
                      onClick={() =>
                        openRestaurantMenu(
                          restaurant.id
                        )
                      }
                      onKeyDown={(event) => {
                        if (
                          event.key ===
                            "Enter" ||
                          event.key === " "
                        ) {
                          event.preventDefault();

                          openRestaurantMenu(
                            restaurant.id
                          );
                        }
                      }}
                      className="food-card food-card-hover cursor-pointer overflow-hidden p-6"
                    >
                      <div className="mb-4 flex items-start justify-between gap-3">
                        <h2 className="text-lg font-semibold text-slate-900">
                          {restaurant.name}
                        </h2>

                        <span
                          className={`rounded-full px-3 py-1 text-xs font-semibold ${
                            status === "OPEN"
                              ? "bg-emerald-100 text-emerald-700"
                              : status ===
                                  "CLOSED"
                                ? "bg-red-100 text-red-700"
                                : "bg-slate-100 text-slate-500"
                          }`}
                        >
                          {status}
                        </span>
                      </div>

                      {restaurant.category && (
                        <p className="mb-3 text-xs font-bold uppercase text-emerald-600">
                          {restaurant.category}
                        </p>
                      )}

                      {restaurant.reviewCount
                        ? (
                          <p className="mb-3 text-xs font-medium text-slate-500">
                            Rating: {Number(
                              restaurant.averageRating
                              ?? 0
                            ).toFixed(1)} / 5 ({restaurant.reviewCount})
                          </p>
                        )
                        : null}

                      <p className="mb-4 line-clamp-2 min-h-10 text-sm text-slate-500">
                        {restaurant.description ||
                          "No description available."}
                      </p>

                      {/* Pre-order restaurant metadata */}
                      <div className="mb-4 grid grid-cols-3 gap-2 rounded-xl bg-slate-50 p-2.5 text-center text-[11px] font-medium text-slate-600 border border-slate-100">
                        <div>
                          <span className="block text-slate-400 font-normal">Est. Delivery</span>
                          <span className="font-bold text-slate-800">25-35 min</span>
                        </div>
                        <div className="border-x border-slate-200">
                          <span className="block text-slate-400 font-normal">Delivery Fee</span>
                          <span className="font-bold text-slate-800">KES 150</span>
                        </div>
                        <div>
                          <span className="block text-slate-400 font-normal">Min. Order</span>
                          <span className="font-bold text-slate-800">KES 200</span>
                        </div>
                      </div>

                      <div className="space-y-1.5 text-xs text-slate-500">
                        <p className="truncate">
                          {restaurant.address ||
                            "Address not provided"}
                        </p>

                        <p className="font-medium text-slate-600">
                          Hours: {formatTime(
                            restaurant.openingTime
                          )}{" "}
                          -{" "}
                          {formatTime(
                            restaurant.closingTime
                          )}
                        </p>
                      </div>

                      <button
                        type="button"
                        onClick={(event) => {
                          event.stopPropagation();

                          openRestaurantMenu(
                            restaurant.id
                          );
                        }}
                        className="food-button-primary mt-6 w-full px-4 py-3 text-sm"
                      >
                        View Menu
                      </button>
                    </article>
                  );
                }
              )}
            </section>
          </>
        )}
      </main>
    </div>
  );
}

export default RestaurantPage;
