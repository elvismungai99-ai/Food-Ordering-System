import axios from "axios";
import type {
  InternalAxiosRequestConfig,
} from "axios";

interface AuthRequestConfig
  extends InternalAxiosRequestConfig {
  sentAuthToken?: string;
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
    "Content-Type":
      "application/json",
  },
});

/**
 * Decodes a JWT's payload without verifying the signature
 * (verification is the backend's job) purely so the frontend
 * can tell "expired" apart from "invalid for some other reason".
 */
function isTokenExpired(
  token: string
): boolean {
  try {
    const payloadBase64 =
      token.split(".")[1];

    if (!payloadBase64) {
      return true;
    }

    const payload = JSON.parse(
      atob(
        payloadBase64
          .replace(/-/g, "+")
          .replace(/_/g, "/")
      )
    );

    if (
      typeof payload.exp
        !== "number"
    ) {
      // No exp claim — treat as
      // expired to be safe.
      return true;
    }

    const nowInSeconds =
      Date.now() / 1000;

    return payload.exp
      < nowInSeconds;
  } catch {
    // Malformed token — treat
    // as expired.
    return true;
  }
}

api.interceptors.request.use(
  (config) => {

    const url =
      config.url ?? "";

    const isPublicAuthRequest =
      url.includes(
        "/auth/login"
      )
      || url.includes(
        "/auth/register"
      )
      || url.includes(
        "/auth/forgot-password"
      )
      || url.includes(
        "/auth/reset-password"
      );

    /*
     * Do not attach old JWTs to
     * login/register requests.
     */
    if (
      !isPublicAuthRequest
    ) {

      const token =
        localStorage.getItem(
          "token"
        );

      if (token) {

        config.headers.Authorization =
          `Bearer ${token}`;

        (
          config as AuthRequestConfig
        ).sentAuthToken =
          token;
      }
    }

    return config;
  },

  (error) =>
    Promise.reject(error)
);

function clearSessionAndRedirect() {
  localStorage.removeItem("token");
  localStorage.removeItem("userId");
  localStorage.removeItem("role");
  localStorage.removeItem("firstName");
  localStorage.removeItem("restaurantId");

  if (
    window.location.pathname !== "/login"
  ) {
    window.location.assign(
      "/login?reason=session_expired"
    );
  }
}

api.interceptors.response.use(
  (response) => response,

  (error) => {
    if (error.response?.status === 401) {
      const url = error.config?.url ?? "";
      const isPublicAuthRequest =
        url.includes("/auth/login")
        || url.includes("/auth/register")
        || url.includes("/auth/forgot-password")
        || url.includes("/auth/reset-password");

      // For public auth endpoints, let the component handle the error message (e.g. Invalid credentials)
      if (!isPublicAuthRequest) {
        clearSessionAndRedirect();
      }
    }

    return Promise.reject(error);
  }
);

export default api;