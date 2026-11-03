import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AgencyComponent} from "./agency.component";
import {StopsDbModule} from "./stops-db/stops-db.module";
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
import {BrigadeModule} from "./brigade/brigade.module";
import {CalendarsModule} from "./calendars/calendars.module";
import {AgencyStopsModule} from "./agency-stops/agency-stops.module";
import {SidebarModule} from "primeng/sidebar";


@NgModule({
    declarations: [
        AgencyComponent,
        FooterComponent,
    ],
    exports: [
        AgencyComponent,
        SidebarComponent
    ],
    imports: [
        CommonModule,
        SidebarComponent,
        HeaderComponent,
        ProfileModule,
        RouterModule,
        RoutesModule,
        TripsModule,
        StopsDbModule,
        AgencyStopsModule,
        GoogleMapsModule,
        MatTreeModule,
        BrigadeModule,
        CalendarsModule,
        SidebarModule
    ],
    providers: [
        StopService
    ]
})
export class AgencyModule {
}
