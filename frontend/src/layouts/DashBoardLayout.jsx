import { Outlet } from "react-router-dom";

function DashboardLayout() {
  return (
    <div>

      <header>
        Food Ordering System
      </header>

      <div>

        <aside>

          Sidebar

        </aside>

        <main>

          <Outlet />

        </main>

      </div>

    </div>
  );
}

export default DashboardLayout;