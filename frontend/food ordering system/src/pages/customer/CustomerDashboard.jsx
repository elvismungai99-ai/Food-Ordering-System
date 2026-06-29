import { useNavigate } from "react-router-dom";
import { getAuthRole, logout } from "../../services/AuthService";

function CustomerDashboard() {
  return (
    <div>
      <h1>Customer Dashboard</h1>
      <p>Welcome to the Food Ordering System.</p>
    </div>
  );
}

export default CustomerDashboard;
