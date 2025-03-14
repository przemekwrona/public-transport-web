import {Routes} from '@angular/router';
import {LandingComponent} from "./landing/landing.component";
import {TransportComponent} from "./transport/transport.component";
import {AgencyComponent} from "./agency/agency.component";
import {StopsComponent} from "./agency/stops/stops.component";
import {RoutesComponent} from "./agency/routes/routes.component";
import {ProfileComponent} from "./agency/profile/profile.component";
import {SigninComponent} from "./auth/signin/signin.component";
import {CreateRouteComponent} from "./agency/routes/create-route/create-route.component";
import {RoutesResolver} from "./agency/routes/routes.resolver";
import {TripListComponent} from "./agency/trips/trip-list/trip-list.component";
import {TripEditorComponent} from "./agency/trips/trip-editor/trip-editor.component";
import {GoogleMapsComponent} from "./agency/google-maps/google-maps.component";
import {TripEditorComponentMode} from "./agency/trips/trip-editor/trip-editor-component-mode";
import {tripsResolver} from "./agency/trips/trip-list/trip-list.resolver";
import {tripEditorResolver} from "./agency/trips/trip-editor/trip-editor.resolver";
import {BrigadeEditorComponent} from "./agency/brigade/brigade-editor/brigade-editor.component";
import {CalendarsEditorComponent} from "./agency/calendars/calendars-editor/calendars-editor.component";

export const routes: Routes = [
    {path: '', redirectTo: 'planner', pathMatch: 'full'},
    {path: 'signin', component: SigninComponent},
    {path: 'planner', component: TransportComponent},
    {
        path: 'agency', component: AgencyComponent, children: [
            {path: 'profile', component: ProfileComponent},
            {path: 'stops', component: StopsComponent},
            {path: 'routes', component: RoutesComponent, resolve: { routes: RoutesResolver }},
            {path: 'routes/create', component: CreateRouteComponent},
            {path: 'trips', component: TripListComponent, resolve: {trips: tripsResolver}},
            {path: 'trips/create', component: TripEditorComponent, resolve: {trip: tripEditorResolver}, data: {mode: TripEditorComponentMode.CREATE}},
            {path: 'trips/edit', component: TripEditorComponent,  resolve: {trip: tripEditorResolver}, data: {mode: TripEditorComponentMode.EDIT}},
            {path: 'brigade', component: BrigadeEditorComponent},
            {path: 'brigade/create', component: BrigadeEditorComponent},
            {path: 'calendars', component: CalendarsEditorComponent},
            {path: 'calendars/create', component: CalendarsEditorComponent},
            {path: 'google/maps', component: GoogleMapsComponent}
        ]
    },
    {path: 'info', component: LandingComponent},
];
