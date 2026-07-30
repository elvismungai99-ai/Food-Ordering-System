import {
  type FormEvent,
  useEffect,
  useState,
} from "react";

import {
  useNavigate,
} from "react-router-dom";

import {
  createRestaurant,
  getMyRestaurant,
  type Restaurant,
  type RestaurantRequest,
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

  const [
    formError,
    setFormError,
  ] = useState("");

  const [
    saving,
    setSaving,
  ] = useState(false);

  const [
    formData,
    setFormData,
  ] = useState<RestaurantRequest>({
    name: "",
    description: "",
    address: "",
    openingTime: "08:00",
    closingTime: "22:00",
    category: "Meals",
  });

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

  const handleFormChange = (
    field: keyof RestaurantRequest,
    value: string
  ) => {
    setFormData((current) => ({
      ...current,
      [field]: value,
    }));
  };

  const handleCreateRestaurant = async (
    event: FormEvent
  ) => {
    event.preventDefault();

    try {
      setSaving(true);
      setFormError("");

      const createdRestaurant =
        await createRestaurant(formData);

      setRestaurant(createdRestaurant);
      setError("");

      if (createdRestaurant.id) {
        localStorage.setItem(
          "restaurantId",
          createdRestaurant.id
        );
      }
    } catch (requestError) {
      console.error(
        "Failed to submit restaurant:",
        requestError
      );

      setFormError(
        getApiErrorMessage(
          requestError
        )
      );
    } finally {
      setSaving(false);
    }
  };

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

        {!restaurant && (
          <section className="mt-6 rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">
            <div className="mb-6">
              <h2 className="text-xl font-semibold text-slate-950">
                Submit Restaurant For Approval
              </h2>

              <p className="mt-2 text-sm text-slate-500">
                New restaurants are sent to the super admin before customers can see or order from them.
              </p>
            </div>

            {formError && (
              <div className="mb-4 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                {formError}
              </div>
            )}

            <form
              onSubmit={handleCreateRestaurant}
              className="grid gap-5 md:grid-cols-2"
            >
              <label className="text-sm font-medium text-slate-700">
                Restaurant Name
                <input
                  type="text"
                  required
                  value={formData.name}
                  onChange={(event) =>
                    handleFormChange(
                      "name",
                      event.target.value
                    )
                  }
                  className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-orange-500"
                />
              </label>

              <label className="text-sm font-medium text-slate-700">
                Category
                <select
                  value={formData.category}
                  onChange={(event) =>
                    handleFormChange(
                      "category",
                      event.target.value
                    )
                  }
                  className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-orange-500"
                >
                  <option value="Meals">Meals</option>
                  <option value="Drinks">Drinks</option>
                  <option value="Dessert">Dessert</option>
                </select>
              </label>

              <label className="text-sm font-medium text-slate-700 md:col-span-2">
                Address
                <input
                  type="text"
                  required
                  value={formData.address}
                  onChange={(event) =>
                    handleFormChange(
                      "address",
                      event.target.value
                    )
                  }
                  className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-orange-500"
                />
              </label>

              <label className="text-sm font-medium text-slate-700">
                Opening Time
                <input
                  type="time"
                  required
                  value={formData.openingTime}
                  onChange={(event) =>
                    handleFormChange(
                      "openingTime",
                      event.target.value
                    )
                  }
                  className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-orange-500"
                />
              </label>

              <label className="text-sm font-medium text-slate-700">
                Closing Time
                <input
                  type="time"
                  required
                  value={formData.closingTime}
                  onChange={(event) =>
                    handleFormChange(
                      "closingTime",
                      event.target.value
                    )
                  }
                  className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-orange-500"
                />
              </label>

              <label className="text-sm font-medium text-slate-700 md:col-span-2">
                Description
                <textarea
                  value={formData.description}
                  onChange={(event) =>
                    handleFormChange(
                      "description",
                      event.target.value
                    )
                  }
                  className="mt-2 min-h-28 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-orange-500"
                />
              </label>

              <div className="md:col-span-2">
                <button
                  type="submit"
                  disabled={saving}
                  className="rounded-3xl bg-orange-500 px-5 py-3 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:bg-orange-300"
                >
                  {
                    saving
                      ? "Submitting..."
                      : "Submit For Approval"
                  }
                </button>
              </div>
            </form>
          </section>
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
                    restaurant.status === "APPROVED"
                      ? "bg-green-100 text-green-700"
                      : restaurant.status === "PENDING_APPROVAL"
                        ? "bg-amber-100 text-amber-700"
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
