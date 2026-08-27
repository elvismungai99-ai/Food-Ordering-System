import React, { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

import { useCart } from "../../context/CartContext";
import OfflineBanner from "../common/OfflineBanner";

interface CustomerHeaderProps {
  title?: string;
}

export const CustomerHeader: React.FC<CustomerHeaderProps> = ({
  title = "Food Ordering",
}) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { totalItems, loading } = useCart();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const token = localStorage.getItem("token");
  const firstName = localStorage.getItem("firstName");

  const navItems = [
    { label: "Home", path: "/" },
    { label: "Restaurants", path: "/customer/restaurants" },
    { label: "Orders", path: "/customer/orders" },
    { label: "Profile", path: "/customer/profile" },
  ];

  const handleLogout = () => {
    localStorage.clear();
    sessionStorage.clear();
    setMobileMenuOpen(false);
    navigate("/login");
  };

  const handleNavigate = (path: string) => {
    setMobileMenuOpen(false);
    navigate(path);
  };

  const isActivePath = (path: string) => {
    if (path === "/" && location.pathname === "/") return true;
    if (path !== "/" && location.pathname.startsWith(path)) return true;
    return false;
  };

  return (
    <>
      <OfflineBanner />

      <header className="sticky top-0 z-40 border-b border-slate-200/80 bg-white/95 shadow-sm shadow-slate-200/50 backdrop-blur-xl">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 sm:px-6 py-3.5">
          {/* Brand logo / home link */}
          <button
            type="button"
            onClick={() => handleNavigate("/customer/dashboard")}
            aria-label="Go to customer dashboard"
            className="flex items-center gap-2 text-xl font-black tracking-tight text-emerald-700 hover:opacity-90 transition"
          >
            <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-emerald-600 text-white font-black text-base shadow-sm">
              FO
            </span>
            <span>{title}</span>
          </button>

          {/* Desktop Navigation */}
          <nav
            aria-label="Desktop navigation"
            className="hidden md:flex items-center gap-1.5 lg:gap-2"
          >
            {navItems.map((item) => {
              const active = isActivePath(item.path);
              return (
                <button
                  key={item.path}
                  type="button"
                  onClick={() => handleNavigate(item.path)}
                  className={`rounded-2xl px-3.5 py-2 text-sm font-semibold transition ${
                    active
                      ? "bg-emerald-50 text-emerald-800 font-bold"
                      : "text-slate-600 hover:bg-slate-50 hover:text-slate-900"
                  }`}
                >
                  {item.label}
                </button>
              );
            })}

            {/* Cart Button with Count Badge */}
            <button
              type="button"
              onClick={() => handleNavigate("/customer/cart")}
              aria-label={`Shopping cart with ${totalItems} items`}
              className={`flex items-center gap-2 rounded-2xl px-4 py-2 text-sm font-bold transition shadow-sm ${
                isActivePath("/customer/cart")
                  ? "bg-emerald-700 text-white shadow-emerald-600/30"
                  : "bg-emerald-600 text-white hover:bg-emerald-700"
              }`}
            >
              <svg
                className="h-4 w-4"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                aria-hidden="true"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"
                />
              </svg>
              <span>Cart</span>
              <span className="flex min-h-5 min-w-5 items-center justify-center rounded-full bg-white px-1 text-[11px] font-black text-emerald-700">
                {loading ? "…" : totalItems}
              </span>
            </button>

            {/* User Greeting / Logout */}
            {token ? (
              <button
                type="button"
                onClick={handleLogout}
                className="ml-2 rounded-2xl border border-slate-200 px-3.5 py-2 text-xs font-semibold text-slate-600 hover:border-red-200 hover:bg-red-50 hover:text-red-700 transition"
              >
                Log out {firstName ? `(${firstName})` : ""}
              </button>
            ) : (
              <button
                type="button"
                onClick={() => handleNavigate("/login")}
                className="ml-2 rounded-2xl border border-slate-300 px-4 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition"
              >
                Log in
              </button>
            )}
          </nav>

          {/* Mobile Right Controls: Cart Quick Button & Hamburger Toggle */}
          <div className="flex md:hidden items-center gap-2">
            <button
              type="button"
              onClick={() => handleNavigate("/customer/cart")}
              aria-label={`Cart: ${totalItems} items`}
              className="relative flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-50 text-emerald-700 transition"
            >
              <svg
                className="h-5 w-5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                aria-hidden="true"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"
                />
              </svg>
              {totalItems > 0 && (
                <span className="absolute -top-1 -right-1 flex h-5 w-5 items-center justify-center rounded-full bg-emerald-600 text-[10px] font-bold text-white shadow-sm">
                  {totalItems}
                </span>
              )}
            </button>

            <button
              type="button"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              aria-expanded={mobileMenuOpen}
              aria-label={mobileMenuOpen ? "Close navigation menu" : "Open navigation menu"}
              className="flex h-10 w-10 items-center justify-center rounded-xl border border-slate-200 bg-white text-slate-700 hover:bg-slate-50 transition"
            >
              {mobileMenuOpen ? (
                <svg
                  className="h-5 w-5"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="2"
                    d="M6 18L18 6M6 6l12 12"
                  />
                </svg>
              ) : (
                <svg
                  className="h-5 w-5"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="2"
                    d="M4 6h16M4 12h16M4 18h16"
                  />
                </svg>
              )}
            </button>
          </div>
        </div>

        {/* Mobile Navigation Slide-down / Drawer */}
        {mobileMenuOpen && (
          <div
            className="md:hidden border-t border-slate-100 bg-white/98 px-5 py-4 shadow-xl animate-fadeIn"
            role="navigation"
            aria-label="Mobile navigation"
          >
            <div className="space-y-1.5">
              {navItems.map((item) => {
                const active = isActivePath(item.path);
                return (
                  <button
                    key={item.path}
                    type="button"
                    onClick={() => handleNavigate(item.path)}
                    className={`w-full rounded-xl px-4 py-3 text-left text-sm font-semibold transition flex items-center justify-between ${
                      active
                        ? "bg-emerald-50 text-emerald-900 font-bold border-l-4 border-emerald-600"
                        : "text-slate-700 hover:bg-slate-50"
                    }`}
                  >
                    <span>{item.label}</span>
                    {active && (
                      <span className="text-xs font-bold text-emerald-600">Active</span>
                    )}
                  </button>
                );
              })}

              <button
                type="button"
                onClick={() => handleNavigate("/customer/cart")}
                className={`w-full rounded-xl px-4 py-3 text-left text-sm font-semibold transition flex items-center justify-between ${
                  isActivePath("/customer/cart")
                    ? "bg-emerald-50 text-emerald-900 font-bold border-l-4 border-emerald-600"
                    : "text-slate-700 hover:bg-slate-50"
                }`}
              >
                <span>Shopping Cart</span>
                <span className="rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-bold text-emerald-800">
                  {totalItems} items
                </span>
              </button>
            </div>

            <div className="mt-4 pt-4 border-t border-slate-100 flex items-center justify-between">
              {token ? (
                <>
                  <span className="text-xs text-slate-500 font-medium">
                    Signed in as <strong className="text-slate-800">{firstName || "Customer"}</strong>
                  </span>
                  <button
                    type="button"
                    onClick={handleLogout}
                    className="rounded-xl border border-red-200 bg-red-50 px-4 py-2 text-xs font-bold text-red-700 hover:bg-red-100 transition"
                  >
                    Log out
                  </button>
                </>
              ) : (
                <button
                  type="button"
                  onClick={() => handleNavigate("/login")}
                  className="w-full rounded-xl bg-emerald-600 py-2.5 text-center text-xs font-bold text-white hover:bg-emerald-700"
                >
                  Log In to Your Account
                </button>
              )}
            </div>
          </div>
        )}
      </header>
    </>
  );
};

export default CustomerHeader;
