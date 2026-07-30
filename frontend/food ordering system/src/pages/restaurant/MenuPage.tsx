import {
  type ChangeEvent,
  type FormEvent,
  useCallback,
  useEffect,
  useState,
} from "react";

import {
  useNavigate,
} from "react-router-dom";

import {
  createMenuItem,
  deleteMenuItem,
  getRestaurantMenu,
  updateMenuItem,
  type MenuItem,
  type MenuItemRequest,
} from "../../services/MenuItemService";

import {
  getApiErrorMessage,
  getApiFieldErrors,
} from "../../utils/apiError";

interface MenuFormData {
  name: string;
  description: string;
  price: string;
  category: string;
  addOns: string;
  available: boolean;
  imageUrl: string;
}

const emptyForm: MenuFormData = {
  name: "",
  description: "",
  price: "",
  category: "",
  addOns: "",
  available: true,
  imageUrl: "",
};

const MAX_IMAGE_UPLOAD_BYTES =
  5 * 1024 * 1024;

const MAX_OPTIMIZED_IMAGE_BYTES =
  700 * 1024;

const MAX_IMAGE_DIMENSION =
  1200;

const IMAGE_QUALITY =
  0.82;

const ALLOWED_IMAGE_TYPES =
  new Set([
    "image/jpeg",
    "image/png",
    "image/webp",
  ]);

