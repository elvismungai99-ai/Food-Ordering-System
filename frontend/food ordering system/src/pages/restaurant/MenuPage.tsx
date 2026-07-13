import {
  type FormEvent,
  useEffect,
  useState,
} from "react";

import axios from "axios";
import { useNavigate } from "react-router-dom";

import api from "../../api/axios";

import {
  createMenuItem,
  deleteMenuItem,
  getRestaurantMenu,
  updateMenuItem,
  type MenuItem,
} from "../../services/MenuItemService";

interface Restaurant {
  id: string;
  name: string;
}

interface MenuFormState {
  name: string;
  description: string;
  price: string;
  category: string;
  available: boolean;
  imageUrl: string;
}

const initialFormState: MenuFormState = {
  name: "",
  description: "",
  price: "",
  category: "",
  available: true,
  imageUrl: "",
};

function MenuPage() {
  const navigate = useNavigate();

  const [restaurant, setRestaurant] =
    useState<Restaurant | null>(null);

  const [menuItems, setMenuItems] =
    useState<MenuItem[]>([]);

  const [createForm, setCreateForm] =
    useState<MenuFormState>(
      initialFormState
    );

  const [editForm, setEditForm] =
    useState<MenuFormState>(
      initialFormState
    );

  const [editingItem, setEditingItem] =
    useState<MenuItem | null>(null);

  const [loading, setLoading] =
    useState(true);

  const [saving, setSaving] =
    useState(false);

  const [deletingItemId, setDeletingItemId] =
    useState<string | null>(null);

  const [error, setError] =
    useState("");

  const [successMessage, setSuccessMessage] =
    useState("");

  useEffect(() => {
    loadPageData();
  }, []);

  const loadPageData = async () => {
    try {
      setLoading(true);
      setError("");

      const restaurantResponse =
        await api.get<Restaurant>(
          "/restaurants/me"
        );

      const restaurantData =
        restaurantResponse.data;

      setRestaurant(restaurantData);

      const menuData =
        await getRestaurantMenu(
          restaurantData.id
        );

      setMenuItems(menuData);
    } catch (requestError) {
      console.error(
        "Failed to load restaurant menu",
        requestError
      );

      setError(
        getErrorMessage(
          requestError,
          "Failed to load your restaurant menu."
        )
      );
    } finally {
      setLoading(false);
    }
  };

  const handleCreateMenuItem = async (
    event: FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    const price = parsePrice(
      createForm.price
    );

    if (price === null) {
      setError(
        "Enter a valid price greater than zero."
      );
      return;
    }

    if (!createForm.name.trim()) {
      setError(
        "Menu item name is required."
      );
      return;
    }

    try {
      setSaving(true);
      setError("");
      setSuccessMessage("");

      const createdItem =
        await createMenuItem({
          name: createForm.name.trim(),
          description:
            createForm.description.trim()
              || null,
          price,
          category:
            createForm.category.trim()
              || null,
          available:
            createForm.available,
          imageUrl:
            createForm.imageUrl.trim()
              || null,
        });

      setMenuItems((currentItems) => [
        ...currentItems,
        createdItem,
      ]);

      setCreateForm(initialFormState);

      setSuccessMessage(
        "Menu item created successfully."
      );
    } catch (requestError) {
      console.error(
        "Failed to create menu item",
        requestError
      );

      setError(
        getErrorMessage(
          requestError,
          "Failed to create the menu item."
        )
      );
    } finally {
      setSaving(false);
    }
  };

  const openEditForm = (
    item: MenuItem
  ) => {
    setEditingItem(item);

    setEditForm({
      name: item.name,
      description:
        item.description || "",
      price: String(item.price),
      category:
        item.category || "",
      available:
        item.available,
      imageUrl:
        item.imageUrl || "",
    });

    setError("");
    setSuccessMessage("");
  };

  const closeEditForm = () => {
    setEditingItem(null);
    setEditForm(initialFormState);
  };

  const handleUpdateMenuItem = async (
    event: FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    if (!editingItem) {
      return;
    }

    const price = parsePrice(
      editForm.price
    );

    if (price === null) {
      setError(
        "Enter a valid price greater than zero."
      );
      return;
    }

    if (!editForm.name.trim()) {
      setError(
        "Menu item name is required."
      );
      return;
    }

    try {
      setSaving(true);
      setError("");
      setSuccessMessage("");

      const updatedItem =
        await updateMenuItem(
          editingItem.id,
          {
            name:
              editForm.name.trim(),
            description:
              editForm.description.trim()
                || null,
            price,
            category:
              editForm.category.trim()
                || null,
            available:
              editForm.available,
            imageUrl:
              editForm.imageUrl.trim()
                || null,
          }
        );

      setMenuItems((currentItems) =>
        currentItems.map((item) =>
          item.id === updatedItem.id
            ? updatedItem
            : item
        )
      );

      closeEditForm();

      setSuccessMessage(
        "Menu item updated successfully."
      );
    } catch (requestError) {
      console.error(
        "Failed to update menu item",
        requestError
      );

      setError(
        getErrorMessage(
          requestError,
          "Failed to update the menu item."
        )
      );
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteMenuItem = async (
    menuItemId: string
  ) => {
    const confirmed = window.confirm(
      "Delete this menu item?"
    );

    if (!confirmed) {
      return;
    }

    try {
      setDeletingItemId(menuItemId);
      setError("");
      setSuccessMessage("");

      await deleteMenuItem(menuItemId);

      setMenuItems((currentItems) =>
        currentItems.filter(
          (item) =>
            item.id !== menuItemId
        )
      );

      setSuccessMessage(
        "Menu item deleted successfully."
      );
    } catch (requestError) {
      console.error(
        "Failed to delete menu item",
        requestError
      );

      setError(
        getErrorMessage(
          requestError,
          "Failed to delete the menu item."
        )
      );
    } finally {
      setDeletingItemId(null);
    }
  };

  const formatPrice = (
    price: number
  ) => {
    return new Intl.NumberFormat(
      "en-KE",
      {
        style: "currency",
        currency: "KES",
      }
    ).format(price);
  };

  if (loading) {
    return (
      <main className="min-h-screen bg-slate-100 p-8">
        <p className="py-20 text-center text-slate-500">
          Loading menu...
        </p>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-slate-100 p-6 md:p-8">
      <div className="mx-auto max-w-6xl">
        <header className="mb-8 flex flex-wrap items-center justify-between gap-4">
          <div>
            <p className="text-sm font-medium text-indigo-600">
              Restaurant management
            </p>

            <h1 className="mt-1 text-3xl font-bold text-slate-950">
              {restaurant?.name || "Your restaurant"} menu
            </h1>

            <p className="mt-2 text-slate-500">
              Create, edit and remove menu items.
            </p>
          </div>

          <button
            type="button"
            onClick={() =>
              navigate(
                "/restaurant/dashboard"
              )
            }
            className="rounded-3xl border border-slate-300 bg-white px-5 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
          >
            ← Dashboard
          </button>
        </header>

        {error && (
          <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}

        {successMessage && (
          <div className="mb-6 rounded-2xl border border-teal-200 bg-teal-50 px-4 py-3 text-sm text-teal-700">
            {successMessage}
          </div>
        )}

        <section className="mb-8 rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">
          <h2 className="text-xl font-semibold text-slate-900">
            Add menu item
          </h2>

          <form
            onSubmit={
              handleCreateMenuItem
            }
            className="mt-6 grid gap-5 md:grid-cols-2"
          >
            <MenuFormFields
              form={createForm}
              setForm={setCreateForm}
            />

            <div className="md:col-span-2">
              <button
                type="submit"
                disabled={saving}
                className="rounded-3xl bg-indigo-600 px-6 py-3 text-sm font-semibold text-white hover:bg-indigo-700 disabled:cursor-not-allowed disabled:bg-slate-300"
              >
                {saving
                  ? "Saving..."
                  : "Add menu item"}
              </button>
            </div>
          </form>
        </section>

        <section>
          <div className="mb-5 flex items-center justify-between">
            <h2 className="text-2xl font-bold text-slate-950">
              Current menu
            </h2>

            <span className="text-sm text-slate-500">
              {menuItems.length} item
              {menuItems.length === 1
                ? ""
                : "s"}
            </span>
          </div>

          {menuItems.length === 0 ? (
            <div className="rounded-[24px] border border-slate-200 bg-white p-10 text-center">
              <p className="text-slate-500">
                No menu items have been added.
              </p>
            </div>
          ) : (
            <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
              {menuItems.map((item) => (
                <article
                  key={item.id}
                  className="overflow-hidden rounded-[24px] border border-slate-200 bg-white shadow-sm"
                >
                  {item.imageUrl ? (
                    <img
                      src={item.imageUrl}
                      alt={item.name}
                      className="h-44 w-full object-cover"
                    />
                  ) : (
                    <div className="flex h-44 items-center justify-center bg-slate-100 text-sm text-slate-400">
                      No image
                    </div>
                  )}

                  <div className="p-5">
                    <div className="flex items-start justify-between gap-3">
                      <h3 className="text-lg font-semibold text-slate-900">
                        {item.name}
                      </h3>

                      <span className="font-bold text-indigo-600">
                        {formatPrice(
                          item.price
                        )}
                      </span>
                    </div>

                    {item.category && (
                      <p className="mt-2 text-xs font-medium uppercase tracking-wide text-indigo-500">
                        {item.category}
                      </p>
                    )}

                    <p className="mt-3 min-h-10 text-sm text-slate-500">
                      {item.description
                        || "No description available."}
                    </p>

                    <p
                      className={`mt-4 text-sm font-semibold ${
                        item.available
                          ? "text-teal-600"
                          : "text-red-600"
                      }`}
                    >
                      {item.available
                        ? "Available"
                        : "Unavailable"}
                    </p>

                    <div className="mt-5 flex gap-3">
                      <button
                        type="button"
                        onClick={() =>
                          openEditForm(item)
                        }
                        className="flex-1 rounded-3xl bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700"
                      >
                        Edit
                      </button>

                      <button
                        type="button"
                        disabled={
                          deletingItemId
                          === item.id
                        }
                        onClick={() =>
                          handleDeleteMenuItem(
                            item.id
                          )
                        }
                        className="flex-1 rounded-3xl border border-red-200 px-4 py-2 text-sm font-semibold text-red-600 hover:bg-red-50 disabled:opacity-50"
                      >
                        {deletingItemId
                        === item.id
                          ? "Deleting..."
                          : "Delete"}
                      </button>
                    </div>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>
      </div>

      {editingItem && (
        <div className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto bg-black/50 px-4 py-8">
          <div className="w-full max-w-2xl rounded-[24px] bg-white p-6 shadow-xl">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-xl font-semibold text-slate-900">
                  Edit menu item
                </h2>

                <p className="mt-1 text-sm text-slate-500">
                  Update the name, price and other details.
                </p>
              </div>

              <button
                type="button"
                onClick={closeEditForm}
                className="rounded-full px-3 py-2 text-slate-500 hover:bg-slate-100"
              >
                ✕
              </button>
            </div>

            <form
              onSubmit={
                handleUpdateMenuItem
              }
              className="mt-6 grid gap-5 md:grid-cols-2"
            >
              <MenuFormFields
                form={editForm}
                setForm={setEditForm}
              />

              <div className="flex justify-end gap-3 md:col-span-2">
                <button
                  type="button"
                  onClick={closeEditForm}
                  className="rounded-3xl border border-slate-300 px-5 py-2 text-sm font-semibold text-slate-700"
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  disabled={saving}
                  className="rounded-3xl bg-indigo-600 px-5 py-2 text-sm font-semibold text-white hover:bg-indigo-700 disabled:bg-slate-300"
                >
                  {saving
                    ? "Saving..."
                    : "Save changes"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </main>
  );
}

interface MenuFormFieldsProps {
  form: MenuFormState;
  setForm: React.Dispatch<
    React.SetStateAction<MenuFormState>
  >;
}

function MenuFormFields({
  form,
  setForm,
}: MenuFormFieldsProps) {
  return (
    <>
      <label className="block">
        <span className="text-sm font-medium text-slate-700">
          Item name
        </span>

        <input
          type="text"
          required
          value={form.name}
          onChange={(event) =>
            setForm((current) => ({
              ...current,
              name: event.target.value,
            }))
          }
          className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
        />
      </label>

      <label className="block">
        <span className="text-sm font-medium text-slate-700">
          Price
        </span>

        <input
          type="number"
          required
          min="0.01"
          step="0.01"
          value={form.price}
          onChange={(event) =>
            setForm((current) => ({
              ...current,
              price: event.target.value,
            }))
          }
          className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
        />
      </label>

      <label className="block md:col-span-2">
        <span className="text-sm font-medium text-slate-700">
          Description
        </span>

        <textarea
          rows={3}
          value={form.description}
          onChange={(event) =>
            setForm((current) => ({
              ...current,
              description:
                event.target.value,
            }))
          }
          className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
        />
      </label>

      <label className="block">
        <span className="text-sm font-medium text-slate-700">
          Category
        </span>

        <input
          type="text"
          value={form.category}
          onChange={(event) =>
            setForm((current) => ({
              ...current,
              category:
                event.target.value,
            }))
          }
          className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
        />
      </label>

      <label className="block">
        <span className="text-sm font-medium text-slate-700">
          Image URL
        </span>

        <input
          type="url"
          value={form.imageUrl}
          onChange={(event) =>
            setForm((current) => ({
              ...current,
              imageUrl:
                event.target.value,
            }))
          }
          className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
        />
      </label>

      <label className="flex items-center gap-3 md:col-span-2">
        <input
          type="checkbox"
          checked={form.available}
          onChange={(event) =>
            setForm((current) => ({
              ...current,
              available:
                event.target.checked,
            }))
          }
          className="h-4 w-4"
        />

        <span className="text-sm font-medium text-slate-700">
          Item is available for ordering
        </span>
      </label>
    </>
  );
}

function parsePrice(
  value: string
): number | null {
  const price = Number(value);

  if (
    !Number.isFinite(price)
    || price <= 0
  ) {
    return null;
  }

  return price;
}

function getErrorMessage(
  error: unknown,
  fallback: string
): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data;

    if (typeof data === "string") {
      return data;
    }

    if (
      data
      && typeof data === "object"
      && "message" in data
      && typeof data.message === "string"
    ) {
      return data.message;
    }

    if (!error.response) {
      return "The backend server could not be reached.";
    }
  }

  return fallback;
}

export default MenuPage;