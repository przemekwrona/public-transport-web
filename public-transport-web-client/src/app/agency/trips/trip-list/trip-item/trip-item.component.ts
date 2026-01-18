import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CommonModule} from "@angular/common";
import {Trip, TripId, TripService} from "../../../../generated/public-transport-api";
import moment from "moment/moment";
import {TranslocoModule} from "@jsverse/transloco";
import {AgencyStorageService} from "../../../../auth/agency-storage.service";
import {Router} from "@angular/router";

@Component({
    selector: 'app-trip-item',
    standalone: true,
    imports: [
        CommonModule,
        TranslocoModule
    ],
    templateUrl: './trip-item.component.html',
    styleUrl: './trip-item.component.scss'
})
export class TripItemComponent {

    @Input() trip: Trip = {} as Trip;
    @Input() state: { line: string, name: string, version: number };

    @Output() onDelete: EventEmitter<TripId> = new EventEmitter();

    constructor(private agencyStorageService: AgencyStorageService, private tripService: TripService, private router: Router) {
    }

    public isCreatedOrUpdated(trip: Trip): boolean {
        const twoMinutesAgo = moment().subtract(1, 'minute');
        return moment(trip.createdAt).isAfter(twoMinutesAgo)
            || moment(trip.updatedAt).isAfter(twoMinutesAgo)
    }

    public editTrip(trip: Trip) {
        const queryParams = {
            name: this.state.name,
            line: this.state.line,
            version: this.state.version,
            variant: trip.variant,
            mode: trip.mode,
            trafficMode: trip.trafficMode
        };
        this.router.navigate(['/agency/trips/edit'], {queryParams: queryParams}).then(() => {
        });
    }

    public deleteTrip(trip: Trip) {
        const tripId: TripId = {
            routeId: {line: this.state.line, name: this.state.name, version: this.state.version},
            variantName: trip.variant,
            variantMode: trip.mode,
            trafficMode: trip.trafficMode
        } as TripId;
        // this.tripService.deleteTripByTripId(this.agencyStorageService.getInstance(), tripId).subscribe(response => {
        this.onDelete.emit(tripId);
        // });
    }

    public isEmpty(value: string | null): boolean {
        return [null, undefined, ''].includes(value)
    }
}
