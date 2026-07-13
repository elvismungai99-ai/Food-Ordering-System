import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { login } from "../../../services/AuthService";

function LoginPage() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const data = await login(formData);
      console.log("Login response:", data);
      console.log("Role:", data.role);
      console.log("localStorage role:", localStorage.getItem("role"));

      if (data.role === "CUSTOMER") {
        navigate("/customer/dashboard");
      } else if (data.role === "OWNER") {
        navigate("/restaurant/dashboard");
      } else if (data.role === "SUPER_ADMIN") {
        navigate("/admin/dashboard");
      } else {
        navigate("/");
      }
    } catch (err) {
      setError("Login failed. Please check your credentials and try again.");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-100 px-4 py-10 flex items-center justify-center">
      <div className="w-full max-w-md rounded-[32px] border border-slate-200 bg-white/95 p-8 shadow-[0_24px_80px_rgba(15,23,42,0.08)] backdrop-blur-xl">
        <div className="mb-6 text-center">
          <p className="text-sm uppercase tracking-[0.35em] text-indigo-600">Welcome back</p>
          <h1 className="mt-3 text-3xl font-semibold text-slate-950">Sign in to your account</h1>
          <p className="mt-2 text-sm text-slate-500">Order fresh meals and manage your account with ease.</p>
        </div>

        {/* Error Message */}
        {error && (
          <div className="mb-4 rounded-2xl bg-red-50 border border-red-200 px-4 py-3 text-sm text-red-600">
            {error}
          </div>
        )}

        <form className="space-y-5" onSubmit={handleSubmit}>
          <label className="block text-sm font-medium text-slate-700">
            Email address
            <input
              className="mt-2 w-full rounded-3xl border border-slate-300 bg-slate-50 px-4 py-3 text-slate-900 outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
              placeholder="Email"
              type="email"
              required
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
            />
          </label>

          <label className="block text-sm font-medium text-slate-700">
            Password
            <input
              className="mt-2 w-full rounded-3xl border border-slate-300 bg-slate-50 px-4 py-3 text-slate-900 outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
              type="password"
              placeholder="Password"
              required
              value={formData.password}
              onChange={(e) => setFormData({ ...formData, password: e.target.value })}
            />
          </label>

          <button
            type="submit"
            disabled={loading}
            className="inline-flex w-full justify-center rounded-3xl bg-indigo-600 px-5 py-3 text-sm font-semibold text-white shadow-lg shadow-indigo-500/15 transition hover:bg-indigo-700 disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {loading ? "Signing in..." : "Login"}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-500">
          Don't have an account?{" "}
          <Link to="/register" className="font-semibold text-indigo-600 hover:text-indigo-700">
            Register here
          </Link>
        </p>
      </div>
    </div>
  );
}

export default LoginPage;