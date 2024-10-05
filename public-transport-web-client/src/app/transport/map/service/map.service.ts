import {Injectable} from '@angular/core';
import * as L from "leaflet";
import {Map} from "leaflet";
import {CityManagerService} from "./city-manager.service";

@Injectable({
    providedIn: 'root'
})
export class MapService {

    private ZOOM: number = 16;
    private map: Map;

    constructor(private cityManagerService: CityManagerService) {
    }

    public initMap(): Map {
        const city: string = this.cityManagerService.getCurrentCity();

        this.map = L.map('map', {
            center: [52.23210, 21.00585],
            zoom: this.ZOOM,
            zoomControl: false
        });

        L.control.zoom({
            position: 'bottomright'
        }).addTo(this.map);

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 18,
            minZoom: 3,
            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        }).addTo(this.map);

        return this.map;
    }

    public getMap(): Map {
        return this.map;
    }

    public getZoom(): number {
        if (this.map == null) {
            return 15;
        }
        return this.map.getZoom();
    }
}
