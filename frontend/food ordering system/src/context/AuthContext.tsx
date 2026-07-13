import { createContext, useContext } from "react";

interface AuthContextType {
  token: string | null;
  role: string | null;
  firstName: string | null;
}

const AuthContext = createContext<AuthContextType>({
  token: null,
  role: null,
  firstName: null,
});

export const useAuth = () => useContext(AuthContext);
export default AuthContext;
