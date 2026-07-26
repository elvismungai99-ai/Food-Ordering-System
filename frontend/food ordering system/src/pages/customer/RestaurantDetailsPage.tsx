import {
  useEffect,
  useState,
} from "react";

import {
  useNavigate,
  useParams,
} from "react-router-dom";

import api from "../../api/axios";

import {
  getMenuByRestaurant,
  type MenuItem,
} from "../../services/MenuItemService";

import {
  useCart,
} from "../../context/CartContext";

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

function RestaurantDetailsPage() {
  const navigate = useNavigate();

  const { restaurantId } =
    useParams<{
      restaurantId: string;
    }>();

  const { addToCart } =
    useCart();

  const [restaurant, setRestaurant] =
    useState<Restaurant | null>(
      null
    );

  const [menuItems, setMenuItems] =
    useState<MenuItem[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState("");

  const [
    successMessage,
    setSuccessMessage,
  ] = useState("");

  const [
    addingItemId,
    setAddingItemId,
  ] = useState<string | null>(null);

  useEffect(() => {
    const loadDetails = async () => {
      if (!restaurantId) {
        setError(
          "Restaurant ID is missing."
        );

        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        setError("");

        const [
          restaurantResponse,
          menuData,
        ] = await Promise.all([
          api.get<Restaurant>(
            `/restaurants/${restaurantId}`
          ),

          getMenuByRestaurant(
            restaurantId
          ),
        ]);

        setRestaurant(
          restaurantResponse.data
        );

        setMenuItems(menuData);
      } catch (requestError) {
        console.error(
          "Failed to load restaurant details:",
          requestError
        );

        setError(
          "Unable to load the restaurant details."
        );
      } finally {
        setLoading(false);
      }
    };

    loadDetails();
  }, [restaurantId]);

  const handleAddToCart = async (
    menuItem: MenuItem
  ) => {
    if (!menuItem.available) {
      return;
    }

    try {
      setAddingItemId(
        menuItem.id
      );

      setError("");
      setSuccessMessage("");

      await addToCart(
        menuItem.id,
        1
      );

      setSuccessMessage(
        `${menuItem.name} added to cart.`
      );
    } catch (requestError) {
      console.error(
        "Failed to add item to cart:",
        requestError
      );

      setError(
        "Unable to add this item to your cart."
      );
    } finally {
      setAddingItemId(null);
    }
  };

  const formatPrice = (
    amount: number
  ) => {
    return new Intl.NumberFormat(
      "en-KE",
      {
        style: "currency",
        currency: "KES",
      }
    ).format(amount);
  };

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-100">
        <p className="text-slate-500">
          Loading restaurant...
        </p>
      </div>
    );
  }

  if (
    error
    && !restaurant
  ) {
    return (
      <div className="min-h-screen bg-slate-100 p-8">
        <div className="mx-auto max-w-3xl rounded-2xl border border-red-200 bg-red-50 p-6">
          <p className="text-red-700">
            {error}
          </p>

          <button
            type="button"
            onClick={() =>
              navigate(
                "/customer/restaurants"
              )
            }
            className="mt-5 rounded-3xl bg-indigo-600 px-5 py-2 text-sm font-semibold text-white"
          >
            Back to Restaurants
          </button>
        </div>
      </div>
    );
  }

  if (!restaurant) {
    return null;
  }

  return (
    <main className="min-h-screen bg-slate-100 p-6 md:p-8">
      <div className="mx-auto max-w-6xl">

        <div className="mb-8 flex flex-wrap items-start justify-between gap-4">
          <div>
            <button
              type="button"
              onClick={() =>
                navigate(
                  "/customer/restaurants"
                )
              }
              className="mb-4 text-sm font-semibold text-indigo-600"
            >
              ← Back to Restaurants
            </button>

            <h1 className="text-3xl font-bold text-slate-950">
              {restaurant.name}
            </h1>

            <p className="mt-2 max-w-3xl text-slate-500">
              {
                restaurant.description
              }
            </p>
          </div>

          <button
            type="button"
            onClick={() =>
              navigate(
                "/customer/cart"
              )
            }
            className="rounded-3xl bg-indigo-600 px-5 py-2 text-sm font-semibold text-white"
          >
            View Cart
          </button>
        </div>

        {error && (
          <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}

        {successMessage && (
          <div className="mb-6 rounded-2xl border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-700">
            {successMessage}
          </div>
        )}

        <section className="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
            <div>
              <p className="text-xs uppercase tracking-wide text-slate-400">
                Category
              </p>

              <p className="mt-2 font-medium text-slate-800">
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

              <p className="mt-2 font-medium text-slate-800">
                {restaurant.address}
              </p>
            </div>

            <div>
              <p className="text-xs uppercase tracking-wide text-slate-400">
                Opening Time
              </p>

              <p className="mt-2 font-medium text-slate-800">
                {
                  restaurant.openingTime
                }
              </p>
            </div>

            <div>
              <p className="text-xs uppercase tracking-wide text-slate-400">
                Closing Time
              </p>

              <p className="mt-2 font-medium text-slate-800">
                {
                  restaurant.closingTime
                }
              </p>
            </div>
          </div>
        </section>

        <section className="mt-10">
          <h2 className="text-2xl font-semibold text-slate-950">
            Menu
          </h2>

          {menuItems.length === 0 ? (
            <div className="mt-5 rounded-[24px] border border-slate-200 bg-white p-10 text-center">
              <p className="text-slate-500">
                This restaurant does not have any menu items yet.
              </p>
            </div>
          ) : (
            <div className="mt-5 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
              {menuItems.map(
                (menuItem) => (
                  <article
                    key={menuItem.id}
                    className="overflow-hidden rounded-[24px] border border-slate-200 bg-white shadow-sm"
                  >
                    <RestaurantDetailsMenuImage
                      imageUrl={
                        menuItem.imageUrl
                      }
                      name={
                        menuItem.name
                      }
                    />

                    <div className="p-5">
                      <div className="flex items-start justify-between gap-3">
                        <div>
                          <h3 className="text-lg font-semibold text-slate-900">
                            {
                              menuItem.name
                            }
                          </h3>

                          <p className="mt-1 text-sm text-slate-500">
                            {
                              menuItem.description
                            }
                          </p>
                        </div>

                        <span
                          className={`rounded-full px-3 py-1 text-xs font-semibold ${
                            menuItem.available
                              ? "bg-green-100 text-green-700"
                              : "bg-slate-100 text-slate-500"
                          }`}
                        >
                          {menuItem.available
                            ? "Available"
                            : "Unavailable"}
                        </span>
                      </div>

                      <p className="mt-5 text-lg font-bold text-indigo-600">
                        {formatPrice(
                          menuItem.price
                        )}
                      </p>

                      <button
                        type="button"
                        disabled={
                          !menuItem.available
                          || addingItemId
                            === menuItem.id
                        }
                        onClick={() =>
                          handleAddToCart(
                            menuItem
                          )
                        }
                        className="mt-5 w-full rounded-3xl bg-indigo-600 px-4 py-3 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:bg-slate-300"
                      >
                        {addingItemId
                        === menuItem.id
                          ? "Adding..."
                          : menuItem.available
                            ? "Add to Cart"
                            : "Unavailable"}
                      </button>
                    </div>
                  </article>
                )
              )}
            </div>
          )}
        </section>
      </div>
    </main>
  );
}

function RestaurantDetailsMenuImage({
  imageUrl,
  name,
}: {
  imageUrl?: string | null;
  name: string;
}) {
  const [failed, setFailed] =
    useState(false);

  const canDisplay =
    Boolean(imageUrl?.trim())
    && !failed;

  return (
    <div className="h-48 w-full overflow-hidden bg-slate-100">
      {canDisplay ? (
        <img
          src={imageUrl ?? ""}
          alt={name}
          className="h-full w-full object-cover"
          loading="lazy"
          decoding="async"
          onError={() =>
            setFailed(true)
          }
        />
      ) : (
        <div className="flex h-full w-full items-center justify-center text-sm text-slate-400">
          No image available
        </div>
      )}
    </div>
  );
}

export default RestaurantDetailsPage;
