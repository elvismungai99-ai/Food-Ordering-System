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

