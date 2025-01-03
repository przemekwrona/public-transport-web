import {Routes} from '@angular/router';
import {LandingComponent} from "./landing/landing.component";
import {TransportComponent} from "./transport/transport.component";
import {AgencyComponent} from "./agency/agency.component";
import {StopsComponent} from "./agency/stops/stops.component";
import {RoutesComponent} from "./agency/routes/routes.component";
import {ProfileComponent} from "./agency/profile/profile.component";
import {SigninComponent} from "./auth/signin/signin.component";

export const routes: Routes = [
    {path: '', redirectTo: 'planner', pathMatch: 'full'},
    {path: 'signin', component: SigninComponent},
    {path: 'planner', component: TransportComponent},
    {
        path: 'agency', component: AgencyComponent, children: [
            {path: 'profile', component: ProfileComponent},
            {path: 'stops', component: StopsComponent},
            {path: 'routes', component: RoutesComponent}
        ]
    },
    {path: 'info', component: LandingComponent},
];
