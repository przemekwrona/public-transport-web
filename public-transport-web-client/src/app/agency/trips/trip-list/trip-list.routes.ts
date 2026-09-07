import {Routes} from "@angular/router";
import {TripListInfoComponent} from "./trip-list-info/trip-list-info.component";
import {TripListVariantsComponent} from "./trip-list-variants/trip-list-variants.component";
import {TripListTimetableComponent} from "./trip-list-timetable/trip-list-timetable.component";
import {tripListInfoResolver} from "./trip-list-info/trip-list-info.resolver";
import {tripListVariantsResolver} from "./trip-list-variants/trip-list-variants.resolver";
import {tripListTimetableResolver} from "./trip-list-timetable/trip-list-timetable.resolver";

export const tripListChildRoutes: Routes = [
    {path: '', pathMatch: 'full', redirectTo: 'info'},
    {path: 'info', component: TripListInfoComponent, resolve: {routeDetails: tripListInfoResolver}},
    {path: 'variants', component: TripListVariantsComponent, resolve: {routeDetails: tripListVariantsResolver}},
    {path: 'timetable', component: TripListTimetableComponent, resolve: {response: tripListTimetableResolver}},
];
