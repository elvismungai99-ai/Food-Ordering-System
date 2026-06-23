import { Link } from "react-router-dom";

function HomePage() {
  return (
    <div className="min-h-screen bg-slate-100 px-4 py-12 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-5xl rounded-[40px] border border-slate-200 bg-white/95 p-10 shadow-[0_24px_80px_rgba(15,23,42,0.08)] backdrop-blur-xl">
        <div className="grid gap-10 lg:grid-cols-[1.1fr_0.9fr] lg:items-center">
          <section>
            <span className="inline-flex rounded-full bg-indigo-100 px-4 py-1 text-sm font-semibold text-indigo-700">
              Food Ordering System
            </span>
            <h1 className="mt-6 text-4xl font-semibold tracking-tight text-slate-950 sm:text-5xl">
              Order food fast, simple, and secure.
            </h1>
            <p className="mt-5 max-w-2xl text-base leading-7 text-slate-600 sm:text-lg">
              Browse restaurants, place orders, and manage your profile from one responsive app. Log in to order food or register to create a customer or restaurant admin account.
            </p>
            <div className="mt-10 flex flex-col gap-4 sm:flex-row">
              <Link
                to="/login"
                className="inline-flex items-center justify-center rounded-3xl bg-indigo-600 px-6 py-3 text-sm font-semibold text-white transition hover:bg-indigo-700"
              >
                Login
              </Link>
              <Link
                to="/register"
                className="inline-flex items-center justify-center rounded-3xl border border-slate-300 bg-white px-6 py-3 text-sm font-semibold text-slate-900 transition hover:border-slate-400 hover:bg-slate-50"
              >
                Register
              </Link>
            </div>
          </section>

          <aside className="rounded-[32px] bg-slate-50 p-8 shadow-inner shadow-slate-200/60">
            <div className="space-y-6">
              <div className="rounded-3xl bg-white p-6 shadow-sm shadow-slate-200/80">
                <p className="text-sm font-medium uppercase tracking-[0.25em] text-teal-600">Fast delivery</p>
                <p className="mt-3 text-lg font-semibold text-slate-950">Choose your favorite restaurants and get your meal delivered quickly.</p>
              </div>
              <div className="rounded-3xl bg-white p-6 shadow-sm shadow-slate-200/80">
                <p className="text-sm font-medium uppercase tracking-[0.25em] text-indigo-600">Secure checkout</p>
                <p className="mt-3 text-lg font-semibold text-slate-950">Save your details and pay securely with one-click checkout.</p>
              </div>
              <div className="rounded-3xl bg-white p-6 shadow-sm shadow-slate-200/80">
                <p className="text-sm font-medium uppercase tracking-[0.25em] text-slate-600">Restaurant tools</p>
                <p className="mt-3 text-lg font-semibold text-slate-950">Manage menu items, orders, and customer requests in one dashboard.</p>
              </div>
            </div>
          </aside>
        </div>
      </div>
    </div>
  );
}

export default HomePage;