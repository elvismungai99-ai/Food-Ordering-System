import { useNavigate } from "react-router-dom";

import CustomerHeader from "../../components/customer/CustomerHeader";

function CustomerProfilePage() {
  const navigate = useNavigate();

  const firstName =
    localStorage.getItem("firstName") || "Customer";

  const role =
    localStorage.getItem("role") || "CUSTOMER";

  const userId =
    localStorage.getItem("userId") || "Not available";

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("userId");
    localStorage.removeItem("firstName");

    navigate("/login");
  };

  return (
    <div className="min-h-screen bg-slate-100">
      <CustomerHeader />

      <main className="mx-auto max-w-4xl px-6 py-10">
        <div>
          <h1 className="text-3xl font-bold text-slate-950">
            Customer Profile
          </h1>

          <p className="mt-2 text-slate-500">
            Review your account information.
          </p>
        </div>

        <section className="mt-8 overflow-hidden rounded-[24px] border border-slate-200 bg-white shadow-sm">
          <div className="bg-slate-950 px-8 py-8 text-white">
            <div className="flex h-20 w-20 items-center justify-center rounded-full bg-indigo-600 text-3xl font-bold">
              {firstName.charAt(0).toUpperCase()}
            </div>

            <h2 className="mt-5 text-2xl font-semibold">
              {firstName}
            </h2>

            <p className="mt-1 text-slate-300">
              Food Ordering customer
            </p>
          </div>

          <div className="space-y-6 p-8">
            <div>
              <p className="text-sm font-medium text-slate-500">
                First name
              </p>

              <p className="mt-1 font-semibold text-slate-900">
                {firstName}
              </p>
            </div>

            <div>
              <p className="text-sm font-medium text-slate-500">
                Account role
              </p>

              <p className="mt-1 font-semibold text-slate-900">
                {role}
              </p>
            </div>

            <div>
              <p className="text-sm font-medium text-slate-500">
                User ID
              </p>

              <p className="mt-1 break-all font-mono text-sm text-slate-700">
                {userId}
              </p>
            </div>

            <div className="border-t border-slate-200 pt-6">
              <button
                type="button"
                onClick={handleLogout}
                className="rounded-3xl bg-red-600 px-6 py-3 text-sm font-semibold text-white transition hover:bg-red-700"
              >
                Log out
              </button>
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}

export default CustomerProfilePage;