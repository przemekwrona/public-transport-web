import { Routes } from '@angular/router';
import {LandingComponent} from "./landing/landing.component";
import {TransportComponent} from "./transport/transport.component";
import {AgencyComponent} from "./agency/agency.component";

export const routes: Routes = [
    { path: '', redirectTo: 'planner', pathMatch: 'full' },
    { path: 'planner', component: TransportComponent },
    { path: 'agency', component: AgencyComponent },
    { path: 'info', component: LandingComponent },
];
