import {
  type ChangeEvent,
  type FormEvent,
  useMemo,
  useState,
} from "react";

import {
  Link,
  useSearchParams,
} from "react-router-dom";

import {
  resetPassword,
} from "../../../services/AuthService";

import {
  getApiErrorMessage,
} from "../../../utils/apiError";

interface ResetPasswordFormData {
  password: string;
  confirmPassword: string;
}

function ResetPasswordPage() {
  const [
    searchParams,
  ] = useSearchParams();

  const token =
    useMemo(
      () => searchParams.get("token") ?? "",
      [
        searchParams,
      ]
    );

  const [
    formData,
    setFormData,
  ] = useState<ResetPasswordFormData>({
    password: "",
    confirmPassword: "",
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
    success,
    setSuccess,
  ] = useState("");

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

    setError("");
    setSuccess("");
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    setError("");
    setSuccess("");

    if (!token) {
      setError(
        "This reset link is missing its token."
      );

      return;
    }

    if (
      formData.password
      !== formData.confirmPassword
    ) {
      setError(
        "Passwords do not match."
      );

      return;
    }

    try {
      setLoading(true);

      const response =
        await resetPassword(
          token,
          formData.password
        );

      setSuccess(
        response.message
      );

      setFormData({
        password: "",
        confirmPassword: "",
      });
    } catch (requestError) {
      setError(
        getApiErrorMessage(
          requestError
        )
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-100 p-6">

      <div className="w-full max-w-md rounded-[28px] border border-slate-200 bg-white p-8 shadow-sm">

        <div className="text-center">

          <h1 className="text-3xl font-bold text-slate-950">
            Reset Password
          </h1>

          <p className="mt-2 text-sm text-slate-500">
            Choose a new password for your account.
          </p>

        </div>

        {error && (
          <div className="mt-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}

        {success && (
          <div className="mt-6 rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
            {success}
          </div>
        )}

        <form
          onSubmit={
            handleSubmit
          }
          className="mt-8 space-y-5"
        >

          <div>

            <label
              htmlFor="password"
              className="block text-sm font-medium text-slate-700"
            >
              New Password
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
              placeholder="Enter a new password"
              autoComplete="new-password"
              className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-500"
            />

          </div>

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
              placeholder="Confirm your new password"
              autoComplete="new-password"
              className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-500"
            />

          </div>

          <button
            type="submit"
            disabled={
              loading
            }
            className="w-full rounded-3xl bg-indigo-600 px-6 py-3 font-semibold text-white transition hover:bg-indigo-700 disabled:cursor-not-allowed disabled:bg-slate-300"
          >
            {loading
              ? "Resetting password..."
              : "Reset Password"}
          </button>

        </form>

        <p className="mt-6 text-center text-sm text-slate-500">

          Back to{" "}

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

export default ResetPasswordPage;
