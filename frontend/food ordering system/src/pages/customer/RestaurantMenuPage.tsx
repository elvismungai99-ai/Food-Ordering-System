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
  getRestaurantById,
  type Restaurant,
} from "../../services/RestaurantService";

import {
  useCart,
} from "../../context/CartContext";

import {
  getRestaurantReviews,
  getMenuItemReviews,
  type Review,
} from "../../services/ReviewService";

import {
  CustomizeItemModal,
  type CustomizationOption,
} from "../../components/customer/CustomizeItemModal";
import CustomerHeader from "../../components/customer/CustomerHeader";

function RestaurantMenuPage() {
  const navigate = useNavigate();

  const { restaurantId } =
    useParams<{
      restaurantId: string;
    }>();

  const { addToCart } =
    useCart();

  const [menuItems, setMenuItems] =
    useState<MenuItem[]>([]);

  const [restaurant, setRestaurant] =
    useState<Restaurant | null>(null);

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

  const [customizingItem, setCustomizingItem] =
    useState<MenuItem | null>(null);

  const [
    expandedReviewItemId,
    setExpandedReviewItemId,
  ] = useState<string | null>(null);

  const [
    reviewsByMenuItemId,
    setReviewsByMenuItemId,
  ] = useState<Record<string, Review[]>>({});

  const [
    restaurantReviews,
    setRestaurantReviews,
  ] = useState<Review[]>([]);

  const [
    loadingReviewsItemId,
    setLoadingReviewsItemId,
  ] = useState<string | null>(null);

  const [
    loadingRestaurantReviews,
    setLoadingRestaurantReviews,
  ] = useState(false);

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

        const restaurantData =
          await getRestaurantById(
            restaurantId
          );

        setRestaurant(restaurantData);

        const menuData =
          await getMenuByRestaurant(
            restaurantId
          );

        setMenuItems(menuData);

        try {
          setLoadingRestaurantReviews(true);

          const reviewsData =
            await getRestaurantReviews(
              restaurantId
            );

          setRestaurantReviews(reviewsData);
        } catch (reviewError) {
          console.error(
            "Failed to load restaurant reviews:",
            reviewError
          );

          setRestaurantReviews([]);
        } finally {
          setLoadingRestaurantReviews(false);
        }
      } catch (requestError) {
        console.error(
          "Failed to load restaurant menu:",
          requestError
        );

        setError(
          "Unable to load the restaurant menu."
        );
      } finally {
        setLoading(false);
      }
    };

    loadMenu();
  }, [restaurantId]);

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
            menuItem.category?.trim()
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

  const handleOpenCustomize = (menuItem: MenuItem) => {
    if (!menuItem.available || restaurant?.openNow === false) {
      return;
    }
    setCustomizingItem(menuItem);
  };

  const handleAddToCartWithCustomization = async (
    menuItem: MenuItem,
    customization: CustomizationOption
  ) => {
    try {
      setAddingItemId(menuItem.id);
      setError("");
      setSuccessMessage("");

      await addToCart({
        menuItemId: menuItem.id,
        quantity: customization.quantity,
        selectedSize: customization.size,
        selectedAddOns: customization.addOns,
        removalRequests: customization.removalRequests,
        specialInstructions: customization.specialInstructions,
        extraPrice: customization.extraPrice,
      });

      setSuccessMessage(
        `${menuItem.name} (${customization.size || "Regular"}) added to cart.`
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

  const formatReviewDate = (
    value: string
  ) => {
    return new Date(value).toLocaleDateString(
      "en-KE",
      {
        dateStyle: "medium",
      }
    );
  };

  const toggleMenuItemReviews = async (
    menuItemId: string
  ) => {
    if (expandedReviewItemId === menuItemId) {
      setExpandedReviewItemId(null);
      return;
    }

    setExpandedReviewItemId(menuItemId);

    if (reviewsByMenuItemId[menuItemId]) {
      return;
    }

    try {
      setLoadingReviewsItemId(menuItemId);

      const reviews =
        await getMenuItemReviews(menuItemId);

      setReviewsByMenuItemId((current) => ({
        ...current,
        [menuItemId]: reviews,
      }));
    } catch (requestError) {
      console.error(
        "Failed to load menu item reviews:",
        requestError
      );

      setReviewsByMenuItemId((current) => ({
        ...current,
        [menuItemId]: [],
      }));
    } finally {
      setLoadingReviewsItemId(null);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-100">
        <CustomerHeader />
        <div className="flex h-96 items-center justify-center text-slate-500">
          Loading restaurant menu...
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-100">
      <CustomerHeader />

      <main className="p-6 md:p-8">
        <div className="mx-auto max-w-6xl">
          {/* Restaurant Hero / Metadata Banner */}
          {restaurant && (
            <div className="mb-8 rounded-[28px] border border-slate-200 bg-white p-6 sm:p-8 shadow-sm">
              <div className="flex flex-wrap items-start justify-between gap-4">
                <div>
                  <div className="flex items-center gap-3">
                    <h1 className="text-2xl sm:text-3xl font-black text-slate-950">
                      {restaurant.name}
                    </h1>
                    <span
                      className={`rounded-full px-3 py-0.5 text-xs font-bold ${
                        restaurant.openNow
                          ? "bg-emerald-100 text-emerald-800"
                          : "bg-rose-100 text-rose-800"
                      }`}
                    >
                      {restaurant.openNow ? "Open Now" : "Closed"}
                    </span>
                  </div>

                  {restaurant.category && (
                    <p className="mt-1 text-xs font-bold uppercase tracking-wider text-emerald-700">
                      {restaurant.category}
                    </p>
                  )}

                  <p className="mt-2 text-sm text-slate-600 max-w-2xl">
                    {restaurant.description || "Freshly prepared meals delivered straight to your door."}
                  </p>
                </div>

                <div className="flex gap-2">
                  <button
                    type="button"
                    onClick={() => navigate("/customer/restaurants")}
                    className="rounded-3xl border border-slate-300 bg-white px-4 py-2 text-xs font-bold text-slate-700 hover:bg-slate-50 transition"
                  >
                    ← All Restaurants
                  </button>
                </div>
              </div>

              {/* Pre-order Details: Delivery time, Minimum order, Delivery Fee, Hours */}
              <div className="mt-6 grid grid-cols-2 gap-3 sm:grid-cols-4 rounded-2xl bg-slate-50 p-4 text-center border border-slate-100">
                <div>
                  <span className="block text-xs text-slate-400 font-medium">Estimated Delivery</span>
                  <span className="text-sm font-bold text-slate-900">25 - 35 mins</span>
                </div>
                <div>
                  <span className="block text-xs text-slate-400 font-medium">Delivery Fee</span>
                  <span className="text-sm font-bold text-slate-900">KES 150.00</span>
                </div>
                <div>
                  <span className="block text-xs text-slate-400 font-medium">Minimum Order</span>
                  <span className="text-sm font-bold text-slate-900">KES 200.00</span>
                </div>
                <div>
                  <span className="block text-xs text-slate-400 font-medium">Operating Hours</span>
                  <span className="text-sm font-bold text-slate-900">
                    {restaurant.openingTime && restaurant.closingTime
                      ? `${restaurant.openingTime.slice(0, 5)} - ${restaurant.closingTime.slice(0, 5)}`
                      : "08:00 - 22:00"}
                  </span>
                </div>
              </div>
            </div>
          )}

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

        <section className="mb-8 rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-xl font-semibold text-slate-950">
                Restaurant Reviews
              </h2>

              <p className="mt-1 text-sm text-slate-500">
                See what customers say about this restaurant.
              </p>
            </div>

            {restaurantReviews.length > 0 && (
              <span className="rounded-full bg-indigo-50 px-3 py-1 text-sm font-semibold text-indigo-700">
                {restaurantReviews.length} review{restaurantReviews.length === 1 ? "" : "s"}
              </span>
            )}
          </div>

          {loadingRestaurantReviews ? (
            <p className="mt-4 text-sm text-slate-500">
              Loading restaurant reviews...
            </p>
          ) : restaurantReviews.length > 0 ? (
            <div className="mt-5 grid gap-4 md:grid-cols-2">
              {restaurantReviews.slice(0, 4).map((review) => (
                <article
                  key={review.id}
                  className="rounded-2xl border border-slate-200 bg-slate-50 p-4"
                >
                  <div className="flex items-center justify-between gap-3">
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-semibold text-slate-800">
                        ⭐ {review.rating} / 5
                      </span>
                      <span className="text-xs text-slate-500 font-medium">
                        • {review.customerDisplayName || "Verified Customer"}
                      </span>
                    </div>

                    <span className="text-xs text-slate-400">
                      {formatReviewDate(review.createdAt)}
                    </span>
                  </div>

                  <p className="mt-2 text-sm text-slate-600">
                    {review.comment || "No comment provided."}
                  </p>
                </article>
              ))}
            </div>
          ) : (
            <p className="mt-4 text-sm text-slate-500">
              No restaurant reviews yet.
            </p>
          )}
        </section>

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
              ([category, items]) => (
                <section key={category}>
                  <h2 className="mb-5 text-2xl font-semibold text-slate-950">
                    {category}
                  </h2>

                  <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
                    {items.map(
                      (menuItem) => (
                        <article
                          key={
                            menuItem.id
                          }
                          className="overflow-hidden rounded-[24px] border border-slate-200 bg-white shadow-sm flex flex-col justify-between"
                        >
                          <CustomerMenuItemImage
                            imageUrl={
                              menuItem.imageUrl
                            }
                            name={
                              menuItem.name
                            }
                          />

                          <div className="p-5 flex flex-col flex-1 justify-between">
                            <div>
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

                              <p className="mt-4 text-lg font-bold text-indigo-600">
                                {formatPrice(
                                  menuItem.price
                                )}
                              </p>

                              {menuItem.reviewCount
                                ? (
                                  <button
                                    type="button"
                                    onClick={() =>
                                      toggleMenuItemReviews(
                                        menuItem.id
                                      )
                                    }
                                    className="mt-2 text-left text-sm font-medium text-indigo-600 hover:text-indigo-700"
                                  >
                                    Rating: {Number(
                                      menuItem.averageRating
                                      ?? 0
                                    ).toFixed(1)} / 5 ({menuItem.reviewCount})
                                  </button>
                                )
                                : (
                                  <p className="mt-2 text-sm text-slate-500">
                                    No ratings yet
                                  </p>
                                )}

                              {menuItem.addOns
                                && menuItem.addOns.length > 0 && (
                                <p className="mt-2 text-xs text-slate-500 font-medium">
                                  Add-ons: {menuItem.addOns.join(", ")}
                                </p>
                              )}

                              {expandedReviewItemId === menuItem.id && (
                                <div className="mt-4 rounded-2xl border border-slate-200 bg-slate-50 p-4">
                                  <h4 className="text-sm font-semibold text-slate-900">
                                    Menu Reviews
                                  </h4>

                                  {loadingReviewsItemId === menuItem.id ? (
                                    <p className="mt-3 text-sm text-slate-500">
                                      Loading reviews...
                                    </p>
                                  ) : reviewsByMenuItemId[menuItem.id]
                                      ?.length ? (
                                    <div className="mt-3 space-y-3">
                                      {reviewsByMenuItemId[
                                        menuItem.id
                                      ].map(review => (
                                        <div
                                          key={review.id}
                                          className="border-b border-slate-200 pb-3 last:border-b-0 last:pb-0"
                                        >
                                          <div className="flex items-center justify-between gap-3">
                                            <span className="text-sm font-semibold text-slate-800">
                                              {review.rating} / 5
                                            </span>

                                            <span className="text-xs text-slate-400">
                                              {formatReviewDate(
                                                review.createdAt
                                              )}
                                            </span>
                                          </div>

                                          <p className="mt-2 text-sm text-slate-600">
                                            {review.comment
                                              || "No comment provided."}
                                          </p>
                                        </div>
                                      ))}
                                    </div>
                                  ) : (
                                    <p className="mt-3 text-sm text-slate-500">
                                      No reviews yet.
                                    </p>
                                  )}
                                </div>
                              )}
                            </div>

                            <button
                              type="button"
                              disabled={
                                !menuItem.available
                                || restaurant?.openNow === false
                                || addingItemId === menuItem.id
                              }
                              onClick={() => handleOpenCustomize(menuItem)}
                              className="mt-5 w-full rounded-3xl bg-indigo-600 px-4 py-3 text-sm font-semibold text-white hover:bg-indigo-700 disabled:cursor-not-allowed disabled:bg-slate-300 transition shadow-sm"
                            >
                              {addingItemId === menuItem.id
                                ? "Adding..."
                                : restaurant?.openNow === false
                                ? "Restaurant Closed"
                                : menuItem.available
                                ? "Customize & Add"
                                : "Unavailable"}
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

        {/* Menu Item Customization Modal */}
        <CustomizeItemModal
          item={customizingItem}
          isOpen={customizingItem !== null}
          onClose={() => setCustomizingItem(null)}
          onAddToCart={handleAddToCartWithCustomization}
        />
      </div>
    </main>
  </div>
  );
}

function CustomerMenuItemImage({
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

export default RestaurantMenuPage;
