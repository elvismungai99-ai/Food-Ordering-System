import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { register } from "../../services/AuthService";

function RegisterPage() {
    const [formData, setFormData] = useState({
        firstName: "",
        lastName: "",
        email: "",
        password: "",
        phoneNumber: "",
        role: "CUSTOMER"
    });
    const [errorMessage, setErrorMessage] = useState("");
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMessage("");

        try {
            const data = await register(formData);
            const role = data.role || data.user?.role || formData.role;

            if (role === "RESTAURANT_ADMIN") {
                navigate("/restaurant/dashboard");
            } else {
                navigate("/customer/dashboard");
            }
        } catch (error) {
            console.error(error);
            setErrorMessage(error.response?.data?.message || "Registration failed. Please verify your information and try again.");
        }
    };

    return (
        <div className="relative min-h-screen overflow-hidden bg-gradient-to-br from-[#FEF1C7] via-[#E3F1FF] to-white px-4 py-12 sm:px-6">
            <div className="pointer-events-none absolute left-0 top-16 h-60 w-60 rounded-full bg-[#FFDE79] opacity-45 blur-3xl" />
            <div className="pointer-events-none absolute right-0 bottom-12 h-64 w-64 rounded-full bg-[#8AC8FF] opacity-30 blur-3xl" />

            <div className="relative mx-auto w-full max-w-xl rounded-[40px] border border-white/80 bg-white/95 p-8 shadow-[0_30px_90px_rgba(24,74,148,0.14)] backdrop-blur-xl">
                <div className="text-center">
                    <div className="inline-flex rounded-full bg-[#EAF6FF] px-4 py-1 text-xs font-semibold uppercase tracking-[0.3em] text-[#145A97]">
                        Create your account
                    </div>
                    <h1 className="mt-5 text-3xl font-semibold tracking-tight text-slate-950 sm:text-4xl">
                        Register and start ordering today
                    </h1>
                    <p className="mt-3 text-sm leading-6 text-slate-600">
                        Create an account as a customer or restaurant admin and enjoy easy ordering and management.
                    </p>
                </div>

                {errorMessage && (
                    <div className="mt-4 rounded-3xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                        {errorMessage}
                    </div>
                )}

                <form className="mt-8 grid gap-5" onSubmit={handleSubmit}>
                    <div className="grid gap-5 sm:grid-cols-2">
                        <label className="block text-sm font-medium text-slate-700">
                            First Name
                            <input
                                className="mt-3 w-full rounded-3xl border border-[#D6E8FF] bg-[#F7FBFF] px-4 py-3 text-slate-900 outline-none transition focus:border-[#1E70FF] focus:ring-2 focus:ring-[#B6D8FF]"
                                placeholder="First Name"
                                value={formData.firstName}
                                onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                            />
                        </label>

                        <label className="block text-sm font-medium text-slate-700">
                            Last Name
                            <input
                                className="mt-3 w-full rounded-3xl border border-[#D6E8FF] bg-[#F7FBFF] px-4 py-3 text-slate-900 outline-none transition focus:border-[#1E70FF] focus:ring-2 focus:ring-[#B6D8FF]"
                                placeholder="Last Name"
                                value={formData.lastName}
                                onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                            />
                        </label>
                    </div>

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

                    <label phoneNumber="block text-sm font-medium text-slate-700">
                        Phone Number
                        <input
                            className="mt-3 w-full rounded-3xl border border-[#D6E8FF] bg-[#F7FBFF] px-4 py-3 text-slate-900 outline-none transition focus:border-[#1E70FF] focus:ring-2 focus:ring-[#B6D8FF]"
                            placeholder="Phone Number"
                            value={formData.phoneNumber}
                            onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                        />
                    </label>

                    <label className="block text-sm font-medium text-slate-700">
                        Role
                        <select
                            className="mt-3 w-full rounded-3xl border border-[#D6E8FF] bg-[#F7FBFF] px-4 py-3 text-slate-900 outline-none transition focus:border-[#1E70FF] focus:ring-2 focus:ring-[#B6D8FF]"
                            value={formData.role}
                            onChange={(e) => setFormData({ ...formData, role: e.target.value })}
                        >
                            <option value="CUSTOMER">Customer</option>
                            <option value="RESTAURANT_ADMIN">Restaurant Admin</option>
                        </select>
                    </label>

                    <button
                        type="submit"
                        className="inline-flex w-full justify-center rounded-3xl bg-[#FFCE4B] px-5 py-3 text-sm font-semibold text-slate-950 shadow-lg shadow-[#FFCE4B]/25 transition hover:bg-[#f2b80f] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#FFCE4B]"
                    >
                        Register
                    </button>
                </form>

                <div className="mt-7 text-center text-sm text-slate-600">
                    <p>
                        Already have an account? <Link to="/login" className="font-semibold text-[#1E70FF] hover:text-[#125fd1]">Login</Link>
                    </p>
                    <p className="mt-3">
                        <Link to="/" className="font-semibold text-[#1E70FF] hover:text-[#125fd1]">Back to Home</Link>
                    </p>
                </div>
            </div>
        </div>
    );
}

export default RegisterPage;
