import api from "../api/axios"; 

interface LoginData {
  email: string;
  password: string;
}

interface RegisterData {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  role: string;
}

interface AuthResponse {
  data: any;
  token: string;
  userId: number;
  role: string;
  firstName: string;
}

export const login = async (loginData: LoginData): Promise<AuthResponse> => {
  const response = await api.post("/auth/login", loginData);
  const data: AuthResponse = response.data;

  localStorage.setItem("token", data.token);
  localStorage.setItem("userId", String(data.userId));
  localStorage.setItem("role", data.role);
  localStorage.setItem("firstName", data.firstName);

  return data;
};

export const register = async (registerData: RegisterData): Promise<void> => {
  const response = await api.post("/auth/register", registerData);
  return response.data;
};

export const logout = () => {
  localStorage.removeItem("token");
  localStorage.removeItem("userId");
  localStorage.removeItem("role");
  localStorage.removeItem("firstName");
};

export const getToken = (): string | null => {
  return localStorage.getItem("token");
};

export const getRole = (): string | null => {
  return localStorage.getItem("role");
};

export const isAuthenticated = (): boolean => {
  return !!localStorage.getItem("token");
};