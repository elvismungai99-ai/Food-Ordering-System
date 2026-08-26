/**
 * Decodes a JWT's payload without verifying signature to check expiration.
 */
export function isTokenExpired(token: string | null): boolean {
  if (!token) {
    return true;
  }

  try {
    const payloadBase64 = token.split(".")[1];
    if (!payloadBase64) {
      return true;
    }

    const payload = JSON.parse(
      atob(payloadBase64.replace(/-/g, "+").replace(/_/g, "/"))
    );

    if (typeof payload.exp !== "number") {
      return true;
    }

    const nowInSeconds = Date.now() / 1000;
    return payload.exp < nowInSeconds;
  } catch {
    return true;
  }
}

/**
 * Clears all authentication-related keys from localStorage.
 */
export function clearAuthSession(): void {
  localStorage.removeItem("token");
  localStorage.removeItem("userId");
  localStorage.removeItem("role");
  localStorage.removeItem("firstName");
  localStorage.removeItem("restaurantId");
}

/**
 * Returns active authentication info, or null values if no valid session exists.
 * Automatically clears stale/expired tokens.
 */
export function getActiveAuthSession(): {
  token: string | null;
  role: string | null;
  firstName: string | null;
  userId: string | null;
} {
  const token = localStorage.getItem("token");

  if (!token || isTokenExpired(token)) {
    if (token) {
      clearAuthSession();
    }
    return {
      token: null,
      role: null,
      firstName: null,
      userId: null,
    };
  }

  const storedRole = localStorage.getItem("role");
  const role = storedRole
    ? storedRole.replace(/^ROLE_/, "").toUpperCase()
    : null;

  const firstName = localStorage.getItem("firstName") || null;
  const userId = localStorage.getItem("userId") || null;

  return {
    token,
    role,
    firstName,
    userId,
  };
}

