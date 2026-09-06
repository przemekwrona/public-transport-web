import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Data, RouterModule} from "@angular/router";
import {RouteDetails} from "../../../generated/public-transport-api";
import {map} from "rxjs";
import {CommonModule} from "@angular/common";
import {MatTabsModule} from "@angular/material/tabs";
import {TRIP_LIST_TABS} from "./trip-list.tabs";

@Component({
    selector: 'app-trip-list',
    templateUrl: './trip-list.component.html',
    styleUrl: './trip-list.component.scss',
    imports: [
        CommonModule,
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
        const routeDetailsRoute = this._route.pathFromRoot.find(route => route.snapshot.data['routeDetails'] !== undefined);
        routeDetailsRoute?.data.pipe(map((data: Data) => data['routeDetails'] as RouteDetails)).subscribe(trips => {
            this.trips = trips ?? {route: {routeId: {line: '', name: ''}}};
        });
    }
}
