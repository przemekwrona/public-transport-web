import {ChangeDetectionStrategy, Component, OnInit, ViewEncapsulation} from "@angular/core";
import {HttpErrorResponse} from "@angular/common/http";
import {ActivatedRoute, Data} from "@angular/router";
import {FormGroup} from "@angular/forms";
import {map} from "rxjs";
import {
    ErrorResponse,
    RouteDetails,
    TrafficMode,
    TripProfileService,
    TripsDetails
} from "../../../generated/public-transport-api";
import {AgencyStorageService} from "../../../auth/agency-storage.service";
import {NotificationService} from "../../../shared/notification.service";
import {getRouteParam} from "../trip-list/trip-list.resolver";
import {TripStopListStore} from "./trip-stop-list.store";

@Component({
    selector: "app-trip-map",
    templateUrl: "./trip-map.component.html",
    styleUrl: "./trip-map.component.scss",
    changeDetection: ChangeDetectionStrategy.Eager,
    encapsulation: ViewEncapsulation.None,
    standalone: false,
    providers: [TripStopListStore]
})
export class TripMapComponent implements OnInit {
    routeCode = "";
    tripCode = "";
    routeLabel = "";
    sidebarOpen = true;
    isSaving = false;
    showStopErrors = false;
    readonly trafficMode = TrafficMode;

    constructor(
        readonly stops: TripStopListStore,
        private route: ActivatedRoute,
        private tripProfileService: TripProfileService,
        private agencyStorageService: AgencyStorageService,
        private notificationService: NotificationService
    ) {
    }

    ngOnInit(): void {
        this.routeCode = getRouteParam(this.route.snapshot, "routeCode") ?? "";
        this.tripCode = getRouteParam(this.route.snapshot, "tripCode") ?? "";
        this.stops.editMode = true;

        const routeDetails = this.route.pathFromRoot
            .map(parent => parent.snapshot.data["routeDetails"] as RouteDetails | undefined)
            .find(details => !!details?.route);
        const routeId = routeDetails?.route?.routeId;
        this.routeLabel = [routeId?.line, routeId?.name].filter(value => !!value).join(" ");

        this.route.data.pipe(map((data: Data) => data["trip"] as TripsDetails)).subscribe((trip: TripsDetails) => {
            this.stops.load(trip);
            if (!this.routeLabel) {
                const tripRouteId = trip.tripId?.routeId;
                this.routeLabel = [tripRouteId?.line, tripRouteId?.name].filter(value => !!value).join(" ");
            }
        });
    }

    get activeProfile(): FormGroup | null {
        return this.stops.getActiveProfile();
    }

    velocityError(profile: FormGroup, error: string): boolean {
        return this.showStopErrors && !!profile.get("calculatedCommunicationVelocity")?.hasError(error);
    }

    lastStopValue(profile: FormGroup, controlName: string): number {
        const stops = this.stops.getStops(profile);
        const last = stops.length > 0 ? stops.at(stops.length - 1) : null;
        return Number(last?.get(controlName)?.value ?? 0);
    }

    saveStopList(): void {
        this.showStopErrors = true;
        if (this.stops.profiles.invalid) {
            this.notificationService.showError("Formularz jest niepoprawny uzupełnij braki");
            return;
        }
        this.isSaving = true;
        this.stops.measureDistance().subscribe({
            next: () => {
                this.stops.clearPendingRefresh();
                const agency = this.agencyStorageService.getInstance();
                this.tripProfileService.updateTripProfiles(agency, this.routeCode, this.tripCode, {
                    tripProfiles: this.stops.buildTripProfiles(),
                    geometry: this.stops.geometry
                }).subscribe({
                    next: () => {
                        this.isSaving = false;
                        this.notificationService.showSuccess("Lista przystanków została zapisana");
                    },
                    error: (response: HttpErrorResponse) => this.showHttpError(response)
                });
            },
            error: (response: HttpErrorResponse) => this.showHttpError(response)
        });
    }

    private showHttpError(response: HttpErrorResponse): void {
        this.isSaving = false;
        const payload: ErrorResponse = response.error;
        this.notificationService.showError(payload?.message || payload?.errorCode);
    }
}
