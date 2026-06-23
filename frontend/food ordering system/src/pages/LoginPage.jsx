import { useState } from "react";
import { login } from "../services/AuthService";

function LoginPage() {
    const [formData, setFormData] = useState({
        email: "",
        password: ""
    });

    const handleSubmit = async (e) => {

        e.preventDefault();

        try {

            await login(formData);

            alert("Login Successful");

        } catch (error) {

            console.error(error);

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

                <form className="space-y-5" onSubmit={handleSubmit}>
                    <label className="block text-sm font-medium text-slate-700">
                        Email address
                        <input
                            className="mt-2 w-full rounded-3xl border border-slate-300 bg-slate-50 px-4 py-3 text-slate-900 outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
                            placeholder="Email"
                            type="email"
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
                            value={formData.password}
                            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                        />
                    </label>

                    <button
                        type="submit"
                        className="inline-flex w-full justify-center rounded-3xl bg-indigo-600 px-5 py-3 text-sm font-semibold text-white shadow-lg shadow-indigo-500/15 transition hover:bg-indigo-700 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                    >
                        Login
                    </button>
                </form>
            </div>
        </div>
    );
}

export default LoginPage;