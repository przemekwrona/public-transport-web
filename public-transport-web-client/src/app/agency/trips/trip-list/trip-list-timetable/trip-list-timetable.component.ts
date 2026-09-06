import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Data} from "@angular/router";
import {map} from "rxjs";
import {RouteDetails} from "../../../../generated/public-transport-api";

@Component({
    selector: 'app-trip-list-timetable',
    templateUrl: './trip-list-timetable.component.html',
    standalone: true
})
export class TripListTimetableComponent implements OnInit {
    public trips: RouteDetails = {route: {routeId: {line: '', name: ''}}};

    constructor(private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.route.data.pipe(map((data: Data) => data['routeDetails'])).subscribe((trips: RouteDetails) => {
            this.trips = trips;
        });
    }
}
