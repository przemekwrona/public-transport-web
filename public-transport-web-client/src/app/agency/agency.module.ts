import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AgencyComponent} from "./agency.component";
import {StopsModule} from "./stops/stops.module";
import {StopService} from "../http/stop.service";
import {RouterModule} from "@angular/router";
import {RoutesModule} from "./routes/routes.module";
import {MatTreeModule} from "@angular/material/tree";
import {ProfileModule} from "./profile/profile.module";
import {TripsModule} from "./trips/trips.module";
import {SidebarComponent} from "./sidebar/sidebar.component";
import {HeaderComponent} from "./header/header.component";
import {FooterComponent} from "./footer/footer.component";
import {GoogleMapsModule} from "./google-maps/google-maps.module";


@NgModule({
    declarations: [
        AgencyComponent,
        HeaderComponent,
        FooterComponent,
        SidebarComponent
    ],
    exports: [
        AgencyComponent
    ],
    imports: [
        CommonModule,
        ProfileModule,
        RouterModule,
        TripsModule,
        StopsModule,
        RoutesModule,
        GoogleMapsModule,
        MatTreeModule
    ],
    providers: [
        StopService
    ]
})
export class AgencyModule {
}
