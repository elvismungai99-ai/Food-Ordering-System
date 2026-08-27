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

export interface AuthResponse {
  token: string;
  refreshToken?: string;
  tokenType?: string;
  expiresIn?: number;
  userId: string;
  role: string;
  firstName: string;
}

export interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

interface PasswordResetResponse {
  message: string;
}

export const register = async (userData: RegisterData): Promise<AuthResponse> => {
  const data = await requestData(
    () => api.post<AuthResponse>("/auth/register", userData),
    "Unable to register your account."
  );

  if (data.token) {
    localStorage.setItem("token", data.token);
    localStorage.setItem("userId", data.userId);
    localStorage.setItem("role", data.role);
    localStorage.setItem("firstName", data.firstName);
    if (data.refreshToken) {
      localStorage.setItem("refreshToken", data.refreshToken);
    }
  }

  return data;
};

export const login = async (credentials: LoginData): Promise<AuthResponse> => {
  localStorage.removeItem("token");
  localStorage.removeItem("refreshToken");
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
  if (data.refreshToken) {
    localStorage.setItem("refreshToken", data.refreshToken);
  }

  return data;
};

export const refreshAuthToken = async (): Promise<RefreshTokenResponse> => {
  const currentRefreshToken = localStorage.getItem("refreshToken");
  if (!currentRefreshToken) {
    throw new Error("No refresh token available");
  }

  const response = await api.post<RefreshTokenResponse>("/auth/refresh", {
    refreshToken: currentRefreshToken,
  });

  const { accessToken, refreshToken: newRefreshToken } = response.data;
  localStorage.setItem("token", accessToken);
  if (newRefreshToken) {
    localStorage.setItem("refreshToken", newRefreshToken);
  }

  return response.data;
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

export const logout = async (): Promise<void> => {
  const refreshToken = localStorage.getItem("refreshToken");
  try {
    if (refreshToken) {
      await api.post("/auth/logout", { refreshToken });
    }
  } catch (ignored) {
    // Graceful logout even if network/backend is unavailable
  } finally {
    localStorage.removeItem("token");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("userId");
    localStorage.removeItem("role");
    localStorage.removeItem("firstName");
    localStorage.removeItem("restaurantId");
  }
};

export const getToken = (): string | null => {
  return localStorage.getItem("token");
};

export const getRefreshToken = (): string | null => {
  return localStorage.getItem("refreshToken");
};

export const getRole = (): string | null => {
  return localStorage.getItem("role");
};

export const isAuthenticated = (): boolean => {
  return !!localStorage.getItem("token");
};
