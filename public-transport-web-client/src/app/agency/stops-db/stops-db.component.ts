import {AfterViewInit, Component, NgZone, OnDestroy, OnInit, ChangeDetectionStrategy} from '@angular/core';
import * as L from 'leaflet';
import {CircleMarker, LatLng, LatLngBounds, LeafletEvent, LeafletMouseEvent, Map, Marker, Polyline} from "leaflet";
import {find} from "lodash";

import {AgencyStopsService, CenterPoint, Stop, StopsPatchRequest, StopsResponse, Territory} from "../../generated/public-transport-api";
import {LoginService} from "../../auth/login.service";
import {AgencyStorageService} from "../../auth/agency-storage.service";
import {ActivatedRoute, Router} from "@angular/router";

import {SharedModule} from "../shared/shared.module";
import {GoogleAnalyticsService} from "../../google-analytics.service";
import {TerritoryDetailsComponent} from "./territory-details/territory-details.component";

interface StopMarker extends L.Marker {
    id: number;
}

@Component({
    imports: [
    SharedModule,
    TerritoryDetailsComponent
],
    providers: [
        LoginService
    ],
    selector: 'app-stops-db',
    templateUrl: './stops-db.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './stops-db.component.scss'
})
export class StopsDbComponent implements OnInit, AfterViewInit, OnDestroy {
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
    public territories: Territory[] = [];

    public lastClickedStop: Stop = {} as Stop;
    public lastClickedStopTerritory: Territory = {} as Territory;
    public detailsOpen = false;

    private detailsResizeTimer = 0;
    private mapResizeSettled = false;
    private ignoreMapReload = false;

    constructor(private agencyStopsService: AgencyStopsService, private authService: LoginService, private agencyStorageService: AgencyStorageService, private router: Router, private route: ActivatedRoute, private googleAnalyticsService: GoogleAnalyticsService, private ngZone: NgZone) {
    }

    public onStopSaved(request: StopsPatchRequest): void {
        if (this.lastClickedStop?.id === request.id) {
            this.lastClickedStop.name = request.name;
        }
    }

    public isAdmin(): boolean {
        return this.authService.hasRoleSuperAdmin();
    }

    public hasSelectedStop(): boolean {
        return this.lastClickedStop?.id != null;
    }

    public closeDetails(): void {
        window.clearTimeout(this.detailsResizeTimer);
        const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
        if (reduceMotion || !this.detailsOpen) {
            this.finishDetailsClose();
            return;
        }

        this.ignoreMapReload = true;
        this.ngZone.runOutsideAngular(() => {
            requestAnimationFrame(() => this.playDetailsCloseAnimation());
        });
    }

    private playDetailsCloseAnimation(): void {
        const details = document.querySelector('.stops-details') as HTMLElement | null;
        const panel = document.querySelector('.stops-details-panel') as HTMLElement | null;
        const wide = window.matchMedia('(min-width: 1024px)').matches;
        const motion: KeyframeAnimationOptions = {
            duration: 420,
            easing: 'cubic-bezier(0.22, 1, 0.36, 1)',
            fill: 'forwards'
        };
        if (wide && details) {
            const current = details.getBoundingClientRect().width;
            details.animate([
                {flexBasis: `${current}px`, marginRight: '30px'},
                {flexBasis: '0px', marginRight: '0px'}
            ], motion);
            panel?.animate([
                {opacity: 1, transform: 'none'},
                {opacity: 0, transform: 'translateX(-1.5rem)'}
            ], motion);
        } else {
            panel?.animate([
                {opacity: 1, transform: 'none'},
                {opacity: 0, transform: 'translateY(-0.75rem)'}
            ], motion);
        }

        this.detailsResizeTimer = window.setTimeout(() => this.ngZone.run(() => this.finishDetailsClose()), 450);
    }

    private finishDetailsClose(): void {
        this.detailsOpen = false;
        this.lastClickedStop = {} as Stop;
        this.lastClickedStopTerritory = {} as Territory;
        this.mapResizeSettled = false;
        requestAnimationFrame(() => {
            document.querySelector('.stops-details')?.getAnimations().forEach(animation => animation.cancel());
            this.ignoreMapReload = true;
            this.map?.invalidateSize({animate: false, pan: false});
            this.ignoreMapReload = false;
            this.reloadStops(this.map);
        });
    }

