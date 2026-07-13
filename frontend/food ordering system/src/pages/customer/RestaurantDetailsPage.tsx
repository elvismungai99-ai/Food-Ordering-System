import {
  useEffect,
  useMemo,
  useState,
} from "react";

import axios from "axios";

import {
  Link,
  useNavigate,
  useParams,
} from "react-router-dom";

import api from "../../api/axios";
import CustomerHeader from "../../components/customer/CustomerHeader";
import { useCart } from "../../context/CartContext";

interface Restaurant {
  id: string;
  name: string;
  description: string;
  address: string;
  openingTime: string;
  closingTime: string;
  status: string;
  category: string;
}

interface MenuItem {
  id: string;
  restaurantId: string;
  name: string;
  description: string;
  price: number;
  category: string;
  available: boolean;
  imageUrl?: string | null;
}

function RestaurantDetailsPage() {
  const { restaurantId } =
    useParams<{ restaurantId: string }>();

  const navigate = useNavigate();

  const {
    addToCart,
    totalItems,
  } = useCart();

  const [restaurant, setRestaurant] =
    useState<Restaurant | null>(null);

  const [menuItems, setMenuItems] =
    useState<MenuItem[]>([]);

  const [selectedCategory, setSelectedCategory] =
    useState("All");

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState("");

  const [addingItemId, setAddingItemId] =
    useState<string | null>(null);

  const [cartMessage, setCartMessage] =
    useState("");

  const [cartError, setCartError] =
    useState("");

  useEffect(() => {
    const loadRestaurantDetails = async () => {
      if (!restaurantId) {
        setError("Restaurant ID is missing.");
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        setError("");

        const [
          restaurantResponse,
          menuResponse,
        ] = await Promise.all([
          api.get<Restaurant>(
            `/restaurants/${restaurantId}`
          ),

          api.get<MenuItem[]>(
            `/menu-items/restaurant/${restaurantId}`
          ),
        ]);

        setRestaurant(restaurantResponse.data);
        setMenuItems(menuResponse.data);
      } catch (requestError) {
        console.error(
          "Failed to load restaurant details",
          requestError
        );

        setError(
          getErrorMessage(
            requestError,
            "Unable to load restaurant details."
          )
        );
      } finally {
        setLoading(false);
      }
    };

    loadRestaurantDetails();
  }, [restaurantId]);

  const groupedMenuItems = useMemo(() => {
    return menuItems.reduce<
      Record<string, MenuItem[]>
    >((groups, item) => {
      const category =
        item.category?.trim() || "Other";

      if (!groups[category]) {
        groups[category] = [];
      }

      groups[category].push(item);

      return groups;
    }, {});
  }, [menuItems]);

  const categories = useMemo(() => {
    return Object.keys(groupedMenuItems).sort();
  }, [groupedMenuItems]);

  const displayedCategories =
    selectedCategory === "All"
      ? categories
      : categories.filter(
          (category) =>
            category === selectedCategory
        );

  const handleAddToCart = async (
    menuItemId: string
  ) => {
    try {
      setAddingItemId(menuItemId);
      setCartMessage("");
      setCartError("");

      await addToCart(menuItemId, 1);

      setCartMessage(
        "Item added to your cart successfully."
      );
    } catch (requestError) {
      console.error(
        "Failed to add item to cart",
        requestError
      );

      setCartError(
        getErrorMessage(
          requestError,
          "Failed to add item to your cart."
        )
      );
    } finally {
      setAddingItemId(null);
    }
  };

  const formatPrice = (
    price: number
  ): string => {
    return new Intl.NumberFormat("en-KE", {
      style: "currency",
      currency: "KES",
    }).format(price);
  };

  const formatTime = (
    time?: string
  ): string => {
    if (!time) {
      return "Not specified";
    }

    return time.slice(0, 5);
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-100">
        <CustomerHeader />

        <main className="mx-auto max-w-6xl px-6 py-20 text-center">
          <p className="text-slate-500">
            Loading restaurant...
          </p>
        </main>
      </div>
    );
  }

  if (error || !restaurant) {
    return (
      <div className="min-h-screen bg-slate-100">
        <CustomerHeader />

        <main className="mx-auto max-w-6xl px-6 py-10">
          <div className="rounded-2xl border border-red-200 bg-red-50 p-6">
            <p className="text-red-700">
              {error || "Restaurant not found."}
            </p>

            <Link
              to="/customer/restaurants"
              className="mt-4 inline-block font-semibold text-indigo-600 transition hover:text-indigo-700"
            >
              Return to restaurants
            </Link>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-100">
      <CustomerHeader />

      <section className="bg-slate-950 text-white">
        <div className="mx-auto max-w-6xl px-6 py-12">
          <div className="flex flex-wrap items-center justify-between gap-4">
            <Link
              to="/customer/restaurants"
              className="text-sm font-medium text-indigo-300 transition hover:text-indigo-200"
            >
              ← Back to restaurants
            </Link>

            <button
              type="button"
              onClick={() =>
                navigate("/customer/cart")
              }
              className="rounded-3xl bg-white px-5 py-2 text-sm font-semibold text-indigo-600 transition hover:bg-slate-100"
            >
              View Cart ({totalItems})
            </button>
          </div>

          <div className="mt-8 flex flex-col justify-between gap-6 md:flex-row md:items-end">
            <div>
              {restaurant.category && (
                <span className="inline-block rounded-full bg-indigo-600 px-3 py-1 text-sm font-medium">
                  {restaurant.category}
                </span>
              )}

              <h1 className="mt-4 text-4xl font-bold">
                {restaurant.name}
              </h1>

              <p className="mt-3 max-w-2xl text-slate-300">
                {restaurant.description ||
                  "No restaurant description is available."}
              </p>

              <p className="mt-4 text-sm text-slate-300">
                📍{" "}
                {restaurant.address ||
                  "Address not provided"}
              </p>
            </div>

            <div className="rounded-2xl bg-white/10 p-5">
              <p className="text-sm text-slate-300">
                Opening hours
              </p>

              <p className="mt-1 font-semibold">
                {formatTime(
                  restaurant.openingTime
                )}
                {" — "}
                {formatTime(
                  restaurant.closingTime
                )}
              </p>

              <p
                className={`mt-2 font-semibold ${
                  restaurant.status === "OPEN"
                    ? "text-teal-300"
                    : "text-red-300"
                }`}
              >
                {restaurant.status}
              </p>
            </div>
          </div>
        </div>
      </section>

      <main className="mx-auto max-w-6xl px-6 py-10">
        <div>
          <h2 className="text-3xl font-bold text-slate-950">
            Menu
          </h2>

          <p className="mt-2 text-slate-500">
            Browse available food by category.
          </p>
        </div>

        {cartMessage && (
          <div className="mt-6 flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-teal-200 bg-teal-50 px-4 py-3 text-sm text-teal-700">
            <span>{cartMessage}</span>

            <button
              type="button"
              onClick={() =>
                navigate("/customer/cart")
              }
              className="font-semibold text-teal-800 transition hover:text-teal-900"
            >
              Open cart
            </button>
          </div>
        )}

        {cartError && (
          <div className="mt-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {cartError}
          </div>
        )}

        {menuItems.length > 0 && (
          <div className="my-8 flex flex-wrap gap-3">
            <button
              type="button"
              onClick={() =>
                setSelectedCategory("All")
              }
              className={`rounded-full px-5 py-2 text-sm font-medium transition ${
                selectedCategory === "All"
                  ? "bg-indigo-600 text-white"
                  : "border border-slate-300 bg-white text-slate-700 hover:border-indigo-500"
              }`}
            >
              All
            </button>

            {categories.map((category) => (
              <button
                key={category}
                type="button"
                onClick={() =>
                  setSelectedCategory(category)
                }
                className={`rounded-full px-5 py-2 text-sm font-medium transition ${
                  selectedCategory === category
                    ? "bg-indigo-600 text-white"
                    : "border border-slate-300 bg-white text-slate-700 hover:border-indigo-500"
                }`}
              >
                {category}
              </button>
            ))}
          </div>
        )}

        {menuItems.length === 0 ? (
          <section className="mt-8 rounded-[24px] border border-slate-200 bg-white p-10 text-center">
            <h3 className="text-xl font-semibold text-slate-900">
              No menu items available
            </h3>

            <p className="mt-2 text-slate-500">
              This restaurant has not added menu items yet.
            </p>
          </section>
        ) : (
          <div className="mt-10 space-y-12">
            {displayedCategories.map(
              (category) => (
                <section key={category}>
                  <div className="mb-5 flex items-center gap-4">
                    <h3 className="text-2xl font-bold text-slate-900">
                      {category}
                    </h3>

                    <div className="h-px flex-1 bg-slate-200" />
                  </div>

                  <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
                    {groupedMenuItems[
                      category
                    ].map((item) => (
                      <article
                        key={item.id}
                        className="overflow-hidden rounded-[24px] border border-slate-200 bg-white shadow-sm transition hover:-translate-y-1 hover:shadow-md"
                      >
                        {item.imageUrl ? (
                          <img
                            src={item.imageUrl}
                            alt={item.name}
                            className="h-48 w-full object-cover"
                          />
                        ) : (
                          <div className="flex h-48 items-center justify-center bg-slate-100 text-sm text-slate-400">
                            No image
                          </div>
                        )}

                        <div className="p-5">
                          <div className="flex items-start justify-between gap-4">
                            <h4 className="text-lg font-semibold text-slate-900">
                              {item.name}
                            </h4>

                            <span className="whitespace-nowrap font-bold text-indigo-600">
                              {formatPrice(
                                item.price
                              )}
                            </span>
                          </div>

                          <p className="mt-3 text-sm leading-6 text-slate-500">
                            {item.description ||
                              "No description available."}
                          </p>

                          <div className="mt-5 flex items-center justify-between gap-3">
                            <span
                              className={`rounded-full px-3 py-1 text-xs font-medium ${
                                item.available
                                  ? "bg-teal-100 text-teal-700"
                                  : "bg-red-100 text-red-700"
                              }`}
                            >
                              {item.available
                                ? "Available"
                                : "Unavailable"}
                            </span>

                            <button
                              type="button"
                              disabled={
                                !item.available ||
                                addingItemId ===
                                  item.id
                              }
                              onClick={() =>
                                handleAddToCart(
                                  item.id
                                )
                              }
                              className="rounded-3xl bg-indigo-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-indigo-700 disabled:cursor-not-allowed disabled:bg-slate-300"
                            >
                              {addingItemId ===
                              item.id
                                ? "Adding..."
                                : item.available
                                  ? "Add to Cart"
                                  : "Unavailable"}
                            </button>
                          </div>
                        </div>
                      </article>
                    ))}
                  </div>
                </section>
              )
            )}
          </div>
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
    const responseData = error.response?.data;

    if (typeof responseData === "string") {
      return responseData;
    }

    if (
      responseData &&
      typeof responseData === "object" &&
      "message" in responseData &&
      typeof responseData.message === "string"
    ) {
      return responseData.message;
    }
  }

  return fallbackMessage;
}

export default RestaurantDetailsPage;