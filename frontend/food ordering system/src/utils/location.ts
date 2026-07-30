export function buildOpenRouteServiceMapUrl(
  latitude: number,
  longitude: number
): string {
  return `https://maps.openrouteservice.org/#/place/@${longitude},${latitude},18`;
}

export function buildOpenRouteServiceRouteUrl(
  fromLatitude: number,
  fromLongitude: number,
  toLatitude: number,
  toLongitude: number
): string {
  return `https://maps.openrouteservice.org/#/directions/${fromLongitude},${fromLatitude}/${toLongitude},${toLatitude}`;
}
