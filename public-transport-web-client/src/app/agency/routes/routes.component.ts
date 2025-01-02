import {AfterViewInit, ChangeDetectionStrategy, Component} from '@angular/core';
import {LeafletEvent, LeafletMouseEvent, Map, Marker} from "leaflet";
import * as L from "leaflet";
import {StopService} from "../stops/stop.service";
import {Stop} from "../../generated/public-transport";

enum RouteComponentMode {
    OVERVIEW,
    EDIT
}

@Component({
    selector: 'app-routes',
    templateUrl: './routes.component.html',
    styleUrl: './routes.component.scss',
    changeDetection: ChangeDetectionStrategy.Default
})
export class RoutesComponent implements AfterViewInit {
    private ICON = L.divIcon({
        html: `<div style="background-color: #0096FF; padding: 1px 0 0 1px; width: 20px; height: 20px; border-radius: 2px; color: whitesmoke"><img src="assets/bus-solid.svg"></div>`,
        className: 'stop-marker'
    })

    private map: Map;
    private stopMarkers: Marker[] = [];
    private mode: RouteComponentMode = RouteComponentMode.OVERVIEW;

    public stops: Stop[] = [];

    constructor(private stopService: StopService) {
    }

    ngAfterViewInit(): void {
        this.map = this.initMap();
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
                        this.stops.push(stop);
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

    public clickEdit() {
        this.mode = RouteComponentMode.EDIT;
    }

    public routes = [{
        line: '201',
        variants: [
            {
                variant: 1,
                start: 'Kielce',
                end: 'Chmielnik',
            },
            {
                variant: 2,
                start: 'Chmielnik',
                end: 'Kielce',
            }
        ]
    },
        {
            line: '208',
            variants: [
                {
                    variant: 1,
                    start: 'Kielce',
                    end: 'Busko Zdrój',
                },
                {
                    variant: 2,
                    start: 'Kielce',
                    end: 'Chmielnik',
                },
                {
                    variant: 3,
                    start: 'Busko Zdoj',
                    end: 'Kielce',
                },
                {
                    variant: 4,
                    start: 'Chmielnik',
                    end: 'Kielce',
                }
            ]
        }]

}
