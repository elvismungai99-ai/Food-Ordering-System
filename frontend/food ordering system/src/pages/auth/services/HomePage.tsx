import { Link } from "react-router-dom";
import heroImage from "../../../assets/hero.png";

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
  const token =
    localStorage.getItem("token");

  const role =
    localStorage
      .getItem("role")
      ?.replace("ROLE_", "")
      .toUpperCase();

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

  return (
    <div className="min-h-screen bg-[#f7f8f4] text-slate-950">
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
            className="hidden rounded-full px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-white sm:inline-flex"
          >
            Become a rider
          </Link>
          {token ? (
            <Link
              to={dashboardPath}
              className="rounded-full bg-emerald-600 px-5 py-2 text-sm font-semibold text-white shadow-sm hover:bg-emerald-700"
            >
              Dashboard
            </Link>
          ) : (
            <>
              <Link
                to="/login"
                className="rounded-full px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-white"
              >
                Login
              </Link>
              <Link
                to="/register"
                className="rounded-full bg-emerald-600 px-5 py-2 text-sm font-semibold text-white shadow-sm hover:bg-emerald-700"
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

            <div className="mt-8 max-w-2xl rounded-2xl border border-slate-200 bg-white p-3 shadow-xl shadow-slate-200/70">
              <div className="flex flex-col gap-3 sm:flex-row">
                <label className="sr-only" htmlFor="delivery-location">
                  Delivery address
                </label>
                <input
                  id="delivery-location"
                  type="text"
                  placeholder="Enter your delivery address"
                  className="min-h-12 flex-1 rounded-xl border border-slate-200 px-4 text-sm font-medium text-slate-800 outline-none focus:border-emerald-500"
                />
                <button
                  type="button"
                  className="min-h-12 rounded-xl border border-slate-200 px-4 text-sm font-bold text-slate-700 hover:bg-slate-50"
                >
                  Use location
                </button>
                <Link
                  to={token ? browsePath : "/login"}
                  className="inline-flex min-h-12 items-center justify-center rounded-xl bg-emerald-600 px-5 text-sm font-bold text-white hover:bg-emerald-700"
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

          <div className="relative overflow-hidden rounded-[32px] bg-emerald-900 shadow-2xl shadow-emerald-900/20">
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
                    className="rounded-xl bg-slate-100 px-3 py-3 text-sm font-bold text-slate-800"
                  >
                    {item}
                  </div>
                ))}
              </div>
            </div>
          </div>
        </section>

        <section className="border-y border-slate-200 bg-white py-8">
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
                  className="rounded-2xl border border-slate-200 bg-[#f7f8f4] px-4 py-5 text-center text-sm font-bold text-slate-800 hover:border-emerald-300 hover:bg-emerald-50"
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
                className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm"
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
                  className="rounded-2xl border border-white/10 bg-white/5 p-6"
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
