import { Routes } from '@angular/router';
import {LandingComponent} from "./landing/landing.component";
import {TransportComponent} from "./transport/transport.component";

export const routes: Routes = [
    { path: '', redirectTo: 'planner', pathMatch: 'full' },
    { path: 'planner', component: TransportComponent },
    { path: 'info', component: LandingComponent },
];
