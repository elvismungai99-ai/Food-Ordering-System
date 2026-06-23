import { useState } from "react";
import { register } from "../services/AuthService";

function RegisterPage() {

    const [formData, setFormData] = useState({
        firstName: "",
        lastName: "",
        email: "",
        password: "",
        role: "CUSTOMER"
    });

    const handleSubmit = async (e) => {

        e.preventDefault();

        try {

            await register(formData);

            alert("Registration Successful");

        } catch (error) {

            console.error(error);

        }
    };

    return (
        <div className="min-h-screen bg-slate-100 px-4 py-10 flex items-center justify-center">
            <div className="w-full max-w-xl rounded-[32px] border border-slate-200 bg-white/95 p-8 shadow-[0_24px_80px_rgba(15,23,42,0.08)] backdrop-blur-xl">
                <div className="mb-6 text-center">
                    <p className="text-sm uppercase tracking-[0.35em] text-teal-600">Create account</p>
                    <h1 className="mt-3 text-3xl font-semibold text-slate-950">Register for Food Ordering</h1>
                    <p className="mt-2 text-sm text-slate-500">Choose a role and start ordering or managing your restaurant.</p>
                </div>

                <form className="grid gap-5" onSubmit={handleSubmit}>
                    <div className="grid gap-5 sm:grid-cols-2">
                        <label className="block text-sm font-medium text-slate-700">
                            First Name
                            <input
                                className="mt-2 w-full rounded-3xl border border-slate-300 bg-slate-50 px-4 py-3 text-slate-900 outline-none transition focus:border-teal-500 focus:ring-2 focus:ring-teal-100"
                                placeholder="First Name"
                                value={formData.firstName}
                                onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                            />
                        </label>

                        <label className="block text-sm font-medium text-slate-700">
                            Last Name
                            <input
                                className="mt-2 w-full rounded-3xl border border-slate-300 bg-slate-50 px-4 py-3 text-slate-900 outline-none transition focus:border-teal-500 focus:ring-2 focus:ring-teal-100"
                                placeholder="Last Name"
                                value={formData.lastName}
                                onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                            />
                        </label>
                    </div>

                    <label className="block text-sm font-medium text-slate-700">
                        Email address
                        <input
                            className="mt-2 w-full rounded-3xl border border-slate-300 bg-slate-50 px-4 py-3 text-slate-900 outline-none transition focus:border-teal-500 focus:ring-2 focus:ring-teal-100"
                            placeholder="Email"
                            type="email"
                            value={formData.email}
                            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                        />
                    </label>

                    <label className="block text-sm font-medium text-slate-700">
                        Password
                        <input
                            className="mt-2 w-full rounded-3xl border border-slate-300 bg-slate-50 px-4 py-3 text-slate-900 outline-none transition focus:border-teal-500 focus:ring-2 focus:ring-teal-100"
                            type="password"
                            placeholder="Password"
                            value={formData.password}
                            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                        />
                    </label>

                    <label className="block text-sm font-medium text-slate-700">
                        Role
                        <select
                            className="mt-2 w-full rounded-3xl border border-slate-300 bg-slate-50 px-4 py-3 text-slate-900 outline-none transition focus:border-teal-500 focus:ring-2 focus:ring-teal-100"
                            value={formData.role}
                            onChange={(e) => setFormData({ ...formData, role: e.target.value })}
                        >
                            <option value="CUSTOMER">CUSTOMER</option>
                            <option value="RESTAURANT_ADMIN">RESTAURANT_ADMIN</option>
                        </select>
                    </label>

                    <button
                        type="submit"
                        className="inline-flex w-full justify-center rounded-3xl bg-teal-600 px-5 py-3 text-sm font-semibold text-white shadow-lg shadow-teal-500/15 transition hover:bg-teal-700 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-teal-600"
                    >
                        Register
                    </button>
                </form>
            </div>
        </div>
    );
}

export default RegisterPage;