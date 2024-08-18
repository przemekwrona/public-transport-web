import {Component, Input} from '@angular/core';
import * as L from 'leaflet';
import {DivIcon, LatLng, LatLngBounds, LatLngExpression, Map, Marker, Polyline} from "leaflet";
import {OtpService} from "../../../http/otp.service";
import moment from "moment";
import {decode} from "@googlemaps/polyline-codec";
import {Itinerary, RoutesResponse} from "../../../generated";

export class OtpPoint {
    name: string = '';
    lat: number;
    lon: number;
}

@Component({
    selector: 'app-otp',
    templateUrl: './otp.component.html',
    styleUrl: './otp.component.scss'
})
export class OtpComponent {
    private STOP_CIRCLE: DivIcon = L.divIcon({
        html: '<i class="fa fa-circle-o " style="background-color: white; border-radius: 2em;"></i>',
        className: 'stop-circle'
    });

    @Input() startPoint: OtpPoint;
    @Input() map: Map;

    private _endPoint: OtpPoint;

    @Input() set endPoint(endPoint: OtpPoint) {
        this._endPoint = endPoint;
        if (this.wasPlanned) {
            this.plan();
        }
    }

    get endPoint(): OtpPoint {
        return this._endPoint;
    }

    public routeResponse: RoutesResponse | null;
    public bikeResponse: RoutesResponse | null;
    public panelOpenState: boolean = false;

    private wasPlanned: boolean = false;

    private polyline: Polyline[] = [];
    private intermediateStops: Marker[] = [];
    private firstAndLatStops: Marker[] = [];
    public isExpanded: boolean = true;

    constructor(private otpService: OtpService) {
    }

    public plan(): void {
        this.wasPlanned = true;
        this.otpService.plan(this.startPoint.lat, this.startPoint.lon, this.endPoint.lat, this.endPoint.lon, moment()).subscribe(routeResponse => this.routeResponse = routeResponse);
        this.otpService.planBike(this.startPoint.lat, this.startPoint.lon, this.endPoint.lat, this.endPoint.lon, moment()).subscribe(bikeResponse => this.bikeResponse = bikeResponse);
    }

    public hasPlan(): boolean {
        return this.routeResponse != null;
    }

    public isItinerary(itinerary: Itinerary): boolean {
        return (itinerary.legs || []).length > 1;
    }

    public isWalk(itinerary: Itinerary): boolean {
        return (itinerary.legs || []).length == 1 && (itinerary.legs || [])[0]?.mode == 'WALK';
    }

    public isBike(itinerary: Itinerary): boolean {
        const bicycles = (itinerary.legs || []).map(leg => leg.mode).filter(mode => mode === 'BICYCLE');
        if (bicycles.length > 0) {
            console.log(bicycles.length);
        }
        return false;
    }

    public scrollTop(): void {
    }

    public expand(): void {
        this.isExpanded = true;

    }

    public collapse(): void {
        this.isExpanded = false;
    }

    public showItineraryOnMap(itinerary: Itinerary): void {
        this.polyline.forEach(polyline => polyline.removeFrom(this.map));

        this.polyline = (itinerary.legs || [])
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

                if (leg.mode == 'BICYCLE') {
                    config = {
                        weight: 8,
                        color: "#2790FF"
                    }
                }

                return L.polyline(decode(leg.legGeometry?.points || '', 5), config)
            })
        this.polyline.forEach(polyline => polyline.addTo(this.map));

        this.firstAndLatStops.forEach(stop => stop.removeFrom(this.map));

        this.firstAndLatStops = (itinerary.legs || [])
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

        this.firstAndLatStops.forEach(stop => stop.addTo(this.map));

        this.intermediateStops.forEach(stop => stop.removeFrom(this.map));

        const stopCount = (itinerary.legs || [])
            .map(leg => (leg.intermediateStops || []))
            .flat().length;

        this.intermediateStops = (itinerary.legs || [])
            .map(leg => (leg.intermediateStops || []))
            .flat()
            .map(stop => {
                const marker = L.marker([stop?.lat || 0, stop?.lon || 0], {icon: this.STOP_CIRCLE});

                if (stopCount < 15) {
                    marker.bindTooltip(`<div>${stop.name}</div>`, {
                        direction: 'right',
                        permanent: true,
                        sticky: false,
                        className: 'leaflet-tooltip-igeolab'
                    });
                }

                return marker;
            });
        this.intermediateStops.forEach(stop => stop.addTo(this.map));
    }

    public hideItineraryOnMap(): void {
        if (!this.isExpanded) {
            this.polyline.forEach(polyline => polyline.removeFrom(this.map));
            this.polyline = [];

            this.intermediateStops.forEach(stop => stop.removeFrom(this.map));
            this.intermediateStops = [];

            this.firstAndLatStops.forEach(stop => stop.removeFrom(this.map));
            this.firstAndLatStops = [];
        }
    }

    public zoomToItinerary(): void {
        const bounds: LatLngBounds = this.polyline
            .map(poly => poly.getBounds())
            .reduce((prev: LatLngBounds, curr: LatLngBounds) => prev.extend(curr));

        bounds.extend(new LatLng(bounds.getSouthWest().lat, bounds.getSouthWest().lng - 0.008));
        this.map.fitBounds(bounds);
    }

}
