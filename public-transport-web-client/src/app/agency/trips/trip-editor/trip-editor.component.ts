import {AfterViewInit, Component, OnInit} from '@angular/core';
import {LeafletEvent, LeafletMouseEvent, Map, Marker, Polyline} from "leaflet";
import * as L from "leaflet";
import {Stop, StopTime, Trip, Trips} from "../../../generated/public-transport";
import {StopService} from "../../stops/stop.service";
import {TripsService} from "../trips.service";
import {ActivatedRoute, Router} from "@angular/router";

@Component({
    selector: 'app-trip-editor',
    templateUrl: './trip-editor.component.html',
    styleUrl: './trip-editor.component.scss'
})
export class TripEditorComponent implements OnInit, AfterViewInit {
    private ICON = L.divIcon({
        html: `<div style="background-color: #0096FF; padding: 1px 0 0 1px; width: 20px; height: 20px; border-radius: 2px; color: whitesmoke"><img src="assets/bus-solid.svg"></div>`,
        className: 'stop-marker'
    });

    private map: Map;
    private stopMarkers: Marker[] = [];
    private routePolyline: Polyline;

    public state: { name: string, line: string, variant: string };

    public stopTimes: StopTime[] = [];

    constructor(private stopService: StopService, private tripsService: TripsService, private router: Router, private route: ActivatedRoute) {
        const navigation = this.router.getCurrentNavigation();
        this.state = navigation?.extras.state as { name: string, line: string, variant: string };
    }

    ngOnInit(): void {

    }

    ngAfterViewInit(): void {
        this.map = this.initMap();

        this.tripsService.getTripsByVariant(this.state.name, this.state.line, this.state.variant).subscribe((trips: Trips) => {
            const tripVariants = (trips?.trips || []);
            if (tripVariants.length == 1) {
                const tripVariant = tripVariants[0];
                this.stopTimes = tripVariant.stops || [];
            }

            this.drawPolyline();
        });


        this.reloadStops(this.map);
        this.onZoomEnd(this.map);
        this.onMoveEnd(this.map);
    }

    private initMap(): Map {
        const map: Map = L.map('map', {
            center: [50.613531, 20.743607],
            // center: [52.2321, 20.0559],
            // zoom: 7,
            zoom: 14,
            zoomControl: false
        });

        L.control.zoom({
            position: 'bottomright'
        }).addTo(map);

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 18,
            minZoom: 3,
            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        }).addTo(map);

        return map;
    }

    private onMoveEnd(map: Map): void {
        map.on('moveend', (event: LeafletEvent) => this.reloadStops(map));
    }

    private onZoomEnd(map: Map): void {
        map.on('zoomend', (event: LeafletEvent) => this.reloadStops(map));
    }

    private reloadStops(map: Map) {
        if (map.getZoom() > 12) {
            const bounds = map.getBounds();
            this.stopService.getStopsInArea(bounds.getNorth(), bounds.getWest(), bounds.getSouth(), bounds.getEast()).subscribe(response => {
                const stopMarkers: Marker[] = response.stops?.map((stop: Stop) => L.marker([stop?.lat || 0.0, stop?.lon || 0.0], {icon: this.ICON})
                    .on('click', (event: LeafletMouseEvent) => {
                        const stopTime: StopTime = {} as StopTime;
                        stopTime.stopId = stop.id;
                        stopTime.stopName = stop.name;
                        stopTime.lon = stop.lon;
                        stopTime.lat = stop.lat;

                        this.stopTimes.push(stopTime);

                        const trips: Trip = {};
                        trips.line = this.state.line || '';
                        trips.headsign = '';

                        trips.stops = this.stopTimes.map(stop => {
                            const stopTime: StopTime = {};
                            stopTime.stopId = stop.stopId;
                            stopTime.stopName = stop.stopName;
                            stopTime.lon = stop.lon;
                            stopTime.lat = stop.lat;

                            return stopTime;
                        });

                        this.tripsService.measureDistance(trips).subscribe(response => {
                            this.stopTimes = response.stops || [];
                            this.drawPolyline();
                        });

                    })) || []

                stopMarkers.forEach(marker => marker.addTo(map));

                for (let marker of this.stopMarkers) {
                    marker.removeFrom(map);
                }
                this.stopMarkers = stopMarkers;
            })
        } else {
            this.stopMarkers.forEach(marker => marker.removeFrom(map));
            this.stopMarkers = [];
        }
    }

    public sumMeters(stopId: string): number {
        let sum: number = 0.0;

        for (let val of this.stopTimes) {
            if (val.stopId === stopId) {
                sum = sum + (val?.meters || 0.0);
                return sum;
            } else {
                sum = sum + (val?.meters || 0.0);
            }
        }

        return sum;
    }

    public clickCreateOrEdit() {
        const trip: Trip = {};
        trip.line = this.state.line;
        trip.name = this.state.name;
        trip.headsign = 'PIERZCHNICA';

        trip.stops = this.stopTimes.map(stop => {
            const stopTime: StopTime = {};
            // stopTime.stopId = stop.id;
            // stopTime.stopName = stop.name;
            stopTime.lon = stop.lon;
            stopTime.lat = stop.lat;
            stopTime.meters = 0;
            stopTime.minutes = 3;

            return stopTime;
        });

        this.tripsService.create(trip).subscribe(() => {
        });
    }

    public drawPolyline() {
        const latLngPoints = this.stopTimes.map(stopTime => new L.LatLng(stopTime.lat || 0.0, stopTime.lon || 0.0));
        const polyline = L.polyline(latLngPoints, {
            color: '#416AB6',
            weight: 8,
            opacity: 0.9,
            smoothFactor: 1
        });

        if (this.routePolyline == null) {
            this.routePolyline = polyline;

        } else {
            this.routePolyline.removeFrom(this.map);
            this.routePolyline = polyline;
        }
        polyline.addTo(this.map);
    }
}
