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

      const isSameTokenStillActive =
        failedToken
        && failedToken ===
          localStorage.getItem("token");

      if (isSameTokenStillActive) {

        /*
         * An expired token is dead no
         * matter who it belongs to —
         * always clear it and send
         * the user back to login.
         * This is what was previously
         * missing: admin sessions used
         * to fail silently forever on
         * an expired token.
         */
        if (
          isTokenExpired(failedToken)
        ) {
          clearSessionAndRedirect();
          return Promise.reject(error);
        }

        /*
         * Token isn't expired but the
         * backend still rejected it
         * (e.g. a transient auth/role
         * check). For super admin
         * sessions on /admin/ routes,
         * give the request a pass
         * instead of forcing logout —
         * but only for this narrower,
         * non-expiry case.
         */
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

        clearSessionAndRedirect();
      }
    }

    return Promise.reject(error);
  }
);

export default api;