import {AfterViewInit, Component, OnInit} from '@angular/core';
import * as L from 'leaflet';
import {CircleMarker, LatLng, LatLngBounds, LeafletEvent, LeafletMouseEvent, Map, Marker, Polyline} from "leaflet";
import {find} from "lodash";

import {CenterPoint, Stop, StopsResponse, StopsService} from "../../generated/public-transport";
import {AuthService} from "../../auth/auth.service";
import {ActivatedRoute} from "@angular/router";
import {CommonModule} from "@angular/common";
import {BusStopModalEditorComponent} from "../shared/bus-stop-modal-editor/bus-stop-modal-editor.component";
import {SharedModule} from "../shared/shared.module";

interface StopMarker extends L.Marker {
    id: number;
}

@Component({
    standalone: true,
    imports: [
        CommonModule,
        SharedModule
    ],
    providers: [
        AuthService
    ],
    selector: 'app-stops',
    templateUrl: './stops.component.html',
    styleUrl: './stops.component.scss'
})
export class StopsComponent implements OnInit, AfterViewInit {
    private ICON_BDOT = L.divIcon({
        html: `<div style="background-color: #0096FF; padding: 1px 0 0 1px; width: 20px; height: 20px; border-radius: 2px; color: whitesmoke"><img src="assets/bus-solid.svg"></div>`,
        className: 'stop-marker'
    });

    private ICON_OSM = L.divIcon({
        html: `<div style="background-color: #191970; padding: 1px 0 0 1px; width: 20px; height: 20px; border-radius: 2px; color: whitesmoke"><img src="assets/bus-solid.svg"></div>`,
        className: 'stop-marker'
    })

    private map: Map;
    private stopMarkers: Marker[] = [];

    public stops: Stop[] = [];
    public lastClickedStop: Stop = {} as Stop;

    constructor(private stopsService: StopsService, private authService: AuthService, private route: ActivatedRoute) {
    }

    public isAdmin(): boolean {
        return this.authService.hasRoleSuperAdmin();
    }

    ngAfterViewInit(): void {
        this.map = this.initMap();
        this.reloadStops(this.map);
        this.onZoomEnd(this.map);
        this.onMoveEnd(this.map);
    }

    ngOnInit(): void {
    }

    private initMap(): Map {
        const centerPoint: CenterPoint = this.route.snapshot.data['centerPoint'];
        const map: Map = L.map('map-stops', {
            center: [centerPoint.latitude, centerPoint.longitude],
            zoom: centerPoint.zoom,
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
        if (map.getZoom() > 11) {
            const bounds = map.getBounds();
            this.stopsService.getStopsInArea(bounds.getNorth(), bounds.getWest(), bounds.getSouth(), bounds.getEast() + 0.01).subscribe((response: StopsResponse) => {
                const stopMarkers: StopMarker[] = response.stops?.map(stop => {
                    const stopMarker: StopMarker = stop.isBdot10k
                        ? L.marker([stop?.lat || 0.0, stop?.lon || 0.0], {
                            icon: this.ICON_BDOT,
                            title: stop.name
                        }) as StopMarker
                        : L.marker([stop?.lat || 0.0, stop?.lon || 0.0], {
                            icon: this.ICON_OSM,
                            title: stop.name
                        }) as StopMarker;

                    stopMarker.id = stop.id;
                    return stopMarker;
                }) || []
                stopMarkers.forEach(marker => marker.on('click', (event: LeafletMouseEvent) => this.lastClickedStop = find(this.stops, {id: event.target.id})));
                stopMarkers.forEach(marker => marker.addTo(map));


                this.stops = response.stops || [];

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
}
