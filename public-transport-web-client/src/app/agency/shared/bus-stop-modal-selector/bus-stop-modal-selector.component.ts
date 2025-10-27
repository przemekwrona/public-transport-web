import {AfterViewInit, Component, inject, model} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {BusStopSelectorData} from "../bus-stop-selector/bus-stop-selector.component";
import {LeafletEvent, LeafletMouseEvent, Map, Marker} from "leaflet";
import * as L from "leaflet";
import {Stop, StopsResponse, StopsService} from "../../../generated/public-transport-api";
import {find} from "lodash";

interface StopMarker extends L.Marker {
    id: number;
}

@Component({
    selector: 'app-bus-stop-modal-selector',
    templateUrl: './bus-stop-modal-selector.component.html',
    styleUrl: './bus-stop-modal-selector.component.scss'
})
export class BusStopModalSelectorComponent implements AfterViewInit {
    private ICON = L.divIcon({
        html: `<div style="background-color: #0096FF; padding: 1px 0 0 1px; width: 20px; height: 20px; border-radius: 2px; color: whitesmoke"><img src="assets/bus-solid.svg"></div>`,
        className: 'stop-marker'
    });

    readonly dialogRef = inject(MatDialogRef<BusStopModalSelectorComponent>);
    readonly data = inject<BusStopSelectorData>(MAT_DIALOG_DATA);
    readonly stopId = model(this.data.stopId);
    readonly stopName = model(this.data.stopName);

    private map: Map;
    private stopMarkers: Marker[] = [];
    public stops: Stop[] = [];

    constructor(private stopsService: StopsService) {
    }

    ngAfterViewInit(): void {
        this.map = this.initMap();
        this.reloadStops(this.map);
        this.onMoveEnd(this.map);
        this.onZoomEnd(this.map);
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
                    const stopMarker: StopMarker = L.marker([stop?.lat || 0.0, stop?.lon || 0.0], {
                        icon: this.ICON,
                        title: stop.name
                    }) as StopMarker
                    stopMarker.id = stop.id;
                    return stopMarker;
                }) || []
                stopMarkers.forEach(marker => marker.on('click', (event: LeafletMouseEvent) => {
                    const lastClickedStop: Stop = find(this.stops, {id: event.target.id});

                    const selectedStop: BusStopSelectorData = {} as BusStopSelectorData;
                    selectedStop.stopId = lastClickedStop.id;
                    selectedStop.stopName = lastClickedStop.name;
                    selectedStop.stopLat = lastClickedStop.lat;
                    selectedStop.stopLon = lastClickedStop.lon;

                    this.closeModal(selectedStop);

                }));
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

    private initMap(): Map {
        const map: Map = L.map('map-stops', {
            center: [this.data.stopLat || 52.2321, this.data.stopLon || 20.0559],
            // center: [52.2321, 20.0559],
            // zoom: 7,
            zoom: 17,
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

    closeModal(selectedStop: BusStopSelectorData) {
        this.dialogRef.close(selectedStop);
    }

}
