import {AfterViewChecked, AfterViewInit, Component, OnInit} from '@angular/core';
import * as L from 'leaflet';
import {CircleMarker, LatLng, LatLngBounds, LeafletEvent, LeafletMouseEvent, Map, Marker, Polyline} from "leaflet";
import {StopService} from "../../http/stop.service";
import {Shape, Timetables, TimetableService} from "../../http/timetable.service";
import {TripService} from "../../http/trip.service";
import {DepartureMetadata} from "./timetable-content/timetable-content.component";
import {OtpPoint} from "./otp/otp.component";
import {OtpService} from "../../http/otp.service";
import {Route, Stop, StopDetails, StopTime} from "../../generated/public-transport-planner-api";
import moment from "moment";
import {interval, Observable, timer} from "rxjs";
import {MapService} from "./service/map.service";
import {BikeMapManagerService} from "./service/bike-map-manager.service";
import {ItineraryManagerService} from "./service/itinerary-manager.service";

@Component({
    selector: 'app-map',
    templateUrl: './map.component.html',
    styleUrl: './map.component.scss',
    standalone: false
})
export class MapComponent implements OnInit, AfterViewInit {
    private ZOOM: number = 16;

    private stopMarkers: Marker[] = [];
    private tripMarkers: Marker[] = [];
    private bikeStationMarkers: Marker[] = [];
    private polyline: Polyline | null;

    public map: Map;

    public currentStop: Stop | null;
    public currentStopDetails: StopDetails | null;
    public currentStopRoutes: Route[] = [];
    public currentStopTimes: StopTime[] = [];
    public currentTimetables: Timetables | null;
    public currentLine: string | null;

    public startPoint: OtpPoint = new OtpPoint();
    public endPoint: OtpPoint = new OtpPoint();
    private startMarker: CircleMarker | null;
    private startBorderMarker: CircleMarker | null;
    private endMarker: CircleMarker | null;
    private endBorderMarker: CircleMarker | null;

    public showPlanDetails: boolean = false;

    constructor(private mapService: MapService, private stopService: StopService, private timetableService: TimetableService, private tripService: TripService, private otpService: OtpService, private bikeMapManagerService: BikeMapManagerService, private itineraryManagerService: ItineraryManagerService) {
        this.getStops(52.240, 20.890, 52.220, 21.120);
    }

    ngOnInit(): void {
        this.map = this.mapService.initMap();
        this.initHideBikeStation(this.map);
    }

    ngAfterViewInit(): void {
        timer(0, 60 * 1000).subscribe(() => {
            if (this.map.getZoom() >= 15) {
                this.getBikeStationStatus(this.map);
            }
        });

        this.map.on('moveend', (event: LeafletEvent) => {
            if (this.map.getZoom() >= this.ZOOM) {
                const bounds: LatLngBounds = this.map.getBounds();
                const ne = bounds.getNorthEast();
                const sw = bounds.getSouthWest();
                this.getStops(ne.lat, sw.lng, sw.lat, ne.lng);
            }
        });

        this.map.on('zoomend', (event: LeafletEvent) => {
            if (this.map.getZoom() < this.ZOOM) {
                for (let marker of this.stopMarkers) {
                    marker.removeFrom(this.map);
                }
                this.stopMarkers = [];
            }
        });

        this.map.on('zoomend', (event: LeafletEvent) => {
            if (this.startBorderMarker != null) {
                const startBorderMarker = this.getCircleBorderMarker(this.startBorderMarker.getLatLng(), this.map.getZoom());
                this.startBorderMarker.removeFrom(this.map);
                this.startBorderMarker = startBorderMarker;
                this.startBorderMarker.addTo(this.map);
            }
            if (this.endBorderMarker != null) {
                const endBorderMarker = this.getCircleBorderMarker(this.endBorderMarker.getLatLng(), this.map.getZoom());
                this.endBorderMarker.removeFrom(this.map);
                this.endBorderMarker = endBorderMarker;
                this.endBorderMarker.addTo(this.map);
            }
            if (this.startMarker != null) {
                const startMarker = this.getCircleMarker(this.startMarker.getLatLng(), this.map.getZoom(), '#20A470');
                this.startMarker.removeFrom(this.map);
                this.startMarker = startMarker;
                this.startMarker.addTo(this.map);
            }
            if (this.endMarker != null) {
                const endMarker = this.getCircleMarker(this.endMarker.getLatLng(), this.map.getZoom(), '#061431');
                this.endMarker.removeFrom(this.map);
                this.endMarker = endMarker;
                this.endMarker.addTo(this.map);
            }
        });

        this.map.on('click', (event: LeafletMouseEvent) => {

            this.currentStop = null;

            if (this.startPoint.name == '') {
                this.startPoint = new OtpPoint();
                this.startPoint.name = `${event.latlng.lat.toFixed(5)},${event.latlng.lng.toFixed(5)}`;
                this.startPoint.lat = event.latlng.lat;
                this.startPoint.lon = event.latlng.lng;

                this.startMarker = this.getCircleMarker(event.latlng, this.map.getZoom(), '#20A470');
                this.startBorderMarker = this.getCircleBorderMarker(event.latlng, this.map.getZoom())
                this.startBorderMarker.addTo(this.map);
                this.startMarker.addTo(this.map);
            } else {
                this.endPoint = new OtpPoint();
                this.endPoint.name = `${event.latlng.lat.toFixed(5)},${event.latlng.lng.toFixed(5)}`;
                this.endPoint.lat = event.latlng.lat;
                this.endPoint.lon = event.latlng.lng;

                if (this.endMarker != null) {
                    this.endMarker.removeFrom(this.map);
                }

                if (this.endBorderMarker != null) {
                    this.endBorderMarker.removeFrom(this.map);
                }

                this.endMarker = this.getCircleMarker(event.latlng, this.map.getZoom(), '#061431');
                this.endBorderMarker = this.getCircleBorderMarker(event.latlng, this.map.getZoom());

                this.endBorderMarker.addTo(this.map);
                this.endMarker.addTo(this.map);
            }

            // if (this.currentLine == null) {
            //     this.currentStop = null;
            //     this.polyline?.removeFrom(this.map);
            //     this.polyline = null;
            //     this.tripMarkers.forEach(tripMaker => tripMaker.removeFrom(this.map));
            //     this.tripMarkers = [];
            // }
            // this.currentLine = null;
        });

    }

