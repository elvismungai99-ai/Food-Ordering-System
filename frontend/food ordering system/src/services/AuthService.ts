import api from "../api/axios";
import {
  requestData,
} from "./request";

interface LoginData {
  email: string;
  password: string;
}

interface RegisterData {
  fullName: string;
  email: string;
  phoneNumber: string;
  password: string;
  role: string;
}

interface AuthResponse {
  token: string;
  userId: string;
  role: string;
  firstName: string;
}

interface PasswordResetResponse {
  message: string;
}

export const register = async (userData: RegisterData): Promise<AuthResponse> => {
  return requestData(
    () => api.post<AuthResponse>("/auth/register", userData),
    "Unable to register your account."
  );
};

export const login = async (credentials: LoginData): Promise<AuthResponse> => {
  localStorage.removeItem("token");
  localStorage.removeItem("userId");
  localStorage.removeItem("role");
  localStorage.removeItem("firstName");
  localStorage.removeItem("restaurantId");

  const data = await requestData(
    () => api.post<AuthResponse>("/auth/login", credentials),
    "Unable to log in."
  );

  localStorage.setItem("token", data.token);
  localStorage.setItem("userId", data.userId);
  localStorage.setItem("role", data.role);
  localStorage.setItem("firstName", data.firstName);

  return data;
};

export const requestPasswordReset = async (
  email: string
): Promise<PasswordResetResponse> => {
  return requestData(
    () => api.post<PasswordResetResponse>(
      "/auth/forgot-password",
      {
        email,
      }
    ),
    "Unable to request a password reset."
  );
};

export const resetPassword = async (
  token: string,
  password: string
): Promise<PasswordResetResponse> => {
  return requestData(
    () => api.post<PasswordResetResponse>(
      "/auth/reset-password",
      {
        token,
        password,
      }
    ),
    "Unable to reset your password."
  );
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
