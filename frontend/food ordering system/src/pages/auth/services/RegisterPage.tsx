import {
  type ChangeEvent,
  type FormEvent,
  useState,
} from "react";

import {
  Link,
  useNavigate,
} from "react-router-dom";

import {
  register,
} from "../../../services/AuthService";

import {
  getApiErrorMessage,
  getApiFieldErrors,
} from "../../../utils/apiError";

interface RegisterFormData {

  fullName: string;

  email: string;

  phoneNumber: string;

  password: string;

  confirmPassword: string;

  role:
    | "CUSTOMER"
    | "OWNER";
}

const phoneNumberPattern =
  /^(?:\+254|254|0)?[\s-]?[17](?:[\s-]?\d){8}$/;

const emailPattern =
  /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function RegisterPage() {

  const navigate =
    useNavigate();

  // =========================================================
  // FORM STATE
  // =========================================================

  const [
    formData,
    setFormData,
  ] = useState<RegisterFormData>({

    fullName: "",

    email: "",

    phoneNumber: "",

    password: "",

    confirmPassword: "",

    /*
     * IMPORTANT:
     *
     * A new registration defaults
     * to CUSTOMER.
     */
    role: "CUSTOMER",
  });

  // =========================================================
  // UI STATE
  // =========================================================

  const [
    loading,
    setLoading,
  ] = useState(false);

  const [
    error,
    setError,
  ] = useState("");

  const [
    fieldErrors,
    setFieldErrors,
  ] = useState<
    Record<string, string>
  >({});

  // =========================================================
  // HANDLE INPUT
  // =========================================================

  const handleChange = (
    event: ChangeEvent<
      HTMLInputElement
      | HTMLSelectElement
    >
  ) => {

    const {
      name,
      value,
    } = event.target;

    setFormData(
      current => ({
        ...current,
        [name]: value,
      })
    );

    /*
     * Remove the old validation error
     * belonging to this field once the
     * user starts correcting it.
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

    setError("");
  };

  // =========================================================
  // SUBMIT REGISTRATION
  // =========================================================

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ) => {

    event.preventDefault();

    setError("");

    setFieldErrors({});

    const nextFieldErrors:
      Record<string, string> = {};

    const normalizedPhoneNumber =
      formData
        .phoneNumber
        .trim()
        .replace(/\s+/g, "")
        .replace(/-/g, "");

    if (!formData.fullName.trim()) {
      nextFieldErrors.fullName =
        "Full name is required.";
    }

    if (!formData.email.trim()) {
      nextFieldErrors.email =
        "Email is required.";
    } else if (
      !emailPattern.test(
        formData.email.trim()
      )
    ) {
      nextFieldErrors.email =
        "Enter a valid email address, for example name@example.com.";
    }

    if (!formData.phoneNumber.trim()) {
      nextFieldErrors.phoneNumber =
        "Phone number is required.";
    } else if (
      !phoneNumberPattern.test(
        formData.phoneNumber.trim()
      )
    ) {
      nextFieldErrors.phoneNumber =
        "Enter a valid Kenyan phone number, for example 0712345678 or +254712345678.";
    }

    if (!formData.password) {
      nextFieldErrors.password =
        "Password is required.";
    } else if (formData.password.length < 8) {
      nextFieldErrors.password =
        "Password must be at least 8 characters.";
    } else if (formData.password.length > 72) {
      nextFieldErrors.password =
        "Password must not exceed 72 characters.";
    }

    if (!formData.confirmPassword) {
      nextFieldErrors.confirmPassword =
        "Confirm your password.";
    } else if (
      formData.password
      !== formData.confirmPassword
    ) {
      nextFieldErrors.confirmPassword =
        "Passwords do not match.";
    }

    if (
      Object.keys(
        nextFieldErrors
      ).length > 0
    ) {

      setFieldErrors(
        nextFieldErrors
      );

      return;
    }

    // ---------------------------------------------------------
    // BUILD REQUEST
    // ---------------------------------------------------------

    const registrationData = {

      fullName:
        formData
          .fullName
          .trim(),

      email:
        formData
          .email
          .trim()
          .toLowerCase(),

      phoneNumber:
        normalizedPhoneNumber,

      password:
        formData.password,

      role:
        formData.role,
    };

    try {

      setLoading(true);

      const data =
        await register(
          registrationData
        );

      // =====================================================
      // SAVE AUTH DATA
      // =====================================================

      if (data.token) {

        localStorage.setItem(
          "token",
          data.token
        );
      }

      if (data.role) {

        localStorage.setItem(
          "role",
          data.role
        );
      }

      if (data.userId) {

        localStorage.setItem(
          "userId",
          data.userId
        );
      }

      if (data.firstName) {

        localStorage.setItem(
          "firstName",
          data.firstName
        );
      }

      // =====================================================
      // ROLE REDIRECTION
      // =====================================================

      if (
        data.role === "CUSTOMER"
      ) {

        navigate(
          "/customer/dashboard"
        );

        return;
      }

      if (
        data.role === "OWNER"
      ) {

        navigate(
          "/restaurant/dashboard"
        );

        return;
      }

      /*
       * SUPER_ADMIN should never come through
       * public registration.
       */
      setError(
        "Registration succeeded but the returned account role is not supported."
      );

    } catch (
      requestError
    ) {

      // =====================================================
      // GENERAL ERROR
      // =====================================================

      const apiFieldErrors =
        getApiFieldErrors(
          requestError
        );

      // =====================================================
      // FIELD VALIDATION ERRORS
      // =====================================================

      setFieldErrors(
        apiFieldErrors
      );

      setError(
        Object.keys(
          apiFieldErrors
        ).length > 0
          ? "Please correct the highlighted fields."
          : getApiErrorMessage(
              requestError
            )
      );

    } finally {

      setLoading(false);
    }
  };

  return (
    <main className="food-page flex items-center justify-center p-6">

      <div className="food-card w-full max-w-lg p-8">

        {/* HEADER */}

        <div className="text-center">

          <h1 className="text-3xl font-bold text-slate-950">
            Create Account
          </h1>

          <p className="mt-2 text-sm text-slate-500">
            Create your Food Ordering System account.
          </p>

        </div>

        {/* GENERAL ERROR */}

        {error && (

          <div className="mt-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">

            {error}

          </div>

        )}

        <form
          onSubmit={
            handleSubmit
          }
          className="mt-8 space-y-5"
        >

          {/* FULL NAME */}

          <div>

            <label
              htmlFor="fullName"
              className="block text-sm font-medium text-slate-700"
            >
              Full Name
            </label>

            <input
              id="fullName"
              name="fullName"
              type="text"
              value={
                formData.fullName
              }
              onChange={
                handleChange
              }
              placeholder="Enter your full name"
              className="food-input mt-2 w-full px-4 py-3"
            />

            {fieldErrors.fullName && (

              <p className="mt-1 text-sm text-red-600">
                {fieldErrors.fullName}
              </p>

            )}

          </div>

          {/* EMAIL */}

          <div>

            <label
              htmlFor="email"
              className="block text-sm font-medium text-slate-700"
            >
              Email
            </label>

            <input
              id="email"
              name="email"
              type="email"
              value={
                formData.email
              }
              onChange={
                handleChange
              }
              placeholder="example@email.com"
              autoComplete="email"
              className="food-input mt-2 w-full px-4 py-3"
            />

            {fieldErrors.email && (

              <p className="mt-1 text-sm text-red-600">
                {fieldErrors.email}
              </p>

            )}

          </div>

          {/* PHONE NUMBER */}

          <div>

            <label
              htmlFor="phoneNumber"
              className="block text-sm font-medium text-slate-700"
            >
              Phone Number
            </label>

            <input
              id="phoneNumber"
              name="phoneNumber"
              type="tel"
              value={
                formData.phoneNumber
              }
              onChange={
                handleChange
              }
              placeholder="0712345678"
              autoComplete="tel"
              className="food-input mt-2 w-full px-4 py-3"
            />

            {fieldErrors.phoneNumber && (

              <p className="mt-1 text-sm text-red-600">
                {fieldErrors.phoneNumber}
              </p>

            )}

          </div>

          {/* ACCOUNT TYPE */}

          <div>

            <label
              htmlFor="role"
              className="block text-sm font-medium text-slate-700"
            >
              Account Type
            </label>

            <select
              id="role"
              name="role"
              value={
                formData.role
              }
              onChange={
                handleChange
              }
              className="food-input mt-2 w-full px-4 py-3"
            >

              <option value="CUSTOMER">
                Customer
              </option>

              <option value="OWNER">
                Restaurant Owner
              </option>

            </select>

            {fieldErrors.role && (

              <p className="mt-1 text-sm text-red-600">
                {fieldErrors.role}
              </p>

            )}

          </div>

          {/* PASSWORD */}

          <div>

            <label
              htmlFor="password"
              className="block text-sm font-medium text-slate-700"
            >
              Password
            </label>

            <input
              id="password"
              name="password"
              type="password"
              value={
                formData.password
              }
              onChange={
                handleChange
              }
              placeholder="Enter your password"
              autoComplete="new-password"
              className="food-input mt-2 w-full px-4 py-3"
            />

            {fieldErrors.password && (

              <p className="mt-1 text-sm text-red-600">
                {fieldErrors.password}
              </p>

            )}

          </div>

          {/* CONFIRM PASSWORD */}

          <div>

            <label
              htmlFor="confirmPassword"
              className="block text-sm font-medium text-slate-700"
            >
              Confirm Password
            </label>

            <input
              id="confirmPassword"
              name="confirmPassword"
              type="password"
              value={
                formData.confirmPassword
              }
              onChange={
                handleChange
              }
              placeholder="Confirm password"
              autoComplete="new-password"
              className="food-input mt-2 w-full px-4 py-3"
            />

            {fieldErrors.confirmPassword && (

              <p className="mt-1 text-sm text-red-600">
                {fieldErrors.confirmPassword}
              </p>

            )}

          </div>

          {/* SUBMIT */}

          <button
            type="submit"
            disabled={
              loading
            }
            className="food-button-primary w-full px-6 py-3 disabled:cursor-not-allowed disabled:bg-slate-300"
          >

            {loading
              ? "Creating account..."
              : "Create Account"}

          </button>

        </form>

        {/* LOGIN LINK */}

        <p className="mt-6 text-center text-sm text-slate-500">

          Already have an account?{" "}

          <Link
            to="/login"
            className="font-semibold text-emerald-700 hover:text-emerald-900"
          >
            Login
          </Link>

        </p>

      </div>

    </main>
  );
}

export default RegisterPage;