    private markerRadius(price: number, zoom: number): number {
        let radius = 3;
        for (let i = 0; i < 18 - zoom; i++) {
            radius = 2 * radius;
        }
        return radius;
    }


    private getCircleMarker(latlng: LatLng, zoom: number, color: string) {
        return L.circle([latlng.lat, latlng.lng], {
            color: color,
            fillColor: color,
            fillOpacity: 0.4,
            radius: this.markerRadius(12, zoom),
            stroke: true,
            weight: 6
        });
    }

    private getCircleBorderMarker(latlng: LatLng, zoom: number) {
        return L.circle([latlng.lat, latlng.lng], {
            color: 'white',
            radius: 1.1 * this.markerRadius(12, zoom),
            stroke: true,
            weight: 6
        });
    }

    private getBikeStationStatus(map: Map): void {
        this.bikeMapManagerService.getBikeStationStatus('WAWA').subscribe(bikeStationMarkers => {
            bikeStationMarkers.forEach(bikeStationMarker => bikeStationMarker.addTo(map));
            for (let marker of this.bikeStationMarkers) {
                marker.removeFrom(map);
            }
            this.bikeStationMarkers = bikeStationMarkers;
        });
    }

    getStops(maxLat: number, minLon: number, minLat: number, maxLon: number): void {
        this.otpService.getStops(maxLat, minLon, minLat, maxLon).subscribe(stops => {
            const markers = [];

            for (let stop of stops) {
                const stopSubGroup = stop.id?.slice(-2);

                let icon = L.divIcon({
                    html: `<div style="background-color: #0096FF; padding: 1px 2px 6px 3px; width: 19px; height: 18px; border-radius: 2px; color: whitesmoke">${stopSubGroup}</div>`,
                    className: 'stop-marker'
                })

                if (stopSubGroup === 'P1' || stopSubGroup === 'P2') {
                    icon = L.divIcon({
                        html: `<img src="assets/warsaw/warsaw_metro.png" width="25" height="25">`,
                        className: 'stop-marker'
                    })
                }

                let stopMarker = L.marker([stop?.lat || 0.0, stop?.lon || 0.0], {icon: icon});

                //   if (stopSubGroup.startsWith('m')) {
                //     const iconMetro = L.icon({iconUrl: 'assets/warsaw_metro.png', iconSize: [25, 25]});
                //     stopMarker = L.marker([stop.stopLat, stop.stopLon], {icon: iconMetro});
                //   }

                stopMarker.addTo(this.map).on('click', (e) => {
                    this.currentStop = stop;
                    this.otpService.getStopDetails(stop.id || '').subscribe(stopDetails => this.currentStopDetails = stopDetails);
                    this.otpService.getRoutes(stop.id || '').subscribe(routes => this.currentStopRoutes = routes);
                    this.otpService.getStopTimes(stop.id || '', moment()).subscribe(stopTimes => this.currentStopTimes = stopTimes);
                    this.showPlanDetails = false;

                    this.itineraryManagerService.hidePublicTransportOnMap();

                    if (this.startPoint.name == '') {
                        this.startPoint = new OtpPoint();
                        this.startPoint.name = stop.name || '';
                        this.startPoint.lat = stop.lat || 0.0;
                        this.startPoint.lon = stop.lon || 0.0;
                    } else {
                        this.endPoint = new OtpPoint();
                        this.endPoint.name = stop.name || '';
                        this.endPoint.lat = stop.lat || 0.0;
                        this.endPoint.lon = stop.lon || 0.0;
                    }
                });
                markers.push(stopMarker);
            }

            const removeMarkers = this.stopMarkers;
            this.stopMarkers = markers;
            for (let marker of removeMarkers) {
                marker.removeFrom(this.map);
            }
        });
    }

