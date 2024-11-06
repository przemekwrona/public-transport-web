import {AfterViewInit, Component, OnInit} from '@angular/core';
import {StopService} from "../../http/stop.service";
import * as L from 'leaflet';
import {CircleMarker, LatLng, LatLngBounds, LeafletEvent, LeafletMouseEvent, Map, Marker, Polyline} from "leaflet";

@Component({
    selector: 'app-stops',
    templateUrl: './stops.component.html',
    styleUrl: './stops.component.scss'
})
export class StopsComponent implements OnInit, AfterViewInit {

    private map: Map;

    constructor(private stopService: StopService) {
    }

    ngAfterViewInit(): void {
        this.map = this.initMap();
        this.onZoomEnd(this.map);
    }

    ngOnInit(): void {
    }

    private initMap(): Map {
        const map: Map = L.map('map', {
            center: [52.2321, 20.0559],
            zoom: 7,
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

    private onZoomEnd(map: Map): void {
        map.on('zoomend', (event: LeafletEvent) => {
        });
    }
}
