import {
  useEffect,
  useMemo,
  useState,
} from "react";

import {
  useNavigate,
  useParams,
} from "react-router-dom";

import {
  getMenuByRestaurant,
  type MenuItem,
} from "../../services/MenuItemService";

import {
  useCart,
} from "../../context/CartContext";

function RestaurantMenuPage() {
  const navigate = useNavigate();

  const {
    restaurantId,
  } = useParams<{
    restaurantId: string;
  }>();

  const {
    addToCart,
  } = useCart();

  const [
    menuItems,
    setMenuItems,
  ] = useState<MenuItem[]>([]);

  const [
    loading,
    setLoading,
  ] = useState(true);

  const [
    error,
    setError,
  ] = useState("");

  const [
    addingItemId,
    setAddingItemId,
  ] = useState<string | null>(
    null
  );

  const [
    successMessage,
    setSuccessMessage,
  ] = useState("");


  /*
   * Load menu items for the selected restaurant.
   */
  useEffect(() => {
    const loadMenu = async () => {
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

        const data =
          await getMenuByRestaurant(
            restaurantId
          );

        setMenuItems(data);

      } catch (requestError) {
        console.error(
          "Failed to load restaurant menu:",
          requestError
        );

        setError(
          "Failed to load the restaurant menu. Please try again."
        );

      } finally {
        setLoading(false);
      }
    };

    loadMenu();

  }, [restaurantId]);


  /*
   * Group menu items by category.
   */
  const groupedMenuItems =
    useMemo(() => {
      return menuItems.reduce<
        Record<string, MenuItem[]>
      >(
        (
          groups,
          menuItem
        ) => {

          const category =
            menuItem.category
              ?.trim()
              || "Other";

          if (!groups[category]) {
            groups[category] = [];
          }

          groups[category].push(
            menuItem
          );

          return groups;
        },
        {}
      );

    }, [menuItems]);


  /*
   * Add menu item to customer's cart.
   */
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
      setAddingItemId(
        null
      );
    }
  };


  if (loading) {
    return (
      <div className="min-h-screen bg-slate-100 flex items-center justify-center">
        <p className="text-slate-500">
          Loading menu...
        </p>
      </div>
    );
  }


  return (
    <div className="min-h-screen bg-slate-100 p-6 md:p-8">

      <div className="mx-auto max-w-6xl">

        {/* Header */}

        <div className="mb-8 flex flex-wrap items-center justify-between gap-4">

          <div>
            <h1 className="text-3xl font-bold text-slate-950">
              Restaurant Menu
            </h1>

            <p className="mt-2 text-slate-500">
              Browse available menu items and add them to your cart.
            </p>
          </div>

          <div className="flex gap-3">

            <button
              type="button"
              onClick={() =>
                navigate(
                  "/customer/restaurants"
                )
              }
              className="rounded-3xl border border-slate-300 bg-white px-5 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
            >
              ← Restaurants
            </button>

            <button
              type="button"
              onClick={() =>
                navigate(
                  "/customer/cart"
                )
              }
              className="rounded-3xl bg-indigo-600 px-5 py-2 text-sm font-semibold text-white hover:bg-indigo-700"
            >
              View Cart
            </button>

          </div>

        </div>


        {/* Error */}

        {error && (
          <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}


        {/* Success */}

        {successMessage && (
          <div className="mb-6 rounded-2xl border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-700">
            {successMessage}
          </div>
        )}


        {/* Empty Menu */}

        {menuItems.length === 0 ? (

          <div className="rounded-[24px] border border-slate-200 bg-white p-10 text-center">

            <p className="text-slate-500">
              This restaurant does not have any menu items yet.
            </p>

          </div>

        ) : (

          <div className="space-y-10">

            {Object.entries(
              groupedMenuItems
            ).map(
              ([
                category,
                items,
              ]) => (

                <section
                  key={category}
                >

                  <h2 className="mb-5 text-2xl font-semibold text-slate-950">
                    {category}
                  </h2>


                  <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">

                    {items.map(
                      (
                        menuItem
                      ) => (

                        <article
                          key={
                            menuItem.id
                          }
                          className="overflow-hidden rounded-[24px] border border-slate-200 bg-white shadow-sm"
                        >

                          {/* Item Image */}

                          {menuItem.imageUrl ? (

                            <img
                              src={
                                menuItem.imageUrl
                              }
                              alt={
                                menuItem.name
                              }
                              className="h-48 w-full object-cover"
                            />

                          ) : (

                            <div className="flex h-48 items-center justify-center bg-slate-100 text-slate-400">
                              No image
                            </div>

                          )}


                          {/* Item Details */}

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
                                className={
                                  `rounded-full px-3 py-1 text-xs font-semibold ${
                                    menuItem.available
                                      ? "bg-green-100 text-green-700"
                                      : "bg-slate-100 text-slate-500"
                                  }`
                                }
                              >
                                {
                                  menuItem.available
                                    ? "Available"
                                    : "Unavailable"
                                }
                              </span>

                            </div>


                            {/* Price */}

                            <p className="mt-5 text-lg font-bold text-indigo-600">
                              KES{" "}
                              {
                                menuItem.price
                              }
                            </p>


                            {/* Add To Cart */}

                            <button
                              type="button"
                              disabled={
                                !menuItem.available
                                ||
                                addingItemId
                                  === menuItem.id
                              }
                              onClick={() =>
                                handleAddToCart(
                                  menuItem
                                )
                              }
                              className="mt-5 w-full rounded-3xl bg-indigo-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-indigo-700 disabled:cursor-not-allowed disabled:bg-slate-300"
                            >
                              {
                                addingItemId
                                  === menuItem.id
                                  ? "Adding..."
                                  : menuItem.available
                                    ? "Add to Cart"
                                    : "Unavailable"
                              }
                            </button>

                          </div>

                        </article>

                      )
                    )}

                  </div>

                </section>

              )
            )}

          </div>

        )}

      </div>

    </div>
  );
}

export default RestaurantMenuPage;