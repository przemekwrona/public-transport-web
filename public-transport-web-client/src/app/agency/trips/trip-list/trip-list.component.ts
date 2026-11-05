import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {ActivatedRoute, RouterModule} from "@angular/router";
import {RouteDetails} from "../../../generated/public-transport-api";
import {routeDetailsData} from "./trip-list.resolver";

import {MatTabsModule} from "@angular/material/tabs";
import {TRIP_LIST_TABS} from "./trip-list.tabs";

@Component({
    selector: 'app-trip-list',
    templateUrl: './trip-list.component.html',
    styleUrl: './trip-list.component.scss',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [
    RouterModule,
    MatTabsModule
]
})
export class TripListComponent implements OnInit {
    public readonly tabs = TRIP_LIST_TABS;
    public trips: RouteDetails = {route: {routeId: {line: '', name: ''}}};

    constructor(private _route: ActivatedRoute) {
    }

    ngOnInit(): void {
        routeDetailsData(this._route)?.subscribe(trips => {
            this.trips = trips ?? {route: {routeId: {line: '', name: ''}}};
        });
    }
}
