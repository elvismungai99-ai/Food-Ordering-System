import {
  useEffect,
  useMemo,
} from "react";

import {
  APIProvider,
  Map,
  Marker,
  Polyline,
  useMap,
} from "@vis.gl/react-google-maps";

const GOOGLE_MAPS_API_KEY =
  import.meta.env
    .VITE_GOOGLE_MAPS_API_KEY as string | undefined;

/*
 * Fallback center (Nairobi) used when no
 * coordinates are available at all.
 */
const DEFAULT_CENTER = {
  lat: -1.286389,
  lng: 36.817223,
};

export interface LiveTrackingMapProps {
  restaurantLatitude?: number | null;
  restaurantLongitude?: number | null;
  destinationLatitude?: number | null;
  destinationLongitude?: number | null;
  riderLatitude?: number | null;
  riderLongitude?: number | null;
}

interface LatLng {
  lat: number;
  lng: number;
}

function toLatLng(
  latitude?: number | null,
  longitude?: number | null
): LatLng | null {

  if (
    latitude == null
    || longitude == null
  ) {
    return null;
  }

  return {
    lat: latitude,
    lng: longitude,
  };
}

/*
 * Fits the viewport around every marker
 * whenever the set of points changes.
 */
function MapContent({
  points,
}: {
  points: LatLng[];
}) {
  const map =
    useMap();

  const boundsKey =
    useMemo(
      () =>
        points
          .map(point =>
            `${point.lat.toFixed(5)},${point.lng.toFixed(5)}`
          )
          .join("|"),
      [points]
    );

  useEffect(() => {

    if (!map || points.length === 0) {
      return;
    }

    if (
      points.length === 1
    ) {
      map.setCenter(
        points[0]
      );

      map.setZoom(14);

      return;
    }

    const bounds =
      new google.maps.LatLngBounds();

    for (const point of points) {
      bounds.extend(point);
    }

    map.fitBounds(bounds, 64);

  }, [map, boundsKey, points]);

  return null;
}

function LiveTrackingMapInner({
  restaurant,
  destination,
  rider,
}: {
  restaurant: LatLng | null;
  destination: LatLng | null;
  rider: LatLng | null;
}) {
  const mapPoints =
    useMemo(
      () =>
        [restaurant, rider, destination]
          .filter(
            (point): point is LatLng =>
              point != null
          ),
      [restaurant, rider, destination]
    );

  const routePath =
    restaurant && destination
      ? [
          restaurant,
          destination,
        ]
      : null;

  return (
    <Map
      style={{
        width: "100%",
        height: "100%",
      }}
      defaultCenter={
        mapPoints[0]
        ?? DEFAULT_CENTER
      }
      defaultZoom={13}
      gestureHandling="greedy"
      disableDefaultUI={false}
      mapTypeControl={false}
      streetViewControl={false}
    >
      <MapContent points={mapPoints} />

      {routePath && (
        <Polyline
          path={routePath}
          strokeColor="#6366f1"
          strokeOpacity={0.45}
          strokeWeight={3}
        />
      )}

      {restaurant && (
        <Marker
          position={restaurant}
          title="Restaurant"
          label={{
            text: "🍴",
            fontSize: "18px",
          }}
        />
      )}

      {destination && (
        <Marker
          position={destination}
          title="Delivery destination"
          label={{
            text: "🏠",
            fontSize: "18px",
          }}
        />
      )}

      {rider && (
        <Marker
          position={rider}
          title="Your rider"
          label={{
            text: "🛵",
            fontSize: "20px",
          }}
          zIndex={1000}
        />
      )}
    </Map>
  );
}

/*
 * Embedded Google Map showing the
 * restaurant pickup point, the delivery
 * destination and the rider's live position.
 *
 * Renders a friendly placeholder instead of
 * the map when the API key or coordinates
 * are missing.
 */
export function LiveTrackingMap(
  props: LiveTrackingMapProps
) {

  const restaurant =
    toLatLng(
      props.restaurantLatitude,
      props.restaurantLongitude
    );

  const destination =
    toLatLng(
      props.destinationLatitude,
      props.destinationLongitude
    );

  const rider =
    toLatLng(
      props.riderLatitude,
      props.riderLongitude
    );

  if (!GOOGLE_MAPS_API_KEY) {
    return (
      <div className="flex h-72 items-center justify-center rounded-[24px] bg-slate-100 text-sm text-slate-500">
        Map preview unavailable — Google
        Maps API key is not configured.
      </div>
    );
  }

  if (!restaurant && !destination) {
    return (
      <div className="flex h-72 items-center justify-center rounded-[24px] bg-slate-100 text-sm text-slate-500">
        Map coordinates for this order are
        not available yet.
      </div>
    );
  }

  return (
    <div className="h-80 overflow-hidden rounded-[24px] border border-slate-200">
      <APIProvider apiKey={GOOGLE_MAPS_API_KEY}>
        <LiveTrackingMapInner
          restaurant={restaurant}
          destination={destination}
          rider={rider}
        />
      </APIProvider>
    </div>
  );
}

export default LiveTrackingMap;