function MenuPage() {
  const navigate =
    useNavigate();

  const [
    menuItems,
    setMenuItems,
  ] = useState<MenuItem[]>([]);

  const [
    formData,
    setFormData,
  ] = useState<MenuFormData>(
    emptyForm
  );

  const [
    editingItemId,
    setEditingItemId,
  ] = useState<
    string | null
  >(null);

  const [
    loading,
    setLoading,
  ] = useState(true);

  const [
    saving,
    setSaving,
  ] = useState(false);

  const [
    optimizingImage,
    setOptimizingImage,
  ] = useState(false);

  const [
    deletingItemId,
    setDeletingItemId,
  ] = useState<
    string | null
  >(null);

  const [
    error,
    setError,
  ] = useState("");

  const [
    successMessage,
    setSuccessMessage,
  ] = useState("");

  /*
   * Backend validation errors:
   *
   * {
   *   name: "...",
   *   price: "...",
   *   imageUrl: "..."
   * }
   */
  const [
    fieldErrors,
    setFieldErrors,
  ] = useState<
    Record<string, string>
  >({});

  const [
    imageLoadFailed,
    setImageLoadFailed,
  ] = useState(false);

  const restaurantId =
    localStorage.getItem(
      "restaurantId"
    );

  // =========================================================
  // LOAD MENU
  // =========================================================

  const loadMenu =
    useCallback(
      async () => {

        if (!restaurantId) {

          setError(
            "Restaurant ID was not found. Return to the restaurant dashboard first."
          );

          setLoading(false);

          return;
        }

        try {

          setLoading(true);
          setError("");

          const data =
            await getRestaurantMenu(
              restaurantId
            );

          setMenuItems(
            data
          );

        } catch (
          requestError
        ) {

          console.error(
            "Failed to load restaurant menu:",
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
      },
      [restaurantId]
    );

  useEffect(() => {
    loadMenu();
  }, [loadMenu]);

  // =========================================================
  // HANDLE INPUT
  // =========================================================

  const handleInputChange = (
    event: ChangeEvent<
      HTMLInputElement
      | HTMLSelectElement
      | HTMLTextAreaElement
    >
  ) => {

    const {
      name,
      value,
      type,
    } = event.target;

    /*
     * As soon as the user edits a field,
     * remove the old validation message
     * belonging to that field.
     */
    setFieldErrors(
      current => {

        const updated = {
          ...current,
        };

        delete updated[name];

        return updated;
      }
    );

    if (
      type === "checkbox"
      && event.target
        instanceof HTMLInputElement
    ) {

      setFormData(
        current => ({
          ...current,

          [name]:
            event.target.checked,
        })
      );

      return;
    }

    if (
      name === "imageUrl"
    ) {
      setImageLoadFailed(false);
    }

    setFormData(
      current => ({
        ...current,
        [name]: value,
      })
    );
  };

  const handleImageFileChange = async (
    event: ChangeEvent<HTMLInputElement>
  ) => {

    const file =
      event.target.files?.[0];

    event.target.value = "";

    if (!file) {
      return;
    }

    setError("");
    setSuccessMessage("");
    setImageLoadFailed(false);

    setFieldErrors(
      current => {
        const updated = {
          ...current,
        };

        delete updated.imageUrl;

        return updated;
      }
    );

    if (
      !ALLOWED_IMAGE_TYPES.has(
        file.type
      )
    ) {
      setFieldErrors(
        current => ({
          ...current,
          imageUrl:
            "Upload a JPEG, PNG or WebP image.",
        })
      );

      return;
    }

    if (
      file.size
      > MAX_IMAGE_UPLOAD_BYTES
    ) {
      setFieldErrors(
        current => ({
          ...current,
          imageUrl:
            "Upload an image smaller than 5 MB.",
        })
      );

      return;
    }

    try {
      setOptimizingImage(true);

      const optimizedImageUrl =
        await optimizeImageFile(file);

      setFormData(
        current => ({
          ...current,
          imageUrl: optimizedImageUrl,
        })
      );

      setSuccessMessage(
        "Image optimized successfully."
      );
    } catch (uploadError) {
      setFieldErrors(
        current => ({
          ...current,
          imageUrl:
            uploadError instanceof Error
              ? uploadError.message
              : "Unable to optimize this image.",
        })
      );
    } finally {
      setOptimizingImage(false);
    }
  };

  // =========================================================
  // RESET FORM
  // =========================================================

  const resetForm = () => {

    setFormData(
      emptyForm
    );

    setEditingItemId(
      null
    );

    setImageLoadFailed(
      false
    );

    setFieldErrors({});
  };

  // =========================================================
  // SAVE ITEM
  // =========================================================

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ) => {

    event.preventDefault();

    setError("");
    setSuccessMessage("");
    setFieldErrors({});

    const request: MenuItemRequest = {

      name:
        formData.name.trim(),

      description:
        formData
          .description
          .trim(),

      price:
        Number(
          formData.price
        ),

      category:
        formData
          .category
          .trim(),

      addOns:
        formData
          .addOns
          .split(",")
          .map(addOn =>
            addOn.trim()
          )
          .filter(Boolean),

      available:
        formData.available,

      imageUrl:
        formData
          .imageUrl
          .trim()
        || null,
    };

    try {

      setSaving(true);

      if (
        editingItemId
      ) {

        const updated =
          await updateMenuItem(
            editingItemId,
            request
          );

        setMenuItems(
          current =>
            current.map(
              item =>
                item.id
                  === updated.id
                  ? updated
                  : item
            )
        );

        setSuccessMessage(
          "Menu item updated successfully."
        );

      } else {

        const created =
          await createMenuItem(
            request
          );

        setMenuItems(
          current => [
            ...current,
            created,
          ]
        );

        setSuccessMessage(
          "Menu item created successfully."
        );
      }

      resetForm();

    } catch (
      requestError
    ) {

      console.error(
        "Failed to save menu item:",
        requestError
      );

      /*
       * General API message.
       */
      setError(
        getApiErrorMessage(
          requestError
        )
      );

      /*
       * Field-level backend errors.
       */
      setFieldErrors(
        getApiFieldErrors(
          requestError
        )
      );

    } finally {

      setSaving(false);
    }
  };

  // =========================================================
  // START EDIT
  // =========================================================

  const handleStartEditing = (
    menuItem: MenuItem
  ) => {

    setEditingItemId(
      menuItem.id
    );

    setFormData({

      name:
        menuItem.name,

      description:
        menuItem.description
        ?? "",

      price:
        String(
          menuItem.price
        ),

      category:
        menuItem.category
        ?? "",

      addOns:
        menuItem.addOns
          ?.join(", ")
        ?? "",

      available:
        menuItem.available,

      imageUrl:
        menuItem.imageUrl
        ?? "",
    });

    setImageLoadFailed(false);
    setFieldErrors({});
    setError("");
    setSuccessMessage("");

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  };

  // =========================================================
  // DELETE ITEM
  // =========================================================

  const handleDelete = async (
    menuItemId: string
  ) => {

    const confirmed =
      window.confirm(
        "Are you sure you want to delete this menu item?"
      );

    if (!confirmed) {
      return;
    }

    try {

      setDeletingItemId(
        menuItemId
      );

      setError("");
      setSuccessMessage("");

      await deleteMenuItem(
        menuItemId
      );

      setMenuItems(
        current =>
          current.filter(
            item =>
              item.id
              !== menuItemId
          )
      );

      if (
        editingItemId
        === menuItemId
      ) {
        resetForm();
      }

      setSuccessMessage(
        "Menu item deleted successfully."
      );

    } catch (
      requestError
    ) {

      console.error(
        "Failed to delete menu item:",
        requestError
      );

      setError(
        getApiErrorMessage(
          requestError
        )
      );

    } finally {

      setDeletingItemId(
        null
      );
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
          Loading menu...
        </p>

      </div>
    );
  }

  return (
    <main className="min-h-screen bg-slate-100 p-6 md:p-8">

      <div className="mx-auto max-w-7xl">

        {/* HEADER */}

        <div className="mb-8 flex flex-wrap items-center justify-between gap-4">

          <div>

            <h1 className="text-3xl font-bold text-slate-950">
              Manage Menu
            </h1>

            <p className="mt-2 text-slate-500">
              Create and edit menu items shown to customers.
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
            ← Dashboard
          </button>

        </div>

        {/* GENERAL ERROR */}

        {error && (

          <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">

            {error}

          </div>

        )}

        {/* SUCCESS */}

        {successMessage && (

          <div className="mb-6 rounded-2xl border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-700">

            {successMessage}

          </div>

        )}

        {/* FORM */}

        <section className="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">

          <h2 className="text-xl font-semibold text-slate-950">

            {editingItemId
              ? "Edit Menu Item"
              : "Create Menu Item"}

          </h2>

          <form
            onSubmit={
              handleSubmit
            }
            className="mt-6 grid gap-5 md:grid-cols-2"
          >

            {/* NAME */}

            <div>

              <label
                htmlFor="name"
                className="block text-sm font-medium text-slate-700"
              >
                Name
              </label>

              <input
                id="name"
                name="name"
                type="text"
                value={
                  formData.name
                }
                onChange={
                  handleInputChange
                }
                className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-500"
              />

              {fieldErrors.name && (

                <p className="mt-1 text-sm text-red-600">
                  {
                    fieldErrors.name
                  }
                </p>

              )}

            </div>

            {/* CATEGORY */}

            <div>

              <label
                htmlFor="category"
                className="block text-sm font-medium text-slate-700"
              >
                Category
              </label>

              <select
                id="category"
                name="category"
                value={
                  formData.category
                }
                onChange={
                  handleInputChange
                }
                className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-500"
              >
                <option value="">
                  Select category
                </option>
                <option value="Meals">
                  Meals
                </option>
                <option value="Drinks">
                  Drinks
                </option>
                <option value="Dessert">
                  Dessert
                </option>
              </select>

              {fieldErrors.category && (

                <p className="mt-1 text-sm text-red-600">
                  {
                    fieldErrors.category
                  }
                </p>

              )}

            </div>

            {/* PRICE */}

            <div>

              <label
                htmlFor="price"
                className="block text-sm font-medium text-slate-700"
              >
                Price
              </label>

              <input
                id="price"
                name="price"
                type="number"
                step="0.01"
                value={
                  formData.price
                }
                onChange={
                  handleInputChange
                }
                className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-500"
              />

              {fieldErrors.price && (

                <p className="mt-1 text-sm text-red-600">
                  {
                    fieldErrors.price
                  }
                </p>

              )}

            </div>

            {/* AVAILABLE */}

            <div className="flex items-center pt-7">

              <label className="flex items-center gap-3 text-sm font-medium text-slate-700">

                <input
                  name="available"
                  type="checkbox"
                  checked={
                    formData.available
                  }
                  onChange={
                    handleInputChange
                  }
                  className="h-5 w-5"
                />

                Available to customers

              </label>

            </div>

            {/* DESCRIPTION */}

            <div className="md:col-span-2">

              <label
                htmlFor="description"
                className="block text-sm font-medium text-slate-700"
              >
                Description
              </label>

              <textarea
                id="description"
                name="description"
                rows={4}
                value={
                  formData.description
                }
                onChange={
                  handleInputChange
                }
                className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-500"
              />

              {fieldErrors.description && (

                <p className="mt-1 text-sm text-red-600">
                  {
                    fieldErrors.description
                  }
                </p>

              )}

            </div>

            <div className="md:col-span-2">

              <label
                htmlFor="addOns"
                className="block text-sm font-medium text-slate-700"
              >
                Add-ons
              </label>

              <input
                id="addOns"
                name="addOns"
                type="text"
                value={
                  formData.addOns
                }
                onChange={
                  handleInputChange
                }
                placeholder="Extra cheese, extra sauce, toppings"
                className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-500"
              />

              {fieldErrors.addOns && (

                <p className="mt-1 text-sm text-red-600">
                  {
                    fieldErrors.addOns
                  }
                </p>

              )}

            </div>

            {/* IMAGE URL */}

            <div className="md:col-span-2">

              <label
                htmlFor="imageUrl"
                className="block text-sm font-medium text-slate-700"
              >
                Menu item image URL
              </label>

              <input
                id="imageUrl"
                name="imageUrl"
                type="url"
                value={
                  formData.imageUrl
                }
                onChange={
                  handleInputChange
                }
                placeholder="Paste a public image URL"
                className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-500"
              />

              <label
                htmlFor="imageUpload"
                className="mt-4 block text-sm font-medium text-slate-700"
              >
                Upload image
              </label>

              <input
                id="imageUpload"
                type="file"
                accept="image/jpeg,image/png,image/webp"
                disabled={
                  optimizingImage
                }
                onChange={
                  handleImageFileChange
                }
                className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 text-sm text-slate-700 file:mr-4 file:rounded-2xl file:border-0 file:bg-indigo-50 file:px-4 file:py-2 file:text-sm file:font-semibold file:text-indigo-700 disabled:opacity-60"
              />

              {optimizingImage && (

                <p className="mt-2 text-sm text-slate-500">
                  Optimizing image...
                </p>

              )}

              {fieldErrors.imageUrl && (

                <p className="mt-1 text-sm text-red-600">
                  {
                    fieldErrors.imageUrl
                  }
                </p>

              )}

              {formData
                .imageUrl
                .trim()
                && !imageLoadFailed && (

                <div className="mt-4 h-56 w-full overflow-hidden rounded-2xl bg-slate-100">

                  <img
                    src={
                      formData
                        .imageUrl
                        .trim()
                    }
                    alt="Menu item preview"
                    className="h-full w-full object-cover"
                    loading="lazy"
                    decoding="async"
                    onError={() =>
                      setImageLoadFailed(
                        true
                      )
                    }
                  />

                </div>

              )}

              {imageLoadFailed && (

                <p className="mt-3 text-sm text-red-600">
                  The supplied image could not be loaded.
                </p>

              )}

            </div>

            {/* ACTIONS */}

            <div className="flex flex-wrap gap-3 md:col-span-2">

              <button
                type="submit"
                disabled={
                  saving
                }
                className="rounded-3xl bg-indigo-600 px-6 py-3 text-sm font-semibold text-white disabled:bg-slate-300"
              >
                {saving
                  ? "Saving..."
                  : editingItemId
                    ? "Update Menu Item"
                    : "Create Menu Item"}
              </button>

              {editingItemId && (

                <button
                  type="button"
                  onClick={
                    resetForm
                  }
                  className="rounded-3xl border border-slate-300 px-6 py-3 text-sm font-semibold text-slate-700"
                >
                  Cancel Editing
                </button>

              )}

            </div>

          </form>

        </section>

        {/* CURRENT MENU */}

        <section className="mt-10">

          <h2 className="text-xl font-semibold text-slate-950">
            Current Menu
          </h2>

          {menuItems.length === 0 ? (

            <div className="mt-5 rounded-[24px] border border-slate-200 bg-white p-10 text-center">

              <p className="text-slate-500">
                No menu items have been created yet.
              </p>

            </div>

          ) : (

            <div className="mt-5 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">

              {menuItems.map(
                menuItem => (

                  <article
                    key={
                      menuItem.id
                    }
                    className="overflow-hidden rounded-[24px] border border-slate-200 bg-white shadow-sm"
                  >

                    <MenuItemImage
                      imageUrl={
                        menuItem.imageUrl
                      }
                      name={
                        menuItem.name
                      }
                    />

                    <div className="p-5">

                      <h3 className="text-lg font-semibold text-slate-900">
                        {
                          menuItem.name
                        }
                      </h3>

                      <p className="mt-2 text-sm text-slate-500">
                        {
                          menuItem.description
                        }
                      </p>

                      <p className="mt-4 text-lg font-bold text-indigo-600">
                        {
                          formatPrice(
                            menuItem.price
                          )
                        }
                      </p>

                      <p className="mt-2 text-sm text-slate-500">
                        {
                          menuItem.available
                            ? "Available"
                            : "Unavailable"
                        }
                      </p>

                      {menuItem.addOns
                        && menuItem.addOns.length > 0 && (
                        <p className="mt-2 text-sm text-slate-500">
                          Add-ons: {menuItem.addOns.join(", ")}
                        </p>
                      )}

                      <div className="mt-5 flex gap-3">

                        <button
                          type="button"
                          onClick={() =>
                            handleStartEditing(
                              menuItem
                            )
                          }
                          className="flex-1 rounded-3xl bg-indigo-600 px-4 py-2 text-sm font-semibold text-white"
                        >
                          Edit
                        </button>

                        <button
                          type="button"
                          disabled={
                            deletingItemId
                            === menuItem.id
                          }
                          onClick={() =>
                            handleDelete(
                              menuItem.id
                            )
                          }
                          className="flex-1 rounded-3xl border border-red-300 px-4 py-2 text-sm font-semibold text-red-600"
                        >
                          {deletingItemId
                          === menuItem.id
                            ? "Deleting..."
                            : "Delete"}
                        </button>

                      </div>

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

function MenuItemImage({
  imageUrl,
  name,
}: {
  imageUrl?: string | null;
  name: string;
}) {

  const [
    failed,
    setFailed,
  ] = useState(false);

  const canDisplay =
    Boolean(
      imageUrl?.trim()
    )
    && !failed;

  return (
    <div className="h-48 w-full overflow-hidden bg-slate-100">

      {canDisplay ? (

        <img
          src={
            imageUrl ?? ""
          }
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

async function optimizeImageFile(
  file: File
): Promise<string> {

  const dataUrl =
    await readFileAsDataUrl(file);

  const image =
    await loadImage(dataUrl);

  const scale =
    Math.min(
      1,
      MAX_IMAGE_DIMENSION / image.width,
      MAX_IMAGE_DIMENSION / image.height
    );

  const width =
    Math.max(
      1,
      Math.round(
        image.width * scale
      )
    );

  const height =
    Math.max(
      1,
      Math.round(
        image.height * scale
      )
    );

  const canvas =
    document.createElement("canvas");

  canvas.width = width;
  canvas.height = height;

  const context =
    canvas.getContext("2d");

  if (!context) {
    throw new Error(
      "Unable to prepare this image."
    );
  }

  context.drawImage(
    image,
    0,
    0,
    width,
    height
  );

  const optimizedDataUrl =
    canvas.toDataURL(
      "image/webp",
      IMAGE_QUALITY
    );

  if (
    estimateDataUrlBytes(
      optimizedDataUrl
    )
    > MAX_OPTIMIZED_IMAGE_BYTES
  ) {
    throw new Error(
      "Choose a smaller image or crop it before uploading."
    );
  }

  return optimizedDataUrl;
}

function readFileAsDataUrl(
  file: File
): Promise<string> {

  return new Promise(
    (resolve, reject) => {
      const reader =
        new FileReader();

      reader.onload = () => {
        if (
          typeof reader.result
          === "string"
        ) {
          resolve(reader.result);
          return;
        }

        reject(
          new Error(
            "Unable to read this image."
          )
        );
      };

      reader.onerror = () =>
        reject(
          new Error(
            "Unable to read this image."
          )
        );

      reader.readAsDataURL(file);
    }
  );
}

function loadImage(
  source: string
): Promise<HTMLImageElement> {

  return new Promise(
    (resolve, reject) => {
      const image =
        new Image();

      image.onload = () =>
        resolve(image);

      image.onerror = () =>
        reject(
          new Error(
            "Unable to load this image."
          )
        );

      image.src = source;
    }
  );
}

function estimateDataUrlBytes(
  dataUrl: string
): number {

  const base64 =
    dataUrl.split(",")[1]
    ?? "";

  return Math.ceil(
    base64.length * 0.75
  );
}

export default MenuPage;
