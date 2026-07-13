import {
  useCallback,
  useEffect,
  useState,
} from "react";

import axios from "axios";
import { useNavigate } from "react-router-dom";

import api from "../../api/axios";
import CustomerHeader from "../../components/customer/CustomerHeader";

interface Restaurant {
  id: string;
  name: string;
  description?: string | null;
  address?: string | null;
  openingTime?: string | null;
  closingTime?: string | null;
  status?: string | null;
  category?: string | null;
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

        console.log(
          "Restaurants API response:",
          response.data
        );

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
          getErrorMessage(
            requestError,
            "Failed to load restaurants. Please try again."
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
    <div className="min-h-screen bg-slate-100">
      <CustomerHeader />

      <main className="mx-auto max-w-6xl px-6 py-10">
        <header className="mb-8 flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 className="text-3xl font-semibold text-slate-950">
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
            className="rounded-3xl border border-slate-300 bg-white px-5 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
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
            className="flex-1 rounded-3xl border border-slate-300 bg-white px-5 py-3 text-slate-900 outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
          />

          <select
            value={selectedCategory}
            onChange={(event) =>
              setSelectedCategory(
                event.target.value
              )
            }
            className="rounded-3xl border border-slate-300 bg-white px-5 py-3 text-slate-900 outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
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
          <section className="rounded-[24px] border border-slate-200 bg-white p-12 text-center">
            <p className="text-slate-500">
              Loading restaurants...
            </p>
          </section>
        ) : restaurants.length === 0 ? (
          <section className="rounded-[24px] border border-slate-200 bg-white p-12 text-center">
            <div className="text-5xl">
              🍽️
            </div>

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
                className="mt-6 rounded-3xl bg-indigo-600 px-6 py-3 text-sm font-semibold text-white transition hover:bg-indigo-700"
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
                    normalizeStatus(
                      restaurant.status
                    );

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
                      className="cursor-pointer rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm transition hover:-translate-y-1 hover:shadow-md"
                    >
                      <div className="mb-4 flex items-start justify-between gap-3">
                        <h2 className="text-lg font-semibold text-slate-900">
                          {restaurant.name}
                        </h2>

                        <span
                          className={`rounded-full px-3 py-1 text-xs font-semibold ${
                            status === "OPEN"
                              ? "bg-teal-100 text-teal-700"
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
                        <p className="mb-3 text-xs font-medium uppercase tracking-wide text-indigo-500">
                          {restaurant.category}
                        </p>
                      )}

                      <p className="mb-4 line-clamp-2 min-h-10 text-sm text-slate-500">
                        {restaurant.description ||
                          "No description available."}
                      </p>

                      <div className="space-y-2 text-xs text-slate-500">
                        <p>
                          📍{" "}
                          {restaurant.address ||
                            "Address not provided"}
                        </p>

                        <p>
                          🕐{" "}
                          {formatTime(
                            restaurant.openingTime
                          )}
                          {" — "}
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
                        className="mt-6 w-full rounded-3xl bg-indigo-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-indigo-700"
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

function getErrorMessage(
  error: unknown,
  fallbackMessage: string
): string {
  if (axios.isAxiosError(error)) {
    const responseData =
      error.response?.data;

    if (typeof responseData === "string") {
      return responseData;
    }

    if (
      responseData &&
      typeof responseData === "object" &&
      "message" in responseData &&
      typeof responseData.message ===
        "string"
    ) {
      return responseData.message;
    }

    if (error.response?.status === 404) {
      return "The restaurants endpoint was not found.";
    }

    if (error.response?.status === 401) {
      return "Your session has expired. Please log in again.";
    }

    if (error.response?.status === 403) {
      return "You do not have permission to view restaurants.";
    }

    if (!error.response) {
      return "The backend server could not be reached.";
    }
  }

  return fallbackMessage;
}

export default RestaurantPage;