    private openDetails(): void {
        const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
        this.mapResizeSettled = false;
        this.ignoreMapReload = true;
        if (reduceMotion) {
            this.detailsOpen = true;
            setTimeout(() => this.finishMapResize());
            return;
        }

        this.ngZone.runOutsideAngular(() => {
            requestAnimationFrame(() => this.playDetailsAnimation());
        });
    }

    private playDetailsAnimation(): void {
        const layout = document.querySelector('.stops-layout') as HTMLElement | null;
        const details = document.querySelector('.stops-details') as HTMLElement | null;
        const panel = document.querySelector('.stops-details-panel') as HTMLElement | null;
        if (!layout || !details) {
            this.ngZone.run(() => this.finishMapResize());
            return;
        }

        const wide = window.matchMedia('(min-width: 1024px)').matches;
        const target = Math.max(0, (layout.getBoundingClientRect().width - 30) / 3);
        const motion: KeyframeAnimationOptions = {
            duration: 420,
            easing: 'cubic-bezier(0.22, 1, 0.36, 1)',
            fill: 'forwards'
        };
        if (wide) {
            details.animate([
                {flexBasis: '0px', marginRight: '0px'},
                {flexBasis: `${target}px`, marginRight: '30px'}
            ], motion);
            panel?.animate([
                {opacity: 0, transform: 'translateX(-1.5rem)'},
                {opacity: 1, transform: 'none'}
            ], motion);
        } else {
            panel?.animate([
                {opacity: 0, transform: 'translateY(-0.75rem)'},
                {opacity: 1, transform: 'none'}
            ], motion);
        }
        this.ngZone.run(() => this.detailsOpen = true);

        this.detailsResizeTimer = window.setTimeout(() => this.ngZone.run(() => this.finishMapResize()), 450);
    }

    private finishMapResize(): void {
        if (this.mapResizeSettled) {
            return;
        }
        this.mapResizeSettled = true;
        document.querySelector('.stops-details')?.getAnimations().forEach(animation => animation.cancel());
        document.querySelector('.stops-details-panel')?.getAnimations().forEach(animation => animation.cancel());
        this.ignoreMapReload = true;
        this.map?.invalidateSize({animate: false, pan: false});
        this.ignoreMapReload = false;
        this.reloadStops(this.map);
    }

    ngAfterViewInit(): void {
        this.map = this.initMap();
        this.reloadStops(this.map);
        this.onZoomEnd(this.map);
        this.onMoveEnd(this.map);
        setTimeout(() => this.map.invalidateSize());
    }

    ngOnInit(): void {
        this.googleAnalyticsService.pageView(this.router.url);
    }

    ngOnDestroy(): void {
        window.clearTimeout(this.detailsResizeTimer);
    }

    private initMap(): Map {
        const centerPoint: CenterPoint = this.route.snapshot.data['centerPoint'];
        const map: Map = L.map('map-stops', {
            center: [centerPoint.latitude || 52.23043289212839, centerPoint.longitude || 21.01070162462291],
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
        if (this.ignoreMapReload) {
            return;
        }
        if (map.getZoom() > 11) {
            const bounds = map.getBounds();
            this.agencyStopsService.getAgencyStopsInArea(this.agencyStorageService.getInstance(), bounds.getNorth(), bounds.getWest(), bounds.getSouth(), bounds.getEast() + 0.01).subscribe((response: StopsResponse) => {
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
                stopMarkers.forEach(marker => marker.on('click', (event: LeafletMouseEvent) => {
                    this.ngZone.run(() => {
                        const opening = !this.hasSelectedStop();
                        this.lastClickedStop = find(this.stops, {id: event.target.id});
                        this.lastClickedStopTerritory = find(this.territories, {id: this.lastClickedStop.territory_osm_id});
                        if (opening) {
                            this.openDetails();
                        }
                    });
                }));
                stopMarkers.forEach(marker => marker.addTo(map));


                this.stops = response.stops || [];
                this.territories = response.territories || [];

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
