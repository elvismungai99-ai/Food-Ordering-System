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
  token: string;
  userId: string;
  role: string;
  firstName: string;
}

export const register = async (userData: RegisterData): Promise<void> => {
  await api.post("/auth/register", userData);
};

export const login = async (credentials: LoginData): Promise<AuthResponse> => {
  const response = await api.post<AuthResponse>("/auth/login", credentials);
  const data = response.data;

  localStorage.setItem("token", data.token);
  localStorage.setItem("userId", data.userId);
  localStorage.setItem("role", data.role);
  localStorage.setItem("firstName", data.firstName);

  return data;
};

export const logout = (): void => {
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