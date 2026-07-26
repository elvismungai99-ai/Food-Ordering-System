import {
  type ChangeEvent,
  type FormEvent,
  useEffect,
  useState,
} from "react";

import {
  Link,
  useNavigate,
} from "react-router-dom";

import {
  login,
} from "../../../services/AuthService";

import {
  ApiRequestError,
} from "../../../services/request";

interface LoginFormData {
  email: string;
  password: string;
}

function LoginPage() {
  const navigate =
    useNavigate();

  const [
    formData,
    setFormData,
  ] = useState<LoginFormData>({
    email: "",
    password: "",
  });

  const [
    loading,
    setLoading,
  ] = useState(false);

  const [
    error,
    setError,
  ] = useState("");

  const [
    retrySeconds,
    setRetrySeconds,
  ] = useState(0);

  const isRateLimited =
    retrySeconds > 0;

  const startLoginCooldown = (
    seconds: number
  ) => {
    setRetrySeconds(
      seconds
    );

    setError(
      `Too many login attempts. Try again in ${seconds} seconds, or use Forgot password to reset your password.`
    );
  };

  useEffect(
    () => {
      if (retrySeconds <= 0) {
        return;
      }

      const timer =
        window.setInterval(
          () => {
            setRetrySeconds(
              current =>
                Math.max(
                  current - 1,
                  0
                )
            );
          },
          1000
        );

      return () => {
        window.clearInterval(
          timer
        );
      };
    },
    [
      retrySeconds,
    ]
  );

  // =========================================================
  // HANDLE INPUT CHANGE
  // =========================================================

  const handleChange = (
    event: ChangeEvent<HTMLInputElement>
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
     * Clear the old error once
     * the user starts typing again.
     */
    setError("");
  };

  const validateForm = () => {
    const email =
      formData.email.trim();

    if (!email) {
      setError(
        "Enter your email address."
      );
      return false;
    }

    if (
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(
        email
      )
    ) {
      setError(
        "Enter a valid email address."
      );
      return false;
    }

    if (!formData.password) {
      setError(
        "Enter your password."
      );
      return false;
    }

    return true;
  };

  // =========================================================
  // HANDLE LOGIN
  // =========================================================

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    setError("");

    if (!validateForm()) {
      return;
    }

    if (isRateLimited) {
      setError(
        `Too many login attempts. Please try again in ${retrySeconds} seconds.`
      );

      return;
    }

    try {
      setLoading(true);

      const data =
        await login({
          email:
            formData.email
              .trim(),

          password:
            formData.password,
        });

      const role =
        data.role
          ?.replace(
            /^ROLE_/,
            ""
          )
          .toUpperCase();

      // =====================================================
      // SAVE AUTHENTICATION DATA
      // =====================================================

      localStorage.setItem(
        "token",
        data.token
      );

      localStorage.setItem(
        "role",
        role
      );

      localStorage.setItem(
        "userId",
        data.userId
      );

      localStorage.setItem(
        "firstName",
        data.firstName ?? ""
      );

      // =====================================================
      // ROLE-BASED REDIRECTION
      // =====================================================

      if (
        role === "OWNER"
      ) {
        navigate(
          "/restaurant/dashboard"
        );

        return;
      }

      if (
        role === "CUSTOMER"
      ) {
        navigate(
          "/customer/dashboard"
        );

        return;
      }

      if (
        role === "SUPER_ADMIN"
      ) {
        navigate(
          "/admin/dashboard"
        );

        return;
      }

      /*
       * Safety fallback in case the backend
       * returns an unknown role.
       */
      setError(
        "Your account role is not supported."
      );

    } catch (
      requestError
    ) {

      // =====================================================
      // STEP 11:
      // SPECIAL RATE-LIMIT HANDLING
      // =====================================================

      if (
        requestError
        instanceof ApiRequestError
        && requestError.status === 429
      ) {
        /*
         * The backend LoginRateLimitFilter
         * sends:
         *
         * Retry-After: <seconds>
         */
        const waitSeconds =
          requestError.retryAfterSeconds
          ?? 30;

        startLoginCooldown(
          waitSeconds
        );

        return;
      }

      if (
        requestError
        instanceof ApiRequestError
        && requestError.isNetworkError
      ) {
        startLoginCooldown(
          30
        );

        return;
      }

      if (
        requestError
        instanceof ApiRequestError
        && requestError.status === 401
      ) {
        setError(
          "Email or password is incorrect."
        );

        return;
      }

      // =====================================================
      // ALL OTHER BACKEND ERRORS
      // =====================================================

      setError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to log in. Please try again."
      );

    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-100 p-6">

      <div className="w-full max-w-md rounded-[28px] border border-slate-200 bg-white p-8 shadow-sm">

        {/* HEADER */}

        <div className="text-center">

          <h1 className="text-3xl font-bold text-slate-950">
            Login
          </h1>

          <p className="mt-2 text-sm text-slate-500">
            Sign in to your Food Ordering System account.
          </p>

        </div>

        {/* ERROR MESSAGE */}

        {error && (
          <div className="mt-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}

        {/* LOGIN FORM */}

        <form
          onSubmit={
            handleSubmit
          }
          noValidate
          className="mt-8 space-y-5"
        >

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
              className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-500"
            />

          </div>

          {/* PASSWORD */}

          <div>

            <div className="flex items-center justify-between gap-4">

              <label
                htmlFor="password"
                className="block text-sm font-medium text-slate-700"
              >
                Password
              </label>

              <Link
                to="/forgot-password"
                className="text-sm font-semibold text-indigo-600"
              >
                Forgot password?
              </Link>

            </div>

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
              autoComplete="current-password"
              className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-500"
            />

          </div>

          {/* SUBMIT */}

          <button
            type="submit"
            disabled={
              loading
              || isRateLimited
            }
            className="w-full rounded-3xl bg-indigo-600 px-6 py-3 font-semibold text-white transition hover:bg-indigo-700 disabled:cursor-not-allowed disabled:bg-slate-300"
          >
            {loading
              ? "Logging in..."
              : isRateLimited
                ? `Try again in ${retrySeconds}s`
                : "Login"}
          </button>

        </form>

        {/* REGISTER LINK */}

        <p className="mt-6 text-center text-sm text-slate-500">

          Don't have an account?{" "}

          <Link
            to="/register"
            className="font-semibold text-indigo-600"
          >
            Create account
          </Link>

        </p>

      </div>

    </main>
  );
}

export default LoginPage;
