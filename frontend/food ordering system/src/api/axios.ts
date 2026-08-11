import axios from "axios";
import type {
  InternalAxiosRequestConfig,
} from "axios";

interface AuthRequestConfig
  extends InternalAxiosRequestConfig {
  sentAuthToken?: string;
}

const api = axios.create({
  baseURL:
    import.meta.env.VITE_API_BASE_URL
    ?? "http://localhost:8080/api",

  headers: {
    "Content-Type":
      "application/json",
  },
});

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

api.interceptors.response.use(
  (response) =>
    response,

  (error) => {
    if (
      error.response?.status === 401
    ) {
      const failedToken =
        (
          error.config as
            | AuthRequestConfig
            | undefined
        )?.sentAuthToken;

      if (
        failedToken
        && failedToken ===
          localStorage.getItem("token")
      ) {
        const isAdminSession =
          localStorage.getItem("role")
            ?.replace(/^ROLE_/, "")
            .toUpperCase() === "SUPER_ADMIN";

        const isAdminRequest =
          typeof error.config?.url === "string"
          && error.config.url.includes("/admin/");

        if (
          isAdminSession
          && isAdminRequest
        ) {
          return Promise.reject(error);
        }

        localStorage.removeItem("token");
        localStorage.removeItem("userId");
        localStorage.removeItem("role");
        localStorage.removeItem("firstName");
        localStorage.removeItem("restaurantId");

        if (
          window.location.pathname !== "/login"
        ) {
          window.location.assign("/login");
        }
      }
    }

    return Promise.reject(error);
  }
);

export default api;
