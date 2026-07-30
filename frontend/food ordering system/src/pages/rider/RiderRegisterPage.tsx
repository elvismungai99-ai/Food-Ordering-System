import {
  type FormEvent,
  useState,
} from "react";

import {
  Link,
  useNavigate,
} from "react-router-dom";

import {
  registerRider,
  type VehicleType,
} from "../../services/RiderService";

import {
  getApiErrorMessage,
} from "../../utils/apiError";

function RiderRegisterPage() {
  const navigate =
    useNavigate();

  const [
    formData,
    setFormData,
  ] = useState({
    fullName: "",
    phoneNumber: "",
    email: "",
    password: "",
    vehicleType: "MOTORCYCLE" as VehicleType,
    licencePlate: "",
  });

  const [
    loading,
    setLoading,
  ] = useState(false);

  const [
    error,
    setError,
  ] = useState("");

  const handleSubmit = async (
    event: FormEvent
  ) => {
    event.preventDefault();

    try {
      setLoading(true);
      setError("");

      const data =
        await registerRider({
          ...formData,
          email:
            formData.email
              .trim()
              .toLowerCase(),
          fullName:
            formData.fullName.trim(),
          phoneNumber:
            formData.phoneNumber.trim(),
          licencePlate:
            formData.licencePlate.trim(),
        });

      localStorage.setItem(
        "token",
        data.token
      );
      localStorage.setItem(
        "role",
        data.role
      );
      localStorage.setItem(
        "userId",
        data.userId
      );
      localStorage.setItem(
        "firstName",
        data.firstName ?? ""
      );

      navigate("/rider/dashboard");
    } catch (requestError) {
      console.error(
        "Failed to register rider:",
        requestError
      );
      setError(
        getApiErrorMessage(requestError)
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-100 p-6">
      <div className="w-full max-w-lg rounded-[28px] border border-slate-200 bg-white p-8 shadow-sm">
        <h1 className="text-3xl font-bold text-slate-950">
          Rider Registration
        </h1>

        <p className="mt-2 text-sm text-slate-500">
          Create your delivery rider account.
        </p>

        {error && (
          <div className="mt-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}

        <form
          onSubmit={handleSubmit}
          className="mt-8 space-y-5"
        >
          <RiderInput
            label="Full Name"
            value={formData.fullName}
            onChange={value =>
              setFormData(current => ({
                ...current,
                fullName: value,
              }))
            }
          />

          <RiderInput
            label="Phone Number"
            value={formData.phoneNumber}
            onChange={value =>
              setFormData(current => ({
                ...current,
                phoneNumber: value,
              }))
            }
          />

          <RiderInput
            label="Email Address"
            type="email"
            value={formData.email}
            onChange={value =>
              setFormData(current => ({
                ...current,
                email: value,
              }))
            }
          />

          <RiderInput
            label="Password"
            type="password"
            value={formData.password}
            onChange={value =>
              setFormData(current => ({
                ...current,
                password: value,
              }))
            }
          />

          <label className="block text-sm font-medium text-slate-700">
            Vehicle Type
            <select
              value={formData.vehicleType}
              onChange={event =>
                setFormData(current => ({
                  ...current,
                  vehicleType:
                    event.target.value as VehicleType,
                }))
              }
              className="mt-2 w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 outline-none focus:border-indigo-500"
            >
              <option value="BICYCLE">
                Bicycle
              </option>
              <option value="MOTORCYCLE">
                Motorcycle
              </option>
              <option value="CAR">
                Car
              </option>
            </select>
          </label>

          <RiderInput
            label="Licence Plate"
            value={formData.licencePlate}
            onChange={value =>
              setFormData(current => ({
                ...current,
                licencePlate: value,
              }))
            }
          />

          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-3xl bg-indigo-600 px-6 py-3 font-semibold text-white disabled:bg-slate-300"
          >
            {loading
              ? "Creating rider account..."
              : "Create Rider Account"}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-500">
          Already registered?{" "}
          <Link
            to="/login"
            className="font-semibold text-indigo-600"
          >
            Login
          </Link>
        </p>
      </div>
    </main>
  );
}

function RiderInput({
  label,
  value,
  onChange,
  type = "text",
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
  type?: string;
}) {
  return (
    <label className="block text-sm font-medium text-slate-700">
      {label}
      <input
        type={type}
        required
        value={value}
        onChange={event =>
          onChange(event.target.value)
        }
        className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-500"
      />
    </label>
  );
}

export default RiderRegisterPage;
