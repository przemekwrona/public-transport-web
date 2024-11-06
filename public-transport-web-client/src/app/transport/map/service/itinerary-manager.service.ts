import {Injectable} from '@angular/core';
import {MapService} from './map.service';
import {Itinerary, RoutesResponse} from "../../../generated";
import {DivIcon, FitBoundsOptions, LatLng, LatLngBounds, Marker, Polyline} from "leaflet";
import * as L from "leaflet";
import {decode} from "@googlemaps/polyline-codec";

@Injectable({
    providedIn: 'root'
})
export class ItineraryManagerService {

    private STOP_CIRCLE: DivIcon = L.divIcon({
        html: '<i class="fa fa-circle-o " style="background-color: white; border-radius: 2em;"></i>',
        className: 'stop-circle'
    });

    private publicTransportPolylines: Polyline[] = [];
    private publicTransportFirstAndLatStopMarkers: Marker[] = [];
    private publicTransportIntermediateStopMarkers: Marker[] = [];

    private walkPolylines: Polyline[] = [];

    constructor(private mapService: MapService) {
    }

    public showPublicTransportOnMap(itinerary: Itinerary) {
        this.publicTransportPolylines.forEach(polyline => polyline.removeFrom(this.mapService.getMap()));

        this.publicTransportPolylines = (itinerary.legs || [])
            .map(leg => {
                let config = {};

                config = {
                    weight: 7,
                    color: `#${leg.routeColor}`
                    // color: "#2D9FFD"
                }

                if (leg.mode == 'WALK') {
                    config = {
                        weight: 8,
                        color: "#181818",
                        dashArray: "1 15"
                    }
                }

                return L.polyline(decode(leg.legGeometry?.points || '', 5), config)
            });

        this.publicTransportPolylines.forEach(polyline => polyline.addTo(this.mapService.getMap()));

        this.publicTransportFirstAndLatStopMarkers.forEach(stop => stop.removeFrom(this.mapService.getMap()));

        this.publicTransportFirstAndLatStopMarkers = (itinerary.legs || [])
            .map(leg => [leg?.from, leg?.to])
            .flat()
            .map(stop => {
                const marker = L.marker([stop?.lat || 0, stop?.lon || 0], {icon: this.STOP_CIRCLE});

                if (stop?.vertexType === 'BIKESHARE') {
                    marker.bindTooltip(`<div><img src="assets/warsaw/veturilo.png" style="width: 18px; border-radius: 5px; margin-right: 4px">${stop?.name || ''}</div>`, {
                        direction: 'right',
                        permanent: true,
                        sticky: false,
                        className: 'leaflet-tooltip-igeolab'
                    });
                } else {
                    marker.bindTooltip(`<div>${stop?.name || ''}</div>`, {
                        direction: 'right',
                        permanent: true,
                        sticky: false,
                        className: 'leaflet-tooltip-igeolab'
                    });
                }

                return marker;
            })
            .slice(1, -1);

        this.publicTransportFirstAndLatStopMarkers.forEach(stop => stop.addTo(this.mapService.getMap()));

        this.publicTransportIntermediateStopMarkers.forEach(stop => stop.removeFrom(this.mapService.getMap()));

        const stopCount = (itinerary.legs || [])
            .map(leg => (leg.intermediateStops || []))
            .flat().length;

        this.publicTransportIntermediateStopMarkers = (itinerary.legs || [])
            .map(leg => (leg.intermediateStops || []))
            .flat()
            .map(stop => {
                const marker = L.marker([stop?.lat || 0, stop?.lon || 0], {icon: this.STOP_CIRCLE});
                marker.bindTooltip(`<div>${stop.name}</div>`, {
                    direction: 'right',
                    permanent: true,
                    sticky: false,
                    className: 'leaflet-tooltip-igeolab'
                });
                return marker;
            });
        this.publicTransportIntermediateStopMarkers.forEach(stop => stop.addTo(this.mapService.getMap()));
    }

    public hidePublicTransportOnMap(): void {
        this.publicTransportPolylines.forEach(polyline => polyline.removeFrom(this.mapService.getMap()));
        this.publicTransportPolylines = [];

        this.publicTransportIntermediateStopMarkers.forEach(stop => stop.removeFrom(this.mapService.getMap()));
        this.publicTransportIntermediateStopMarkers = [];

        this.publicTransportFirstAndLatStopMarkers.forEach(stop => stop.removeFrom(this.mapService.getMap()));
        this.publicTransportFirstAndLatStopMarkers = [];
    }

    public zoomToPublicTransportItinerary(): void {
        const bounds: LatLngBounds = this.publicTransportPolylines
            .map(poly => poly.getBounds())
            .reduce((prev: LatLngBounds, curr: LatLngBounds) => prev.extend(curr), new LatLngBounds([]));

        bounds.extend(new LatLng(bounds.getSouthWest().lat, bounds.getSouthWest().lng));
        this.mapService.getMap().fitBounds(bounds);
    }

    public showWalkOnMap(walkItinerary: Itinerary): void {
        this.walkPolylines.forEach(polylines => polylines.removeFrom(this.mapService.getMap()));
        this.walkPolylines = (walkItinerary.legs || [])
            .map(leg => {
                let config = {
                    weight: 8,
                    color: "#6082B6",
                    dashArray: "1 15"
                };

                return L.polyline(decode(leg.legGeometry?.points || '', 5), config)
            });
        this.walkPolylines.forEach(polylines => polylines.addTo(this.mapService.getMap()));
    }

    public hideWalkOnMap(): void {
        this.walkPolylines.forEach(polyline => polyline.removeFrom(this.mapService.getMap()));
        this.walkPolylines = [];
    }

    public reload() {
    }
}
