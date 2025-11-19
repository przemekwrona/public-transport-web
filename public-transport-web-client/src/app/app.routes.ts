import {Routes} from '@angular/router';
import {LandingComponent} from "./landing/landing.component";
import {TransportComponent} from "./transport/transport.component";
import {AgencyComponent} from "./agency/agency.component";
import {StopsComponent} from "./agency/stops/stops.component";
import {RouteListComponent} from "./agency/routes/route-list/route-list.component";
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
import {CalendarsEditorComponent} from "./agency/calendars/calendar-editor/calendars-editor.component";
import {BrigadeListComponent} from "./agency/brigade/brigade-list/brigade-list.component";
import {brigadeGetAllResolver} from "./agency/brigade/brigade-list/brigade-get-all.resolver";
import {BrigadeEditorComponentMode} from "./agency/brigade/brigade-editor/brigade-editor-component-mode";
import {brigadeResolver} from "./agency/brigade/brigade-editor/brigade.resolver";
import {CalendarListComponent} from "./agency/calendars/calendar-list/calendar-list.component";
import {getAllCalendarsResolver} from "./agency/calendars/calendar-list/get-all-calendars.resolver";
import {calendarResolver} from "./agency/calendars/calendar-editor/calendar.resolver";
import {CalendarEditorComponentMode} from "./agency/calendars/calendar-editor/calendar-editor-component-mode";
import {brigadeGetAllCalendarsResolver} from "./agency/brigade/brigade-editor/brigade-get-all-calendars.resolver";
import {googleAgreementsResolver} from "./agency/google-maps/google-agreements.resolver";
import {ProfileListComponent} from "./agency/profile/profile-list/profile-list.component";
import {agenciesResolver} from "./agency/profile/profile-list/agencies.resolver";
import {ProfileDetailsComponent} from "./agency/profile/profile-details/profile-details.component";
import {profileDetailsResolver} from "./agency/profile/profile-details/profile-details.resolver";
import {CreateProfileComponent} from "./agency/profile/create-profile/create-profile.component";
import {centerPointResolver} from "./agency/stops/center-point.resolver";
import {UserListComponent} from "./agency/user/user-list/user-list.component";
import {usersResolver} from "./agency/user/user-list/users.resolver";
import {CreateUserComponent} from "./agency/user/create-user/create-user.component";
import {TimetableListComponent} from "./agency/timetable/timetable-list/timetable-list.component";
import {agencyDetailsResolver} from "./agency/agency-details.resolver";
import {CreateTimetableComponent} from "./agency/timetable/create-timetable/create-timetable.component";

export const routes: Routes = [
    {path: '', redirectTo: 'company', pathMatch: 'full'},

    {path: 'company', component: LandingComponent},
    {path: 'signin', component: SigninComponent},
    {path: 'planner', component: TransportComponent},
    {
        path: 'agency', component: AgencyComponent, resolve: {agencyDetails: agencyDetailsResolver}, children: [
            {path: 'profile', component: ProfileDetailsComponent, resolve: { agencyDetails: profileDetailsResolver }},
            {path: 'stops', component: StopsComponent, resolve: { centerPoint: centerPointResolver }},
            {path: 'routes', component: RouteListComponent, resolve: { routes: RoutesResolver }},
            {path: 'routes/create', component: CreateRouteComponent},
            {path: 'trips', component: TripListComponent, resolve: {trips: tripsResolver}},
            {path: 'trips/create', component: TripEditorComponent, resolve: { trip: tripEditorResolver, variants:  tripsResolver}, data: { mode: TripEditorComponentMode.CREATE }},
            {path: 'trips/edit', component: TripEditorComponent,  resolve: { trip: tripEditorResolver }, data: { mode: TripEditorComponentMode.EDIT }},
            {path: 'timetables', component: TimetableListComponent},
            {path: 'timetables/create', component: CreateTimetableComponent},
            {path: 'brigades', component: BrigadeListComponent, resolve: { brigades: brigadeGetAllResolver }, data: { mode: BrigadeEditorComponentMode.EDIT }},
            {path: 'brigades/create', component: BrigadeEditorComponent, resolve: { calendars: brigadeGetAllCalendarsResolver }, data: { mode: BrigadeEditorComponentMode.CREATE }},
            {path: 'brigades/edit', component: BrigadeEditorComponent, resolve: { calendars: brigadeGetAllCalendarsResolver, brigade: brigadeResolver }, data: { mode: BrigadeEditorComponentMode.EDIT }},
            {path: 'calendars', component: CalendarListComponent, resolve: { calendars: getAllCalendarsResolver }},
            {path: 'calendars/create', component: CalendarsEditorComponent, data: {mode: CalendarEditorComponentMode.CREATE}},
            {path: 'calendars/edit', component: CalendarsEditorComponent, data: {mode: CalendarEditorComponentMode.EDIT}, resolve: { calendar: calendarResolver }},
            {path: 'google/maps', component: GoogleMapsComponent, resolve: { googleAgreements: googleAgreementsResolver }}
        ]
    },
    {
        path: 'admin/agency', component: AgencyComponent, children: [
            {path: 'profiles', component: ProfileListComponent, resolve: {agencies: agenciesResolver}},
            {path: 'create', component: CreateProfileComponent }
        ]
    },
    {
        path: 'admin', component: AgencyComponent, children: [
            {path: 'users', component: UserListComponent, resolve: {users: usersResolver}},
            {path: 'users/create', component: CreateUserComponent}
        ]
    },
    {path: 'info', component: LandingComponent},
];
