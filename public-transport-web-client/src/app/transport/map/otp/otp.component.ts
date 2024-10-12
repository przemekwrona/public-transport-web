import {Component, Input, OnDestroy} from '@angular/core';
import * as L from 'leaflet';
import {DivIcon, LatLng, LatLngBounds, Map, Marker, polyline, Polyline} from "leaflet";
import {OtpService} from "../../../http/otp.service";
import moment from "moment";
import {decode} from "@googlemaps/polyline-codec";
import {Itinerary, Leg, RoutesResponse} from "../../../generated";
import {ItineraryManagerService} from "../service/itinerary-manager.service";
import {JourneySummaryService} from "../../../http/journey-summary.service";
import {MatDialog} from "@angular/material/dialog";
import {SummaryJourneyComponent} from "./summary-journey/summary-journey.component";
import {FormControl} from "@angular/forms";

export class OtpPoint {
    name: string = '';
    lat: number;
    lon: number;
}

@Component({
    selector: 'app-otp',
    templateUrl: './otp.component.html',
    styleUrl: './otp.component.scss'
})
export class OtpComponent implements OnDestroy {
    public myControl = new FormControl('');

    @Input() startPoint: OtpPoint;
    @Input() map: Map;

    private _endPoint: OtpPoint;

    @Input() set endPoint(endPoint: OtpPoint) {
        this._endPoint = endPoint;
        if (this.wasPlanned) {
            this.plan();
        }
    }

    get endPoint(): OtpPoint {
        return this._endPoint;
    }

    public routeResponse: RoutesResponse | null;
    public bikeResponse: RoutesResponse | null;
    public panelOpenState: boolean = false;

    public walkItinerary: Itinerary | null = null;
    public bikeItinerary: Itinerary | null = null;

    public wasPlanned: boolean = false;

    private walkPolyline: Polyline;
    private bikePolyline: Polyline[] = [];
    public isExpanded: boolean = true;

    constructor(private otpService: OtpService, private itineraryManagerService: ItineraryManagerService, private journeySummaryService: JourneySummaryService, private mdatDialog: MatDialog) {
    }

    public plan(): void {
        this.wasPlanned = true;
        this.otpService.plan(this.startPoint.lat, this.startPoint.lon, this.endPoint.lat, this.endPoint.lon, moment()).subscribe(routeResponse => {
            this.routeResponse = routeResponse;
            this.walkItinerary = this.getWalkItinerary(routeResponse)

            if (this.wasPlanned) {
                const itinerary = (routeResponse.plan?.itineraries || [])
                    .filter(itinerary => !this.isWalk(itinerary));

                if (itinerary != null) {
                    this.showItineraryOnMap((routeResponse.plan?.itineraries || [])[1])
                }
            }
        });
        this.otpService.planBike(this.startPoint.lat, this.startPoint.lon, this.endPoint.lat, this.endPoint.lon, moment()).subscribe(bikeResponse => {
            this.bikeResponse = bikeResponse;
            this.bikeItinerary = this.getBikeItinerary(bikeResponse);
        });
    }

    public summary(): void {
        this.journeySummaryService.summary(this.startPoint.lat, this.startPoint.lon, this.endPoint.lat, this.endPoint.lon, moment()).subscribe(summaryResponse => {
            this.mdatDialog.open(SummaryJourneyComponent, {data: summaryResponse});
        });
    }

    public hasPlan(): boolean {
        return this.routeResponse != null;
    }

    public isItinerary(itinerary: Itinerary): boolean {
        return (itinerary.legs || []).length > 1;
    }

    public isWalk(itinerary: Itinerary): boolean {
        return (itinerary.legs || []).length == 1 && (itinerary.legs || [])[0]?.mode == 'WALK';
    }

    public expand(): void {
        this.isExpanded = true;
    }

    public collapse(): void {
        this.isExpanded = false;
    }

    public getPublicTransportItinerary(): Itinerary[] {
        return (this.routeResponse?.plan?.itineraries || [])
            .filter((itinerarie: Itinerary) => (itinerarie.legs || [])?.length != (itinerarie.legs || []).filter((leg: Leg) => leg.mode === 'WALK')?.length);
    }

    public showItineraryWalkMap(): void {
        if (this.walkItinerary != null) {
            this.itineraryManagerService.showWalkOnMap(this.walkItinerary);
        }
    }

    public hideItineraryWalkMap(): void {
        if (this.walkItinerary != null) {
            this.itineraryManagerService.hideWalkOnMap();
        }
    }

    public showItineraryBikeMap(): void {
        this.hideItineraryBikeMap();
        this.bikePolyline = (this.bikeItinerary?.legs || [])
            .map(leg => {
                let config = leg?.mode === 'BICYCLE' ?
                    {
                        weight: 8,
                        color: "#2790FF"
                    } :
                    {
                        weight: 8,
                        color: "#181818",
                        dashArray: "1 15"
                    };

                return L.polyline(decode(leg.legGeometry?.points || '', 5), config)
            });

        this.bikePolyline.forEach(polyline => polyline.addTo(this.map));
    }

    public hideItineraryBikeMap(): void {
        this.bikePolyline.forEach(polyline => polyline.removeFrom(this.map));
    }

    public showItineraryOnMap(itinerary: Itinerary): void {
        this.itineraryManagerService.showPublicTransportOnMap(itinerary);
    }

    public hideItineraryOnMap(): void {
        if (!this.isExpanded) {
            this.itineraryManagerService.hidePublicTransportOnMap();
        }
    }

    public zoomToItinerary(): void {
        this.itineraryManagerService.zoomToPublicTransportItinerary();
    }

    public scrollToTop(index: number): void {
        let el = document.getElementById(`mat-expansion-panel-header-${index}`);
        if (el != null) {
            el.scrollIntoView({behavior: "smooth", block: "start", inline: "start"});
        }
    }

    public getBikeItinerary(bikeResponse: RoutesResponse): Itinerary {
        if (bikeResponse?.plan?.itineraries != undefined
            && bikeResponse?.plan?.itineraries.length > 0) {
            return bikeResponse?.plan?.itineraries[0] || {};
        }
        return {};
    }

    public getWalkItinerary(routesResponse: RoutesResponse): Itinerary {
        return (routesResponse?.plan?.itineraries || []).filter((itinerary: Itinerary) => this.isWalk(itinerary))[0] || {};
    }

    ngOnDestroy(): void {
        console.log("On destroy");
    }

}
