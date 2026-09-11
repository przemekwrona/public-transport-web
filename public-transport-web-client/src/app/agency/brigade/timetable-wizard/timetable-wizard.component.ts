import {Component, CUSTOM_ELEMENTS_SCHEMA, OnInit, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, Router, RouterModule} from '@angular/router';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {NgxSpinnerModule, NgxSpinnerService} from 'ngx-spinner';
import {
    AvailableTripProfile,
    TimetableBoardComponent,
    TimetableBoardEvent
} from '../../timetable/create-timetable/timetable-board/timetable-board.component';
import {
    BrigadeGroupBody,
    BrigadeService,
    GetAllTripsResponse,
    GetBrigadeDetailsResponse,
    PutBrigadeEventBody,
    ResourceService,
    TrafficMode,
    Trip,
    TripId2,
    TripMode,
    TripResponse
} from '../../../generated/public-transport-api';
import {AgencyStorageService} from '../../../auth/agency-storage.service';
import {concatMap, finalize, from, of, switchMap, tap, toArray} from 'rxjs';

@Component({
    selector: 'app-timetable-wizard',
    imports: [
        CommonModule,
        RouterModule,
        ReactiveFormsModule,
        TimetableBoardComponent,
        NgxSpinnerModule
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    templateUrl: './timetable-wizard.component.html',
    styleUrl: './timetable-wizard.component.scss'
})
export class TimetableWizardComponent implements OnInit {

    public brigadeCode: string | null = null;
    public calendarSymbol: string | null = null;
    public brigadeDetails: GetBrigadeDetailsResponse | null = null;
    public isSubmitted: boolean = false;
    public tripResponse: TripResponse = {front: {}, back: {}};
    public defaultRoute: GetAllTripsResponse | null = null;
    public tripProfiles: { front: AvailableTripProfile[]; back: AvailableTripProfile[] } = {front: [], back: []};
    public formGroup: FormGroup;
    public frontDepartures: TimetableBoardEvent[] = [];
    public backDepartures: TimetableBoardEvent[] = [];
    public isGenerating = false;
    public totalRequests = 0;
    public completedRequests = 0;

    @ViewChild('frontBoard') frontBoard?: TimetableBoardComponent;
    @ViewChild('backBoard') backBoard?: TimetableBoardComponent;
    private readonly spinnerName = 'timetable-wizard';

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private formBuilder: FormBuilder,
        private resourceService: ResourceService,
        private brigadeService: BrigadeService,
        private agencyStorageService: AgencyStorageService,
        private spinner: NgxSpinnerService
    ) {
        this.formGroup = this.formBuilder.group({
            front: this.buildDirectionGroup(15),
            back: this.buildDirectionGroup(18)
        });
    }

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.brigadeCode = params.get('brigadeCode');
            this.calendarSymbol = params.get('calendarSymbol');
        });
        this.route.data.subscribe(data => {
            this.defaultRoute = data['defaultRoute'] ?? null;
            this.tripProfiles = this.buildTripProfiles(this.defaultRoute);
            this.brigadeDetails = data['brigade'] ?? null;
        });
    }

    public getFrontTimetable(): FormGroup {
        return this.formGroup.get('front') as FormGroup;
    }

    public getBackTimetable(): FormGroup {
        return this.formGroup.get('back') as FormGroup;
    }

    private buildDirectionGroup(interval: number): FormGroup {
        return this.formBuilder.group({
            startTime: ['06:00'],
            endTime: ['20:00'],
            interval,
            departures: this.formBuilder.array([])
        });
    }

    private buildTripProfiles(defaultRoute: GetAllTripsResponse | null): {
        front: AvailableTripProfile[];
        back: AvailableTripProfile[]
    } {
        const trips = (defaultRoute?.lines ?? []).flatMap(line => line.trips ?? []);
        return {
            front: this.mapTripProfiles(trips, TripMode.Front),
            back: this.mapTripProfiles(trips, TripMode.Back)
        };
    }

    private mapTripProfiles(trips: Trip[], variantMode: TripMode): AvailableTripProfile[] {
        return trips
            .filter(trip => (trip.tripId?.variantMode ?? trip.mode) === variantMode)
            .flatMap(trip => this.toAvailableTripProfiles(trip));
    }

    private toAvailableTripProfiles(trip: Trip): AvailableTripProfile[] {
        const routeCode = trip.tripId?.routeId?.routeCode ?? '';
        const tripCode = trip.tripId?.tripCode ?? '';
        const profiles = trip.profile?.length
            ? trip.profile
            : [{trafficMode: trip.trafficMode ?? TrafficMode.Normal}];

        return profiles.map(profile => ({
            routeCode,
            tripCode,
            trafficMode: profile.trafficMode ?? TrafficMode.Normal,
            isMainVariant: trip.isMainVariant ?? false,
            variantDesignation: trip.variantDesignation,
            variantDescription: trip.variantDescription,
            travelTimeInSeconds: profile.travelTime ?? trip.travelTimeInSeconds ?? 0
        }));
    }

    public generate() {
        const brigadeCode = this.brigadeCode;
        const symbol = this.calendarSymbol;

        this.frontDepartures = this.frontBoard?.getDepartures() ?? [];
        this.backDepartures = this.backBoard?.getDepartures() ?? [];

        const calendarCode = this.brigadeDetails?.brigade?.brigades
            ?.find(group => group.calendarSymbolId?.symbol === symbol)
            ?.calendarSymbolId?.calendarItemId?.code;

        if (!brigadeCode || !calendarCode || !symbol || this.isGenerating) {
            return;
        }

        const instance = this.agencyStorageService.getInstance();
        const departures = [
            ...this.frontDepartures.map(departure => ({departure, variantMode: TripMode.Front})),
            ...this.backDepartures.map(departure => ({departure, variantMode: TripMode.Back}))
        ];

        this.totalRequests = departures.length;
        this.completedRequests = 0;
        this.showSpinner();

        this.resourceService.deleteResource(instance, brigadeCode, calendarCode, symbol).pipe(
            switchMap(() => this.brigadeService.getCalendarSymbolBrigadeResources(
                instance, brigadeCode, calendarCode, symbol)),
            switchMap((group: BrigadeGroupBody) => {
                const resourceCode = group.brigadeResources?.[0]?.sequenceHex;
                if (!resourceCode) {
                    return of([]);
                }

                return from(departures).pipe(
                    concatMap(({departure, variantMode}) =>
                        this.createBrigadeEvent(instance, brigadeCode, calendarCode, symbol, resourceCode, departure, variantMode).pipe(
                            tap(() => this.completedRequests++)
                        )),
                    toArray()
                );
            }),
            finalize(() => this.hideSpinner())
        ).subscribe(() => {
            this.router.navigate(['/agency/brigades', brigadeCode, 'edit'], {
                queryParams: {symbol: this.calendarSymbol}
            }).then();
        });
    }

    private showSpinner(): void {
        this.isGenerating = true;
        this.spinner.show(this.spinnerName, {
            type: 'ball-scale-multiple',
            size: 'large',
            bdColor: 'rgba(51,51,51,0.8)',
            color: '#fff',
            fullScreen: true
        });
    }

    private hideSpinner(): void {
        this.isGenerating = false;
        this.spinner.hide(this.spinnerName);
    }

    private createBrigadeEvent(
        instance: string,
        brigadeCode: string,
        calendarCode: string,
        symbol: string,
        resourceCode: string,
        departure: TimetableBoardEvent,
        variantMode: TripMode
    ) {
        return this.brigadeService.getNextBrigadeEventSequence(
            instance, brigadeCode, calendarCode, symbol, resourceCode
        ).pipe(
            switchMap(sequence => {
                const trip = this.findTrip(departure.routeCode, departure.tripCode);
                const startSecond = this.timeToSeconds(departure.time);
                const travelTime = this.findTravelTime(departure, variantMode);
                const tripId: TripId2 = {
                    routeId: {
                        routeCode: departure.routeCode,
                        line: trip?.line ?? trip?.tripId?.routeId?.line,
                        name: trip?.name ?? trip?.tripId?.routeId?.name,
                        version: trip?.tripId?.routeId?.version
                    },
                    variantName: trip?.tripId?.variantName ?? trip?.variant,
                    variantMode,
                    trafficMode: departure.trafficMode,
                    tripCode: departure.tripCode
                };
                const body: PutBrigadeEventBody = {
                    startSecond,
                    endSecond: startSecond + travelTime,
                    line: tripId.routeId?.line ?? trip?.line,
                    name: tripId.routeId?.name ?? trip?.name,
                    sequence: sequence.sequence,
                    sequenceHex: sequence.sequenceHex,
                    tripId
                };

                return this.brigadeService.putBrigadeEvent(
                    instance, brigadeCode, calendarCode, symbol, resourceCode, body);
            })
        );
    }

    private findTrip(routeCode: string, tripCode: string): Trip | undefined {
        return (this.defaultRoute?.lines ?? [])
            .flatMap(line => line.trips ?? [])
            .find(trip =>
                (trip.tripId?.routeId?.routeCode ?? '') === routeCode
                && (trip.tripId?.tripCode ?? '') === tripCode);
    }

    private findTravelTime(departure: TimetableBoardEvent, variantMode: TripMode): number {
        const profiles = variantMode === TripMode.Front ? this.tripProfiles.front : this.tripProfiles.back;
        return profiles.find(profile =>
            profile.routeCode === departure.routeCode
            && profile.tripCode === departure.tripCode
            && profile.trafficMode === departure.trafficMode
        )?.travelTimeInSeconds ?? 0;
    }

    private timeToSeconds(time: string): number {
        const [hours, minutes] = time.split(':').map(Number);
        return (hours * 3600) + (minutes * 60);
    }

}
