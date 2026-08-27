import axios from "axios";
import type { InternalAxiosRequestConfig } from "axios";

interface AuthRequestConfig extends InternalAxiosRequestConfig {
  sentAuthToken?: string;
  _retry?: boolean;
}

function getBaseUrl(): string {
  const envUrl = (import.meta.env.VITE_API_BASE_URL as string | undefined)?.trim();
  if (!envUrl) {
    return "http://localhost:8080/api";
  }

  // Remove trailing slashes
  const cleanUrl = envUrl.replace(/\/+$/, "");

  // If already ends with /api, use it; otherwise append /api
  if (cleanUrl.endsWith("/api")) {
    return cleanUrl;
  }

  return `${cleanUrl}/api`;
}

const api = axios.create({
  baseURL: getBaseUrl(),
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use(
  (config) => {
    const url = config.url ?? "";
    const isPublicAuthRequest =
      url.includes("/auth/login")
      || url.includes("/auth/register")
      || url.includes("/auth/forgot-password")
      || url.includes("/auth/reset-password")
      || url.includes("/auth/refresh");

    /*
     * Do not attach old JWTs to public auth endpoints.
     */
    if (!isPublicAuthRequest) {
      const token = localStorage.getItem("token");
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
        (config as AuthRequestConfig).sentAuthToken = token;
      }
    }

    return config;
  },
  (error) => Promise.reject(error)
);

function clearSessionAndRedirect() {
  localStorage.removeItem("token");
  localStorage.removeItem("refreshToken");
  localStorage.removeItem("userId");
  localStorage.removeItem("role");
  localStorage.removeItem("firstName");
  localStorage.removeItem("restaurantId");

  if (window.location.pathname !== "/login") {
    window.location.assign("/login?reason=session_expired");
  }
}

let isRefreshing = false;
let failedQueue: Array<{
  resolve: (token: string) => void;
  reject: (error: unknown) => void;
}> = [];

const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else if (token) {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config as AuthRequestConfig;

    // Handle 401 Unauthorized by attempting automatic refresh token rotation
    if (error.response?.status === 401 && originalRequest && !originalRequest._retry) {
      const url = originalRequest.url ?? "";
      const isPublicAuthRequest =
        url.includes("/auth/login")
        || url.includes("/auth/register")
        || url.includes("/auth/forgot-password")
        || url.includes("/auth/reset-password")
        || url.includes("/auth/refresh");

      if (isPublicAuthRequest) {
        return Promise.reject(error);
      }

      const currentRefreshToken = localStorage.getItem("refreshToken");
      if (!currentRefreshToken) {
        clearSessionAndRedirect();
        return Promise.reject(error);
      }

      if (isRefreshing) {
        return new Promise<string>((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return api(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const response = await axios.post(`${getBaseUrl()}/auth/refresh`, {
          refreshToken: currentRefreshToken,
        });

        const { accessToken, refreshToken: newRefreshToken } = response.data;
        localStorage.setItem("token", accessToken);
        if (newRefreshToken) {
          localStorage.setItem("refreshToken", newRefreshToken);
        }

        processQueue(null, accessToken);
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        clearSessionAndRedirect();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    if (error.response?.status === 403 && error.response?.data?.message?.includes("disabled")) {
      clearSessionAndRedirect();
    }

    return Promise.reject(error);
  }
);

export default api;