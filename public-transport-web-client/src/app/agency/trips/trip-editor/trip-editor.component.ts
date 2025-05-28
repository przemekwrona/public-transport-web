import {AfterViewInit, Component, OnInit} from '@angular/core';
import * as L from "leaflet";
import {LeafletEvent, LeafletMouseEvent, Map, Marker, Polyline} from "leaflet";
import {findIndex, last} from "lodash";
import {
    Stop,
    StopTime,
    Trip, TripId,
    TripMode,
    Trips,
    TripsDetails,
    UpdateTripDetailsRequest
} from "../../../generated/public-transport";
import {StopService} from "../../stops/stop.service";
import {TripsService} from "../trips.service";
import {ActivatedRoute, Data, Router} from "@angular/router";
import {CdkDragDrop, moveItemInArray} from "@angular/cdk/drag-drop";
import {animate, state, style, transition, trigger} from "@angular/animations";
import {debounceTime, map, Subject} from "rxjs";
import {TripEditorComponentMode} from "./trip-editor-component-mode";
import {routes} from "../../../app.routes";

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
    private previousVariantName = '';

    public state: { name: string, line: string, variant: string, mode: TripMode };

    public tripModeSelectValue = TripMode;
    public tripEditorComponentMode: TripEditorComponentMode;

    public $tripDetails: TripsDetails = {trip: {}};
    public $tripVariants: Trips = {};

    constructor(private stopService: StopService, private tripsService: TripsService, private router: Router, private _route: ActivatedRoute) {
        this.communicationVelocitySubject.pipe(debounceTime(1000)).subscribe(() => this.measureDistance());
    }

    ngOnInit(): void {
        this._route.data.subscribe((data: Data) => this.tripEditorComponentMode = data['mode']);
        this._route.data.pipe(map((data: Data) => data['trip'])).subscribe(tripDetails => {
            this.$tripDetails = tripDetails;

            this._route.data.pipe(map((data: Data) => data['variants'])).subscribe((tripVariants: Trips) => {
                this.$tripVariants = tripVariants;

                if (this.tripEditorComponentMode === TripEditorComponentMode.CREATE) {
                    if (this.$tripVariants?.trips.length || 0 == 0) {
                        this.$tripDetails.trip.isMainVariant = true;
                        this.$tripDetails.trip.variant = "MAIN";
                        this.$tripDetails.trip.mode = TripMode.Front;
                        this.$tripDetails.trip.origin = tripVariants.route.originStop.name;
                        this.$tripDetails.trip.destination = tripVariants.route.destinationStop.name;
                        this.$tripDetails.trip.headsign = tripVariants.route.destinationStop.name;

                        const stopTime: StopTime = {} as StopTime;
                        stopTime.stopId = tripVariants.route.originStop.id;
                        stopTime.stopName = tripVariants.route.originStop.name;
                        stopTime.lon = tripVariants.route.originStop.lon;
                        stopTime.lat = tripVariants.route.originStop.lat;

                        this.$tripDetails.trip.stops.push(stopTime);

                        this.measureDistance();
                    } else if (this.$tripVariants.trips.length == 1 && this.$tripVariants.trips[0].isMainVariant && this.$tripVariants.trips[0].mode === TripMode.Front) {
                        this.$tripDetails.trip.isMainVariant = true;
                        this.$tripDetails.trip.variant = "MAIN";
                        this.$tripDetails.trip.mode = TripMode.Back;
                        this.$tripDetails.trip.origin = tripVariants.route.destinationStop.name;
                        this.$tripDetails.trip.destination = tripVariants.route.originStop.name;
                        this.$tripDetails.trip.headsign = tripVariants.route.originStop.name;

                        const stopTime: StopTime = {} as StopTime;
                        stopTime.stopId = tripVariants.route.destinationStop.id;
                        stopTime.stopName = tripVariants.route.destinationStop.name;
                        stopTime.lon = tripVariants.route.destinationStop.lon;
                        stopTime.lat = tripVariants.route.destinationStop.lat;

                        this.$tripDetails.trip.stops.push(stopTime);

                        this.measureDistance();
                    }
                }
            });
        });
        this._route.queryParams.subscribe(params => this.state = params as {
            line: string,
            name: string,
            variant: string,
            mode: TripMode
        });
        this.$tripDetails.trip.communicationVelocity = 45;
    }

    ngAfterViewInit(): void {
        this.map = this.initMap();
        if (this.tripEditorComponentMode === TripEditorComponentMode.CREATE) {
            this.map.flyTo([this.$tripVariants.route.originStop.lat, this.$tripVariants.route.originStop.lon], 15)
        }

        this.drawPolyline();
        this.zoomPolyline();

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

                        this.$tripDetails.trip.stops.push(stopTime);

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
        trips.communicationVelocity = this.$tripDetails.trip.communicationVelocity;

        trips.stops = this.$tripDetails.trip.stops.map(stop => {
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
                this.$tripDetails.trip.stops[Number(index)].meters = stopTime.meters;
                this.$tripDetails.trip.stops[Number(index)].seconds = stopTime.seconds;
            }

            this.drawPolyline();
        });
    }

    public clickCreateOrEdit() {
        const tripId: TripId = {};
        tripId.line = this.state.line;
        tripId.name = this.state.name;
        tripId.variant = this.state.variant;
        tripId.mode = this.state.mode;

        const tripDetailsRequest: UpdateTripDetailsRequest = {};
        tripDetailsRequest.tripId = tripId;
        tripDetailsRequest.trip = this.$tripDetails;

        if (this.tripEditorComponentMode == TripEditorComponentMode.CREATE) {
            this.tripsService.create(tripDetailsRequest).subscribe(() => {
            });
        } else if (this.tripEditorComponentMode == TripEditorComponentMode.EDIT) {
            this.tripsService.update(tripDetailsRequest).subscribe(() => {
            });
        }

    }

    public drawPolyline() {
        const latLngPoints = (this.$tripDetails?.trip?.stops || []).map(stopTime => new L.LatLng(stopTime.lat || 0.0, stopTime.lon || 0.0));
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
        moveItemInArray(this.$tripDetails.trip.stops, event.previousIndex, event.currentIndex);
        this.measureDistance();
    }

    public remove(stopTime: StopTime) {
        const index = findIndex(this.$tripDetails.trip.stops, {stopId: stopTime.stopId});
        this.$tripDetails.trip.stops.splice(index, 1);
        this.drawPolyline();
    }

    public addBrake() {
        // const lastStop = last(this.stopTimes);
        // const stopTime: StopTime = {} as StopTime;
        // stopTime.stopId = lastStop.stopId + '-break';
        // stopTime.stopName = lastStop.stopName;
        // stopTime.lon = lastStop.lon;
        // stopTime.lat = lastStop.lat;
        //
        // this.stopTimes.push(stopTime);
    }

    public onCommunicationVelocityChange(value: number): void {
        this.communicationVelocitySubject.next(value);
    }

    public getLastStop(): StopTime {
        return last(this.$tripDetails.trip.stops);
    }

    public zoomPolyline(): void {
        if (this.routePolyline.getBounds().isValid()) {
            this.map.fitBounds(this.routePolyline.getBounds());
        }
    }

    public clickIsMainVariant(): void {
        if (this.$tripDetails.trip.isMainVariant) {
            this.$tripDetails.trip.variant = this.previousVariantName;
        } else {
            this.previousVariantName = this.$tripDetails.trip.variant;
            this.$tripDetails.trip.variant = 'MAIN';
            this.$tripDetails.trip.variantDesignation = '';
            this.$tripDetails.trip.variantDescription = '';
        }

    }

    protected readonly routes = routes;
}
