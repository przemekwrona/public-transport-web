import {NgTemplateOutlet} from '@angular/common';
import {Component, ElementRef, inject, ChangeDetectionStrategy} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {FormatSecondsPipe} from "../trip-variant-select/format-seconds.pipe";
import {
    MAT_DIALOG_DATA,
    MatDialogActions,
    MatDialogClose,
    MatDialogContent, MatDialogRef,
    MatDialogTitle
} from "@angular/material/dialog";
import {
    GetAllTripsResponse,
    ProfileShortcut,
    RouteDetails,
    RouteId,
    RouteService,
    Trip,
    TripId1,
    TripService
} from "../../../../generated/public-transport-api";

import {FormBuilder, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatButton} from "@angular/material/button";
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {Select} from 'primeng/select';
import {catchError, distinctUntilChanged, map, of, switchMap} from 'rxjs';
import {AgencyStorageService} from "../../../../auth/agency-storage.service";
import {NotificationService} from "../../../../shared/notification.service";
import {RouteNameNormPipe} from "../../../timetable/create-timetable/route-id-normalization.pipe";
import moment from "moment";

interface RouteOption {
    routeId: RouteId;
    line: string;
    name: string;
}

export interface OnTimeRangeAndTripSelected {
    start: string
    end: string
    resourceId: string
    tripId: TripId1
    origin: string
    destination: string
}

export interface TripProfileChoice {
    trip: Trip;
    profile: ProfileShortcut;
}

@Component({
    selector: 'app-on-time-range-selected-modal',
    imports: [
    NgTemplateOutlet,
    FormatSecondsPipe,
    RouteNameNormPipe,
    Select,
    TranslocoPipe,
    MatDialogContent,
    MatDialogTitle,
    FormsModule,
    MatButton,
    MatDialogActions,
    MatDialogClose,
    ReactiveFormsModule
],
    templateUrl: './on-time-range-selected-modal.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './on-time-range-selected-modal.component.scss'
})
export class OnTimeRangeSelectedModalComponent {

    private formBuilder: FormBuilder = inject(FormBuilder);

    data = inject<{ start: string, end: string, resourceId: string, defaultRoutes: GetAllTripsResponse }>(MAT_DIALOG_DATA);

    modelForm = this.formBuilder.group({
        tripId: this.formBuilder.control<TripId1 | null>(null, [Validators.required]),
        extraRouteId: this.formBuilder.control<RouteId | null>(null)
    });

    showRoutePicker = false;
    loadingExtraRoute = false;
    loadingRoutes = false;
    extraLines: RouteDetails[] = [];
    routeOptions: RouteOption[] = [];
    routeOverlayAppendTo: HTMLElement = document.body;
    routeOverlayStyle: Record<string, string> = {zIndex: '11000', pointerEvents: 'auto'};

    private selectedTrip: Trip | null = null;
    private selectedProfile: ProfileShortcut | null = null;
    private notificationService = inject(NotificationService);
    private transloco = inject(TranslocoService);
    private elementRef = inject(ElementRef<HTMLElement>);

    constructor(private dialogRef: MatDialogRef<OnTimeRangeSelectedModalComponent>, private agencyStorageService: AgencyStorageService, private tripService: TripService, private routeService: RouteService) {
        this.modelForm.controls.extraRouteId.valueChanges.pipe(
            distinctUntilChanged((left, right) => left?.routeCode === right?.routeCode),
            switchMap(routeId => this.loadExtraRoute(routeId)),
            takeUntilDestroyed()
        ).subscribe(lines => {
            this.extraLines = lines;
            this.loadingExtraRoute = false;
        });
    }

    get defaultLines(): RouteDetails[] {
        return this.data.defaultRoutes?.lines ?? [];
    }

    openRoutePicker(): void {
        this.routeOverlayAppendTo = this.dialogOverlayHost();
        this.showRoutePicker = true;
        if (this.routeOptions.length === 0 && !this.loadingRoutes) {
            this.loadRouteOptions();
        }
    }

    placeRouteOverlay(): void {
        this.pinRouteOverlay();
        requestAnimationFrame(() => this.pinRouteOverlay());
    }

    trackRoute(route: RouteDetails): string {
        return this.routeCodeOf(route) ?? '';
    }

    public selectProfile(choice: TripProfileChoice): void {
        this.selectedTrip = choice.trip;
        this.selectedProfile = choice.profile;
        this.modelForm.patchValue({
            tripId: {
                ...choice.trip.tripId,
                trafficMode: choice.profile.trafficMode
            }
        });

        this.selectTrip();
    }

    public selectTrip() {
        const tripId = this.modelForm.controls.tripId.value;
        if (!tripId) {
            return;
        }

        const selectedTrip = this.selectedTrip?.tripId?.tripCode === tripId.tripCode
            ? this.selectedTrip
            : this.findTripByTripId(tripId);
        const travelTime = (this.selectedTrip?.tripId?.tripCode === tripId.tripCode
                ? this.selectedProfile?.travelTime
                : undefined)
            ?? selectedTrip?.travelTimeInSeconds
            ?? 0;

        if (travelTime > 0 || selectedTrip) {
            this.closeWithSelection(tripId, selectedTrip, travelTime);
            return;
        }

        const instance = this.agencyStorageService.getInstance();
        const routeCode = tripId.routeId?.routeCode;
        const tripCode = tripId.tripCode;
        this.tripService.getTripVariantDetails(instance, routeCode, tripCode).subscribe(tripDetails => {
            const lastStop = tripDetails?.tripProfiles[0]?.stops?.reduce((curr, next) =>
                curr.calculatedSeconds > next.calculatedSeconds ? curr : next);

            this.closeWithSelection(tripId, selectedTrip, lastStop?.calculatedSeconds ?? 0, tripDetails?.tripId);
        });
    }

