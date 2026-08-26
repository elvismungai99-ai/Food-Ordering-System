import { useState } from "react";
import { Link } from "react-router-dom";
import heroImage from "../../../assets/hero.png";
import { clearAuthSession, getActiveAuthSession } from "../../../utils/auth";

const categories = [
  "Pizza",
  "Burgers",
  "Chicken",
  "Local meals",
  "Drinks",
  "Desserts",
];

const steps = [
  {
    title: "Set your location",
    text: "Start with your delivery address so nearby restaurants can serve you faster.",
  },
  {
    title: "Choose your meal",
    text: "Browse menus, compare ratings, and add your favorite food to the cart.",
  },
  {
    title: "Track the order",
    text: "Follow the restaurant and rider progress from checkout to delivery.",
  },
];

const roleLinks = [
  {
    title: "Order as a customer",
    text: "Find restaurants, checkout, and review your delivered meals.",
    href: "/register",
    action: "Create account",
  },
  {
    title: "Run a restaurant",
    text: "Manage your profile, menu, orders, and sales analytics.",
    href: "/register",
    action: "Register restaurant",
  },
  {
    title: "Deliver as a rider",
    text: "Go online, receive delivery requests, and track your earnings.",
    href: "/rider/register",
    action: "Become a rider",
  },
];