    onClickLine(line: string) {
        this.currentLine = line;
    }

    hasCurrentStop(): boolean {
        return this.currentStop != null;
    }

    hasCurrentLine(): boolean {
        return this.hasCurrentStop() && this.currentLine != null;
    }

    public showTrip(metadata: DepartureMetadata) {
        //
        // this.stopMarkers.forEach(stopMarker => stopMarker.removeFrom(this.map));
        //
        // this.tripService.getTrip(metadata.tripId).subscribe(response => {
        //     const fontAwesomeIcon = L.divIcon({
        //         html: '<i class="fa fa-circle-o " style="background-color: white; border-radius: 2em;"></i>',
        //         className: 'stop-circle'
        //     });
        //
        //     const markers = [];
        //     for (let tripStop of response.tripStops) {
        //         const marker = L.marker([tripStop.stopLat, tripStop.stopLon], {icon: fontAwesomeIcon});
        //         marker.on('click', (e) => {
        //             const stop = new Stop();
        //             stop.id = tripStop.stopId;
        //             stop.stopId = tripStop.stopId;
        //             stop.stopName = tripStop.stopName;
        //             stop.stopLat = tripStop.stopLat;
        //             stop.stopLon = tripStop.stopLon;
        //
        //             this.flyTo(stop.stopLat, stop.stopLon);
        //
        //             this.currentStop = stop;
        //             this.timetableService.getTimetable(stop.stopId).subscribe(timetables => this.currentTimetables = timetables);
        //         });
        //         marker.addTo(this.map);
        //         const tooltip = marker.bindTooltip(`<div>${tripStop.stopName}</div>`, {
        //             direction: 'right',
        //             permanent: true,
        //             sticky: false,
        //             className: 'leaflet-tooltip-igeolab'
        //         });
        //         tooltip.on('click', function (e) {
        //             console.log("clicked", JSON.stringify(e.target.getLatLng()));
        //         });
        //         tooltip.openTooltip();
        //         markers.push(marker);
        //     }
        //     this.tripMarkers.forEach(marker => marker.removeFrom(this.map));
        //     this.tripMarkers = markers;
        // });
        //
        // this.flyTo(this.currentStop?.stopLat || null, this.currentStop?.stopLon || null);
        //
        // const shape: Shape[] = this.currentTimetables?.shapes[metadata.shapeId] || [];
        // const points: LatLng[] = shape
        //     .sort((prev: Shape, curr: Shape) => prev.shapePtSequence - curr.shapePtSequence)
        //     .map(point => new L.LatLng(point.shapePtLat, point.shapePtLon));
        //
        // const polyline = new L.Polyline(points, {
        //     color: '#0074c1',
        //     smoothFactor: 1,
        //     weight: 8,
        //     opacity: 0.9
        // });
        //
        // if (this.polyline != null) {
        //     polyline.addTo(this.map);
        //     this.polyline.removeFrom(this.map);
        //     this.polyline = polyline;
        // } else {
        //     polyline.addTo(this.map);
        //     this.polyline = polyline;
        // }
    }

    public flyTo(lat: number | null, lng: number | null) {
        if (lat != null && lng != null) {
            this.map.flyTo([lat, lng - 0.01], 14);
        }
    }

    public clearTripStopMarkers() {
        this.tripMarkers.forEach(marker => marker.removeFrom(this.map));
        this.tripMarkers = [];
    }

    private initHideBikeStation(map: Map): void {
        map.on('zoomend', (event: LeafletEvent) => {
            if (map.getZoom() < 15) {
                for (let marker of this.bikeStationMarkers) {
                    marker.removeFrom(map);
                }
                this.bikeStationMarkers = [];
            } else {
                if (this.bikeStationMarkers.length == 0) {
                    this.getBikeStationStatus(map);
                }
            }
        });
    }

    public planHasBeenClicked(event: boolean) {
        this.showPlanDetails = event;
    }
}