    private closeWithSelection(tripId: TripId1, trip: Trip | null, travelTime: number, fallbackTripId?: TripId1): void {
        const results: OnTimeRangeAndTripSelected = {} as OnTimeRangeAndTripSelected;
        results.start = this.data.start;
        results.end = moment(this.data.start).add(travelTime, 'seconds').format("yyyy-MM-DDTHH:mm:SS");
        results.resourceId = this.data.resourceId;
        results.tripId = tripId ?? fallbackTripId;
        results.origin = trip?.origin;
        results.destination = trip?.destination;
        this.dialogRef.close(results);
    }

    private findTripByTripId(tripId: TripId1): Trip | null {
        for (const route of [...this.defaultLines, ...this.extraLines]) {
            const trip = route.trips?.find(item => item.tripId?.tripCode === tripId.tripCode);
            if (trip) {
                return trip;
            }
        }
        return null;
    }

    private loadRouteOptions(): void {
        this.loadingRoutes = true;
        this.routeService.getRoutes(this.agencyStorageService.getInstance()).subscribe({
            next: response => {
                this.routeOptions = (response?.items ?? []).flatMap(route => {
                    const routeId = route.routeId;
                    if (!routeId?.routeCode) {
                        return [];
                    }
                    return [{
                        routeId,
                        line: routeId.line ?? '',
                        name: (routeId.name ?? '').replaceAll('_', ' ')
                    }];
                });
                this.loadingRoutes = false;
            },
            error: () => {
                this.routeOptions = [];
                this.loadingRoutes = false;
                this.notificationService.showError(this.transloco.translate('agency.brigade.loadMoreRoutes.failed'));
            }
        });
    }

    private loadExtraRoute(routeId: RouteId | null) {
        const routeCode = routeId?.routeCode;
        if (!routeCode || this.isDefaultRoute(routeCode)) {
            return of([] as RouteDetails[]);
        }

        this.loadingExtraRoute = true;
        return this.tripService.getTripsByRouteAndFilterLineOrName(this.agencyStorageService.getInstance(), routeCode).pipe(
            map(response => (response?.lines ?? []).filter(line => !this.isDefaultRoute(this.routeCodeOf(line)))),
            catchError(() => {
                this.notificationService.showError(this.transloco.translate('agency.brigade.loadMoreRoutes.failed'));
                return of([] as RouteDetails[]);
            })
        );
    }

    private pinRouteOverlay(): void {
        const overlay = document.querySelector('.p-overlay:has(.route-select-panel)') as HTMLElement | null;
        const field = document.getElementById('extraRoute')?.closest('.p-select') as HTMLElement | null;
        if (!overlay || !field) {
            return;
        }

        const rect = field.getBoundingClientRect();
        const panelHeight = overlay.getBoundingClientRect().height;
        const spaceBelow = window.innerHeight - rect.bottom;
        const openAbove = panelHeight > spaceBelow - 8 && rect.top > spaceBelow;
        const top = openAbove ? Math.max(8, rect.top - panelHeight - 4) : rect.bottom + 4;
        const host = this.dialogOverlayHost();
        if (overlay.parentElement !== host) {
            host.appendChild(overlay);
        }

        const next: Record<string, string> = {
            position: 'fixed',
            top: `${top}px`,
            left: `${rect.left}px`,
            insetInlineStart: `${rect.left}px`,
            marginTop: '0',
            zIndex: '11000',
            pointerEvents: 'auto',
            minWidth: `${rect.width}px`
        };

        overlay.style.position = next['position'];
        overlay.style.top = next['top'];
        overlay.style.left = next['left'];
        overlay.style.insetInlineStart = next['insetInlineStart'];
        overlay.style.marginTop = next['marginTop'];
        overlay.style.zIndex = next['zIndex'];
        overlay.style.pointerEvents = next['pointerEvents'];
        overlay.style.minWidth = next['minWidth'];
        overlay.classList.add('route-select-ready');

        if (this.routeOverlayStyle['top'] === next['top']
            && this.routeOverlayStyle['left'] === next['left']
            && this.routeOverlayStyle['minWidth'] === next['minWidth']) {
            return;
        }
        this.routeOverlayStyle = next;
    }

    private dialogOverlayHost(): HTMLElement {
        return this.elementRef.nativeElement.closest('.cdk-overlay-popover')
            ?? document.querySelector('.cdk-overlay-popover')
            ?? document.body;
    }

    private isDefaultRoute(routeCode: string | undefined): boolean {
        return !!routeCode && this.defaultLines.some(line => this.routeCodeOf(line) === routeCode);
    }

    private routeCodeOf(line: RouteDetails): string | undefined {
        return line.route?.routeCode ?? line.route?.routeId?.routeCode;
    }

}