function HomePage() {
  const [session, setSession] = useState(() => getActiveAuthSession());

  const { token, role, firstName } = session;

  const handleLogout = () => {
    clearAuthSession();
    setSession({
      token: null,
      role: null,
      firstName: null,
      userId: null,
    });
  };

  const dashboardPath =
    role === "CUSTOMER"
      ? "/customer/dashboard"
      : role === "OWNER"
        ? "/restaurant/dashboard"
        : role === "RIDER"
          ? "/rider/dashboard"
          : role === "SUPER_ADMIN"
            ? "/admin/dashboard"
            : "/login";

  const browsePath =
    role === "CUSTOMER"
      ? "/customer/restaurants"
      : dashboardPath;

  const roleLabel =
    role === "CUSTOMER"
      ? "Customer"
      : role === "OWNER"
        ? "Restaurant"
        : role === "RIDER"
          ? "Rider"
          : role === "SUPER_ADMIN"
            ? "Admin"
            : "";

  return (
    <div className="food-page text-slate-950">
      <header className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-5 sm:px-6 lg:px-8">
        <Link
          to="/"
          className="text-xl font-black text-emerald-700"
        >
          Food Ordering System
        </Link>

        <nav className="flex items-center gap-2 sm:gap-3">
          <Link
            to="/rider/register"
            className="food-button-secondary hidden px-4 py-2 text-sm sm:inline-flex"
          >
            Become a rider
          </Link>
          {token ? (
            <div className="flex items-center gap-2">
              <span className="hidden rounded-full bg-emerald-100 px-3 py-1 text-xs font-bold text-emerald-800 sm:inline-block">
                {firstName ? `${firstName} (${roleLabel})` : roleLabel}
              </span>
              <Link
                to={dashboardPath}
                className="food-button-primary px-5 py-2 text-sm"
              >
                Dashboard
              </Link>
              <button
                type="button"
                onClick={handleLogout}
                className="food-button-secondary px-4 py-2 text-sm"
              >
                Log out
              </button>
            </div>
          ) : (
            <>
              <Link
                to="/login"
                className="food-button-secondary px-4 py-2 text-sm"
              >
                Login
              </Link>
              <Link
                to="/register"
                className="food-button-primary px-5 py-2 text-sm"
              >
                Register
              </Link>
            </>
          )}
        </nav>
      </header>

      <main>
        <section className="mx-auto grid max-w-7xl gap-10 px-4 pb-12 pt-5 sm:px-6 lg:grid-cols-[1.02fr_0.98fr] lg:items-center lg:px-8 lg:pb-16">
          <div>
            <p className="text-sm font-bold uppercase text-emerald-700">
              Fast meals from nearby restaurants
            </p>
            <h1 className="mt-4 max-w-3xl text-5xl font-black leading-tight text-slate-950 sm:text-6xl">
              Food you love, delivered when you need it.
            </h1>
            <p className="mt-5 max-w-2xl text-lg leading-8 text-slate-600">
              Discover restaurants, place secure orders, and follow delivery progress from one simple food ordering platform.
            </p>

            <div className="food-card mt-8 max-w-2xl p-3">
              <div className="flex flex-col gap-3 sm:flex-row">
                <label className="sr-only" htmlFor="delivery-location">
                  Delivery address
                </label>
                <input
                  id="delivery-location"
                  type="text"
                  placeholder="Enter your delivery address"
                  className="food-input min-h-12 flex-1 px-4 text-sm font-medium"
                />
                <button
                  type="button"
                  className="food-button-secondary min-h-12 px-4 text-sm"
                >
                  Use location
                </button>
                <Link
                  to={token ? browsePath : "/login"}
                  className="food-button-primary inline-flex min-h-12 items-center justify-center px-5 text-sm"
                >
                  Find food
                </Link>
              </div>
            </div>

            <div className="mt-8 grid max-w-2xl grid-cols-3 gap-4">
              <div>
                <p className="text-2xl font-black text-slate-950">30 min</p>
                <p className="text-sm text-slate-500">Average delivery</p>
              </div>
              <div>
                <p className="text-2xl font-black text-slate-950">4 roles</p>
                <p className="text-sm text-slate-500">Customer to admin</p>
              </div>
              <div>
                <p className="text-2xl font-black text-slate-950">Live</p>
                <p className="text-sm text-slate-500">Rider updates</p>
              </div>
            </div>
          </div>

          <div className="relative overflow-hidden rounded-[32px] bg-emerald-900 shadow-2xl shadow-emerald-900/20 ring-1 ring-emerald-200/40">
            <img
              src={heroImage}
              alt="Prepared food ready for delivery"
              className="h-[420px] w-full object-cover sm:h-[520px]"
            />
            <div className="absolute inset-x-5 bottom-5 rounded-2xl bg-white/95 p-5 shadow-lg backdrop-blur">
              <p className="text-sm font-bold text-emerald-700">
                Today&apos;s flow
              </p>
              <div className="mt-3 grid grid-cols-3 gap-3 text-center">
                {["Order", "Prepare", "Deliver"].map((item) => (
                  <div
                    key={item}
                className="rounded-xl bg-emerald-50 px-3 py-3 text-sm font-bold text-emerald-900"
                  >
                    {item}
                  </div>
                ))}
              </div>
            </div>
          </div>
        </section>

        <section className="border-y border-emerald-100 bg-white/86 py-8 backdrop-blur">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <div className="flex flex-wrap items-center justify-between gap-4">
              <h2 className="text-2xl font-black text-slate-950">
                Browse popular categories
              </h2>
              <Link
                to={token ? browsePath : "/login"}
                className="text-sm font-bold text-emerald-700 hover:text-emerald-800"
              >
                View restaurants
              </Link>
            </div>
            <div className="mt-6 grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6">
              {categories.map((category) => (
                <Link
                  key={category}
                  to={token ? browsePath : "/login"}
                  className="food-card food-card-hover px-4 py-5 text-center text-sm font-bold text-slate-800"
                >
                  {category}
                </Link>
              ))}
            </div>
          </div>
        </section>

        <section className="mx-auto max-w-7xl px-4 py-14 sm:px-6 lg:px-8">
          <div className="grid gap-4 md:grid-cols-3">
            {steps.map((step, index) => (
              <article
                key={step.title}
                className="food-card food-card-hover p-6"
              >
                <span className="inline-flex h-10 w-10 items-center justify-center rounded-full bg-emerald-100 text-sm font-black text-emerald-700">
                  {index + 1}
                </span>
                <h3 className="mt-5 text-xl font-black text-slate-950">
                  {step.title}
                </h3>
                <p className="mt-3 text-sm leading-6 text-slate-600">
                  {step.text}
                </p>
              </article>
            ))}
          </div>
        </section>

        <section className="bg-slate-950 py-14 text-white">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <div className="flex flex-wrap items-end justify-between gap-4">
              <div>
                <p className="text-sm font-bold uppercase text-emerald-300">
                  One platform, every workflow
                </p>
                <h2 className="mt-3 text-3xl font-black">
                  Built for customers, restaurants, riders, and admins.
                </h2>
              </div>
              <Link
                to={token ? dashboardPath : "/login"}
                className="rounded-full bg-white px-5 py-3 text-sm font-bold text-slate-950 hover:bg-emerald-100"
              >
                {token ? "Go to dashboard" : "Sign in"}
              </Link>
            </div>

            <div className="mt-8 grid gap-4 md:grid-cols-3">
              {roleLinks.map((role) => (
                <article
                  key={role.title}
                  className="rounded-2xl border border-white/10 bg-white/5 p-6 shadow-xl shadow-black/10"
                >
                  <h3 className="text-xl font-black">
                    {role.title}
                  </h3>
                  <p className="mt-3 min-h-16 text-sm leading-6 text-slate-300">
                    {role.text}
                  </p>
                  <Link
                    to={role.href}
                    className="mt-5 inline-flex rounded-full bg-emerald-500 px-4 py-2 text-sm font-bold text-slate-950 hover:bg-emerald-400"
                  >
                    {role.action}
                  </Link>
                </article>
              ))}
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}

export default HomePage;
