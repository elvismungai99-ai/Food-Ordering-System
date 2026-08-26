import api from "../api/axios";

export function buildGoogleMapsPlaceUrl(
  latitude: number,
  longitude: number
): string {
  const query = encodeURIComponent(`${latitude},${longitude}`);

  return `https://www.google.com/maps/search/?api=1&query=${query}`;
}

export function buildGoogleMapsRouteUrl(
  fromLatitude: number,
  fromLongitude: number,
  toLatitude: number,
  toLongitude: number
): string {
  const origin = encodeURIComponent(`${fromLatitude},${fromLongitude}`);
  const destination = encodeURIComponent(`${toLatitude},${toLongitude}`);

  return `https://www.google.com/maps/dir/?api=1&origin=${origin}&destination=${destination}&travelmode=driving`;
}

/**
 * Resolves a human-readable street address from GPS latitude & longitude.
 * 1. Calls the backend Google Maps geocoding endpoint.
 * 2. Falls back to OpenStreetMap Nominatim if Google Maps is unavailable/restricted.
 * 3. Falls back to formatted GPS coordinates if all networks fail.
 */
export async function reverseGeocodeLocation(
  latitude: number,
  longitude: number
): Promise<string> {
  // 1. Try Google Maps reverse geocoding via Spring Boot backend
  try {
    const response = await api.get<{ displayName?: string }>(
      "/location/reverse",
      {
        params: {
          lat: latitude,
          lon: longitude,
        },
      }
    );

    if (response.data?.displayName && response.data.displayName.trim()) {
      return response.data.displayName.trim();
    }
  } catch (backendError) {
    console.warn(
      "Backend Google reverse geocoding unavailable, falling back to OSM:",
      backendError
    );
  }

  // 2. Fallback to OpenStreetMap Nominatim reverse geocoder
  try {
    const osmResponse = await fetch(
      `https://nominatim.openstreetmap.org/reverse?format=json&lat=${latitude}&lon=${longitude}&zoom=18&addressdetails=1`,
      {
        headers: {
          "Accept-Language": "en",
        },
      }
    );

    if (osmResponse.ok) {
      const data = await osmResponse.json();
      if (data && data.display_name && typeof data.display_name === "string") {
        return data.display_name.trim();
      }
    }
  } catch (osmError) {
    console.warn("OSM reverse geocoding failed:", osmError);
  }

  // 3. Fallback to readable GPS coordinates
  return `GPS Location (${latitude.toFixed(5)}, ${longitude.toFixed(5)})`;
}


