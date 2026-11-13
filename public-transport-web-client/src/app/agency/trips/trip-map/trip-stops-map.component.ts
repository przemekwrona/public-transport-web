import {AfterViewInit, ChangeDetectionStrategy, Component, ElementRef, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {ViewportScroller} from "@angular/common";
import {Subscription} from "rxjs";
import * as L from "leaflet";
import {LeafletMouseEvent, Map, Marker, Polyline, Popup} from "leaflet";
import {Point2D, Stop} from "../../../generated/public-transport-api";
import {AgencyStorageService} from "../../../auth/agency-storage.service";
import {AgencyStopsService} from "../../../generated/public-transport-api";
import {TripStopListStore} from "./trip-stop-list.store";

@Component({
    selector: "app-trip-stops-map",
    templateUrl: "./trip-stops-map.component.html",
    styleUrl: "./trip-stops-map.component.scss",
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
export class TripStopsMapComponent implements OnInit, AfterViewInit, OnDestroy {
    @ViewChild("mapHost", {static: true}) mapHost!: ElementRef<HTMLDivElement>;

    private readonly bdot10kStop = L.divIcon({
        html: `<div style="background-color: #0096FF; padding: 1px 0 0 1px; width: 20px; height: 20px; border-radius: 2px; color: whitesmoke"><img src="assets/bus-solid.svg"></div>`,
        className: "stop-marker"
    });

    private readonly otpStop = L.divIcon({
        html: `<div style="background-color: #00395c; padding: 1px 0 0 1px; width: 20px; height: 20px; border-radius: 2px; color: whitesmoke"><img src="assets/bus-solid.svg"></div>`,
        className: "stop-marker"
    });

    private map?: Map;
    private stopMarkers: Marker[] = [];
    private routePolyline?: Polyline;
    private popup?: Popup;
    private resizeObserver?: ResizeObserver;
    private viewFitted = false;
    private zooming = false;
    private lastWidth = 0;
    private lastHeight = 0;
    private readonly subscriptions = new Subscription();

    constructor(
        readonly store: TripStopListStore,
        private agencyStopsService: AgencyStopsService,
        private agencyStorageService: AgencyStorageService,
        private viewportScroller: ViewportScroller
    ) {
    }

    ngOnInit(): void {
        this.subscriptions.add(this.store.geometryChange.subscribe(geometry => this.drawPolyline(geometry)));
        this.subscriptions.add(this.store.focus$.subscribe(center => this.focus(center)));
    }

    ngAfterViewInit(): void {
        this.map = this.initMap();
        this.drawPolyline(this.store.geometry || []);
        this.reloadStops(this.map);
        this.map.on("zoomstart", () => this.zooming = true);
        this.map.on("zoomend", () => this.zooming = false);
        this.map.on("moveend", () => this.reloadStops(this.map!));
        this.resizeObserver = new ResizeObserver(() => this.onContainerResize());
        this.resizeObserver.observe(this.mapHost.nativeElement);
    }

    ngOnDestroy(): void {
        this.resizeObserver?.disconnect();
        this.subscriptions.unsubscribe();
        this.map?.remove();
    }

    zoom(): void {
        this.viewFitted = false;
        this.onContainerResize();
    }

    private onContainerResize(): void {
        if (!this.map || this.zooming) {
            return;
        }
        const host = this.mapHost.nativeElement;
        const width = host.clientWidth;
        const height = host.clientHeight;
        if (width === this.lastWidth && height === this.lastHeight) {
            return;
        }
        this.lastWidth = width;
        this.lastHeight = height;
        if (width === 0 || height === 0) {
            return;
        }
        this.map.invalidateSize({animate: false});
        if (this.viewFitted) {
            return;
        }
        this.viewFitted = true;
        if (this.routePolyline?.getBounds().isValid()) {
            this.map.fitBounds(this.routePolyline.getBounds(), {animate: false, padding: [24, 24]});
            return;
        }
        if (this.store.focusTarget) {
            this.map.setView(this.store.focusTarget, 15, {animate: false});
        }
    }

    private initMap(): Map {
        const map = L.map(this.mapHost.nativeElement, {
            center: this.store.initialCenter,
            zoom: this.store.initialZoom,
            zoomControl: false,
            fadeAnimation: false
        });

        L.control.zoom({
            position: "bottomright"
        }).addTo(map);

        L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
            maxZoom: 18,
            minZoom: 3,
            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        }).addTo(map);

        return map;
    }

    private focus(center: [number, number]): void {
        if (!this.map) {
            return;
        }
        this.map.invalidateSize();
        this.map.setView(center, 15, {animate: false});
    }

    private drawPolyline(geometry: Point2D[]): void {
        if (!this.map) {
            return;
        }
        const latLngPoints = (geometry || []).map(point => new L.LatLng(point.lat || 0.0, point.lon || 0.0));
        const polyline = L.polyline(latLngPoints, {
            color: "#416AB6",
            weight: 8,
            opacity: 0.9,
            smoothFactor: 1
        });
        this.routePolyline?.removeFrom(this.map);
        this.routePolyline = polyline;
        polyline.addTo(this.map);
    }

    private reloadStops(map: Map): void {
        if (map.getZoom() > 12) {
            const bounds = map.getBounds();
            this.agencyStopsService.getAgencyStopsInArea(
                this.agencyStorageService.getInstance(),
                bounds.getNorth(),
                bounds.getWest(),
                bounds.getSouth(),
                bounds.getEast()
            ).subscribe(response => {
                const stopMarkers: Marker[] = response.stops?.map((stop: Stop) => L.marker([stop?.lat || 0.0, stop?.lon || 0.0], {icon: stop.isBdot10k ? this.bdot10kStop : this.otpStop})
                    .on("click", (event: LeafletMouseEvent) => {
                        L.DomEvent.stopPropagation(event);
                        this.store.addStop(stop);
                        this.store.forceRefreshIn10seconds();
                        this.store.approximateDistances();
                        this.viewportScroller.scrollToAnchor("map");
                    })
                    .on("mouseover", (event: LeafletMouseEvent) => {
                        this.popup = L.popup({closeButton: false})
                            .setLatLng(event.latlng)
                            .setContent(stop.name)
                            .openOn(map);
                    })
                    .on("mouseout", () => {
                        if (this.popup) {
                            this.popup.closePopup();
                            this.popup.removeFrom(map);
                        }
                    })) || [];

                stopMarkers.forEach(marker => marker.addTo(map));
                for (const marker of this.stopMarkers) {
                    marker.removeFrom(map);
                }
                this.stopMarkers = stopMarkers;
            });
        } else {
            this.stopMarkers.forEach(marker => marker.removeFrom(map));
            this.stopMarkers = [];
        }
    }
}
