import {AfterViewInit, Component, OnInit} from '@angular/core';
import {LeafletEvent, LeafletMouseEvent, Map, Marker, Polyline} from "leaflet";
import * as L from "leaflet";
import { findIndex, last } from "lodash";
import {Stop, StopTime, Trip, TripMode, Trips} from "../../../generated/public-transport";
import {StopService} from "../../stops/stop.service";
import {TripsService} from "../trips.service";
import {ActivatedRoute, Router} from "@angular/router";
import {CdkDragDrop, moveItemInArray} from "@angular/cdk/drag-drop";
import {animate, trigger, state, style, transition} from "@angular/animations";
import {debounceTime, Subject} from "rxjs";

interface DropzoneLayout {
    container: string;
    list: string;
    dndHorizontal: boolean;
}

@Component({
    selector: 'app-trip-editor',
    templateUrl: './trip-editor.component.html',
    styleUrl: './trip-editor.component.scss',
    animations: [
        trigger('simpleFadeAnimation', [
            state('in', style({opacity: 1})),
            transition(':enter', [style({opacity: 0}), animate(500)]),
            transition(':leave', animate(500, style({opacity: 0})))
        ])
    ]
})
export class TripEditorComponent implements OnInit, AfterViewInit {
    private ICON = L.divIcon({
        html: `<div style="background-color: #0096FF; padding: 1px 0 0 1px; width: 20px; height: 20px; border-radius: 2px; color: whitesmoke"><img src="assets/bus-solid.svg"></div>`,
        className: 'stop-marker'
    });

    private map: Map;
    private stopMarkers: Marker[] = [];
    private routePolyline: Polyline;

    private communicationVelocitySubject = new Subject<number>();


    public state: { name: string, line: string, variant: string, mode: TripMode };

    public variant: string = '';
    public mode: TripMode | null;
    public headsign: string = '';

    public communicationVelocity: number = 45;

    public tripMode = TripMode;

    public stopTimes: StopTime[] = [];

    constructor(private stopService: StopService, private tripsService: TripsService, private router: Router, private route: ActivatedRoute) {
        const navigation = this.router.getCurrentNavigation();
        this.state = navigation?.extras.state as { name: string, line: string, variant: string, mode: TripMode };
        this.variant = this.state.variant || '';
        this.mode = this.state.mode;

        this.communicationVelocitySubject.pipe(debounceTime(1000)).subscribe(() => this.measureDistance());
    }

    ngOnInit(): void {
    }

    ngAfterViewInit(): void {
        this.map = this.initMap();

        this.tripsService.getTripsByVariant(this.state.name, this.state.line, this.state.variant).subscribe((trips: Trips) => {
            const tripVariants = (trips?.trips || []);
            if (tripVariants.length == 1) {
                const tripVariant: Trip = tripVariants[0];
                this.headsign = tripVariant.headsign || '';
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

                        this.measureDistance();

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

    private measureDistance() {
        const trips: Trip = {};
        trips.line = this.state.line || '';
        trips.headsign = '';
        trips.communicationVelocity = this.communicationVelocity;

        trips.stops = this.stopTimes.map(stop => {
            const stopTime: StopTime = {};
            stopTime.stopId = stop.stopId;
            stopTime.stopName = stop.stopName;
            stopTime.lon = stop.lon;
            stopTime.lat = stop.lat;

            return stopTime;
        });

        this.tripsService.measureDistance(trips).subscribe(response => {

            for (const index in response.stops) {
                const stopTime: StopTime = (response.stops || [])[Number(index)];
                this.stopTimes[Number(index)].meters = stopTime.meters;
                this.stopTimes[Number(index)].seconds = stopTime.seconds;
            }

            this.drawPolyline();
        });
    }

    public clickCreateOrEdit() {
        const trip: Trip = {};
        trip.line = this.state.line;
        trip.name = this.state.name;
        trip.variant = this.variant;
        trip.mode = this.mode || TripMode.Main;
        trip.headsign = this.headsign;
        trip.communicationVelocity = this.communicationVelocity;

        trip.stops = this.stopTimes.map(stop => {
            const stopTime: StopTime = {};
            stopTime.stopId = stop.stopId;
            stopTime.stopName = stop.stopName;
            stopTime.lon = stop.lon;
            stopTime.lat = stop.lat;
            stopTime.meters = stop.meters;
            stopTime.seconds = stop.seconds;

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

    public drop(event: CdkDragDrop<string[]>) {
        moveItemInArray(this.stopTimes, event.previousIndex, event.currentIndex);
        this.measureDistance();
    }

    public remove(stopTime: StopTime) {
        const index = findIndex(this.stopTimes, {stopId: stopTime.stopId});
        this.stopTimes.splice(index, 1);
        this.drawPolyline();
    }

    public addBrake() {
        const lastStop = last(this.stopTimes);
        const stopTime: StopTime = {} as StopTime;
        stopTime.stopId = lastStop.stopId + '-break';
        stopTime.stopName = lastStop.stopName;
        stopTime.lon = lastStop.lon;
        stopTime.lat = lastStop.lat;

        this.stopTimes.push(stopTime);
    }

    public onCommunicationVelocityChange(value: number): void {
        this.communicationVelocitySubject.next(value);
    }
}
