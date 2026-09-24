import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CommonModule} from "@angular/common";
import {TrafficMode, Trip, TripId1, TripService} from "../../../../generated/public-transport-api";
import moment from "moment/moment";
import {TranslocoModule} from "@jsverse/transloco";
import {AgencyStorageService} from "../../../../auth/agency-storage.service";
import {Router} from "@angular/router";
import {MatIconModule} from "@angular/material/icon";

@Component({
    selector: 'app-trip-item',
    imports: [
        CommonModule,
        TranslocoModule,
        MatIconModule
    ],
    templateUrl: './trip-item.component.html',
    styleUrl: './trip-item.component.scss'
})
export class TripItemComponent {

    @Input() routeCode: string = '';
    @Input() trip: Trip = {} as Trip;
    @Input() state: { line: string, name: string, version: number };

    @Output() onDelete: EventEmitter<TripId1> = new EventEmitter();

    constructor(private agencyStorageService: AgencyStorageService, private tripService: TripService, private router: Router) {
    }

    public isCreatedOrUpdated(trip: Trip): boolean {
        const twoMinutesAgo = moment().subtract(1, 'minute');
        return moment(trip.createdAt).isAfter(twoMinutesAgo)
            || moment(trip.updatedAt).isAfter(twoMinutesAgo)
    }

    public editTrip(trip: Trip) {
        this.router.navigate(['/agency/routes', this.routeCode, 'trips', trip.tripId.tripCode, 'edit']).then(() => {
        });
    }

    public deleteTrip(trip: Trip) {
        const tripId: TripId1 = {
            routeId: {line: this.state.line, name: this.state.name, version: this.state.version},
            variantName: trip.variant,
            variantMode: trip.mode,
            trafficMode: trip.trafficMode,
            tripCode: trip.tripId?.tripCode
        };
        this.onDelete.emit(tripId);
    }

    public isEmpty(value: string | null): boolean {
        return [null, undefined, ''].includes(value)
    }

    protected readonly TrafficMode = TrafficMode;
}
