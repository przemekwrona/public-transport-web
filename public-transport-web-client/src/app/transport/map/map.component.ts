import {AfterViewInit, Component} from '@angular/core';
import * as L from 'leaflet';
import {LatLng, LatLngBounds, LeafletEvent, LeafletMouseEvent, Map, Marker, Polyline} from "leaflet";
import {StopService} from "../../http/stop.service";
import {Shape, Timetables, TimetableService} from "../../http/timetable.service";
import {TripService} from "../../http/trip.service";
import {DepartureMetadata} from "./timetable-content/timetable-content.component";
import {OtpPoint} from "./otp/otp.component";
import {OtpService} from "../../http/otp.service";
import {Route, Stop, StopDetails, StopTime} from "../../generated";
import moment from "moment";
import {GbfsService} from "../../http/gbfs.service";
import {v3} from "gbfs-typescript-types";
import {Station} from "gbfs-typescript-types/v3.0/station_information";
import {interval, Observable, timer} from "rxjs";

@Component({
    selector: 'app-map',
    templateUrl: './map.component.html',
    styleUrl: './map.component.scss'
})
export class MapComponent implements AfterViewInit {
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

    constructor(private stopService: StopService, private timetableService: TimetableService, private tripService: TripService, private otpService: OtpService, private gbfsService: GbfsService) {
        this.getStops(52.240, 20.890, 52.220, 21.120);
    }

    ngAfterViewInit(): void {

        this.map = L.map('map', {
            center: [52.23210, 21.00585],
            zoom: this.ZOOM,
            zoomControl: false
        });

        L.control.zoom({
            position: 'bottomright'
        }).addTo(this.map);


        this.initHideBikeStation(this.map);

        const tiles = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 18,
            minZoom: 3,
            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        });

        tiles.addTo(this.map);

        timer(0, 60 * 1000).subscribe(() => {
            if(this.map.getZoom() >= 15) {
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

        this.map.on('click', (event: LeafletMouseEvent) => {

            if (this.startPoint.name == '') {
                this.startPoint = new OtpPoint();
                this.startPoint.name = `${event.latlng.lat.toFixed(5)},${event.latlng.lng.toFixed(5)}`;
                this.startPoint.lat = event.latlng.lat;
                this.startPoint.lon = event.latlng.lng;
            } else {
                this.endPoint = new OtpPoint();
                this.endPoint.name = `${event.latlng.lat.toFixed(5)},${event.latlng.lng.toFixed(5)}`;
                this.endPoint.lat = event.latlng.lat;
                this.endPoint.lon = event.latlng.lng;
            }

            if (this.currentLine == null) {
                this.currentStop = null;
                this.polyline?.removeFrom(this.map);
                this.polyline = null;
                this.tripMarkers.forEach(tripMaker => tripMaker.removeFrom(this.map));
                this.tripMarkers = [];
            }
            this.currentLine = null;
        });

    }

    private getBikeStationStatus(map: Map) {
        this.gbfsService.getStationInformation('nextbike_vw').subscribe((stationInformation: { [station_id: string]: Station }) => {
            this.gbfsService.getStationStatus('nextbike_vw').subscribe((stationStatus: v3.StationStatus) => {
                const bikeStationMarkers: Marker[] = stationStatus.data.stations.map(stationStatus => {
                    let iconBike = L.divIcon({
                        // html: `<mat-icon matBadge="15">home</mat-icon>`,
                        html: `<div style="display: inline-flex; opacity: 0.5;"><img src="assets/warsaw/veturilo.png" width="20" height="20" style="border-radius: 4px"/><div class="num" style="background-color: #ED1C24;color:white;padding: 0 4px;border-radius: 12px;position: absolute;bottom: 4px;left: 14px;border: 1px solid red;font-size:0.5rem;">${stationStatus['num_bikes_available'] || 0}</div></div>`,
                        className: 'bike-station-marker'
                    });

                    if (stationStatus['num_bikes_available'] > 0) {
                        iconBike = L.divIcon({
                            html: `<div style="display: inline-flex"><img src="assets/warsaw/veturilo.png" width="20" height="20" style="border-radius: 4px"/><div class="num" style="background-color: #ED1C24;color:white;padding: 0 4px;border-radius: 12px;position: absolute;bottom: 4px;left: 14px;border: 1px solid red;font-size:0.5rem;">${stationStatus['num_bikes_available'] || 0}</div></div>`,
                            className: 'bike-station-marker'
                        });
                    }

                    if (stationStatus['num_bikes_available'] > 2) {
                        iconBike = L.divIcon({
                            html: `<div style="display: inline-flex"><img src="assets/warsaw/veturilo.png" width="20" height="20" style="border-radius: 4px"/><div class="num" style="background-color: yellow;padding: 0 4px;border-radius: 12px;position: absolute;bottom: 4px;left: 14px;border: 2px solid yellow;font-size:0.5rem;">${stationStatus['num_bikes_available'] || 0}</div></div>`,
                            className: 'bike-station-marker'
                        })
                    }

                    if (stationStatus['num_bikes_available'] > 4) {
                        iconBike = L.divIcon({
                            html: `<div style="display: inline-flex"><img src="assets/warsaw/veturilo.png" width="20" height="20" style="border-radius: 4px"/><div class="num" style="background-color: #2D9127;color:white;padding: 0 4px;border-radius: 12px;position: absolute;bottom: 4px;left: 14px;font-size:0.5rem;">${stationStatus['num_bikes_available'] || 0}</div></div>`,
                            className: 'bike-station-marker'
                        })
                    }


                    let stationMarker = L.marker([stationInformation[stationStatus.station_id].lat || 0.0, stationInformation[stationStatus.station_id].lon || 0.0], {icon: iconBike});
                    return stationMarker;
                });
                bikeStationMarkers.forEach(bikeStationMarker => bikeStationMarker.addTo(map));

                for (let marker of this.bikeStationMarkers) {
                    marker.removeFrom(map);
                }
                this.bikeStationMarkers = bikeStationMarkers;
            })
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
                    this.currentLine = null;

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

                    this.otpService.getStopTimes(stop.id || '', moment()).subscribe(stopTimes => this.currentStopTimes = stopTimes);
                    //     this.timetableService.getTimetable(stop.stopId).subscribe(timetables => this.currentTimetables = timetables);
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
                    this.getBikeStationStatus(this.map);
                }
            }
        });
    }
}
