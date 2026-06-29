import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { login } from "../../services/AuthService";

function LoginPage() {
    const [formData, setFormData] = useState({
        email: "",
        password: ""
    });
    const [errorMessage, setErrorMessage] = useState("");
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMessage("");

        try {
            const data = await login(formData);
            const role = data.role || data.user?.role || "CUSTOMER";

            if (role === "RESTAURANT_ADMIN") {
                navigate("/restaurant/dashboard");
            } else {
                navigate("/customer/dashboard");
            }
        } catch (error) {
            console.error(error);
            setErrorMessage(error.response?.data?.message || "Login failed. Please check your email and password.");
        }
    };

    return (
        <div className="relative min-h-screen overflow-hidden bg-gradient-to-br from-[#FDF3C4] via-[#E1F0FF] to-white px-4 py-12 sm:px-6">
            <div className="pointer-events-none absolute -left-16 top-10 h-56 w-56 rounded-full bg-[#FFE28A] opacity-50 blur-3xl" />
            <div className="pointer-events-none absolute right-0 top-28 h-64 w-64 rounded-full bg-[#94CDFD] opacity-35 blur-3xl" />

            <div className="relative mx-auto w-full max-w-md rounded-[40px] border border-white/80 bg-white/95 p-8 shadow-[0_30px_90px_rgba(32,78,168,0.14)] backdrop-blur-xl">
                <div className="text-center">
                    <div className="inline-flex rounded-full bg-[#F9E075] px-4 py-1 text-xs font-semibold uppercase tracking-[0.3em] text-[#774D00]">
                        Welcome back
                    </div>
                    <h1 className="mt-5 text-3xl font-semibold tracking-tight text-slate-950 sm:text-4xl">
                        Sign in to your account
                    </h1>
                    <p className="mt-3 text-sm leading-6 text-slate-600">
                        Access your orders, manage your profile, and start ordering delicious meals.
                    </p>
                </div>

                {errorMessage && (
                    <div className="mt-4 rounded-3xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                        {errorMessage}
                    </div>
                )}

                <form className="mt-8 space-y-5" onSubmit={handleSubmit}>
                    <label className="block text-sm font-medium text-slate-700">
                        Email address
                        <input
                            className="mt-3 w-full rounded-3xl border border-[#D6E8FF] bg-[#F7FBFF] px-4 py-3 text-slate-900 outline-none transition focus:border-[#1E70FF] focus:ring-2 focus:ring-[#B6D8FF]"
                            placeholder="Email"
                            type="email"
                            value={formData.email}
                            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                        />
                    </label>

                    <label className="block text-sm font-medium text-slate-700">
                        Password
                        <input
                            className="mt-3 w-full rounded-3xl border border-[#D6E8FF] bg-[#F7FBFF] px-4 py-3 text-slate-900 outline-none transition focus:border-[#1E70FF] focus:ring-2 focus:ring-[#B6D8FF]"
                            type="password"
                            placeholder="Password"
                            value={formData.password}
                            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                        />
                    </label>

                    <button
                        type="submit"
                        className="inline-flex w-full justify-center rounded-3xl bg-[#FFCE4B] px-5 py-3 text-sm font-semibold text-slate-950 shadow-lg shadow-[#FFCE4B]/25 transition hover:bg-[#f2b80f] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#FFCE4B]"
                    >
                        Login
                    </button>
                </form>

                <div className="mt-7 text-center text-sm text-slate-600">
                    <p>
                        Need an account? <Link to="/register" className="font-semibold text-[#1E70FF] hover:text-[#125fd1]">Register now</Link>
                    </p>
                    <p className="mt-3">
                        <Link to="/" className="font-semibold text-[#1E70FF] hover:text-[#125fd1]">Back to Home</Link>
                    </p>
                </div>
            </div>
        </div>
    );
}

export default LoginPage;
