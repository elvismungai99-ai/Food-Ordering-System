export function buildOpenRouteServiceMapUrl(
  latitude: number,
  longitude: number
): string {
  return `https://maps.openrouteservice.org/#/place/@${longitude},${latitude},18`;
}
