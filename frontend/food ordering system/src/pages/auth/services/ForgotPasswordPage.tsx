import {
  type ChangeEvent,
  type FormEvent,
  useState,
} from "react";

import {
  Link,
} from "react-router-dom";

import {
  requestPasswordReset,
} from "../../../services/AuthService";

import {
  getApiErrorMessage,
} from "../../../utils/apiError";

function ForgotPasswordPage() {
  const [
    email,
    setEmail,
  ] = useState("");

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
    setEmail(
      event.target.value
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

    try {
      setLoading(true);

      const response =
        await requestPasswordReset(
          email.trim().toLowerCase()
        );

      setSuccess(
        response.message
      );
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
            Forgot Password
          </h1>

          <p className="mt-2 text-sm text-slate-500">
            Enter your account email to receive a reset link.
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
                email
              }
              onChange={
                handleChange
              }
              placeholder="example@email.com"
              autoComplete="email"
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
              ? "Sending link..."
              : "Send Reset Link"}
          </button>

        </form>

        <p className="mt-6 text-center text-sm text-slate-500">

          Remember your password?{" "}

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

export default ForgotPasswordPage;
