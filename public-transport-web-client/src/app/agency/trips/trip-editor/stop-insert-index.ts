export interface StopCoordinate {
    lat?: number | null;
    lon?: number | null;
}

/**
 * Index for a stop added on the trip edit screen.
 * The stop is inserted between the two geographically nearest stops when
 * neither of them is stop 1, stop 2, the penultimate, or the last stop.
 * Otherwise it is appended.
 */
export function stopInsertIndex(existing: StopCoordinate[], added: StopCoordinate): number {
    const count = existing.length;
    if (count < 2 || !hasCoordinate(added)) {
        return count;
    }

    const nearest = existing
        .map((stop, index) => ({index, distance: distanceMeters(added, stop)}))
        .filter((item): item is {index: number; distance: number} => item.distance != null)
        .sort((left, right) => left.distance - right.distance || left.index - right.index)
        .slice(0, 2);

    if (nearest.length < 2) {
        return count;
    }

    const [first, second] = nearest;
    const terminal = new Set([0, 1, count - 2, count - 1]);
    if (terminal.has(first.index) || terminal.has(second.index)) {
        return count;
    }

    return Math.min(first.index, second.index) + 1;
}

function hasCoordinate(stop: StopCoordinate): stop is StopCoordinate & {lat: number; lon: number} {
    return Number.isFinite(stop.lat) && Number.isFinite(stop.lon);
}

function distanceMeters(from: StopCoordinate & {lat: number; lon: number}, to: StopCoordinate): number | null {
    if (!hasCoordinate(to)) {
        return null;
    }

    const earthRadius = 6_371_000;
    const toRadians = (degrees: number) => degrees * Math.PI / 180;
    const latitudeDelta = toRadians(to.lat - from.lat);
    const longitudeDelta = toRadians(to.lon - from.lon);
    const a = Math.sin(latitudeDelta / 2) ** 2
        + Math.cos(toRadians(from.lat)) * Math.cos(toRadians(to.lat)) * Math.sin(longitudeDelta / 2) ** 2;
    return 2 * earthRadius * Math.asin(Math.min(1, Math.sqrt(a)));
}
