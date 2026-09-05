import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, RouterModule} from '@angular/router';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {
    AvailableTripProfile,
    TimetableBoardComponent
} from '../../timetable/create-timetable/timetable-board/timetable-board.component';
import {
    GetAllTripsResponse,
    TrafficMode,
    Trip,
    TripMode,
    TripResponse
} from '../../../generated/public-transport-api';

@Component({
    selector: 'app-timetable-wizard',
    imports: [
        CommonModule,
        RouterModule,
        ReactiveFormsModule,
        TimetableBoardComponent
    ],
    templateUrl: './timetable-wizard.component.html',
    styleUrl: './timetable-wizard.component.scss'
})
export class TimetableWizardComponent implements OnInit {

    public brigadeCode: string | null = null;
    public calendarSymbol: string | null = null;
    public isSubmitted: boolean = false;
    public tripResponse: TripResponse = {front: {}, back: {}};
    public defaultRoute: GetAllTripsResponse | null = null;
    public tripProfiles: { front: AvailableTripProfile[]; back: AvailableTripProfile[] } = {front: [], back: []};
    public formGroup: FormGroup;

    constructor(private route: ActivatedRoute, private formBuilder: FormBuilder) {
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

    private buildTripProfiles(defaultRoute: GetAllTripsResponse | null): { front: AvailableTripProfile[]; back: AvailableTripProfile[] } {
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

}
