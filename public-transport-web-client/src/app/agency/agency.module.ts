import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AgencyComponent} from "./agency.component";
import {StopsModule} from "./stops/stops.module";
import {StopService} from "../http/stop.service";
import {RouterModule} from "@angular/router";
import {RoutesModule} from "./routes/routes.module";


@NgModule({
    declarations: [
        AgencyComponent
    ],
    exports: [
        AgencyComponent
    ],
    imports: [
        CommonModule,
        RouterModule,
        StopsModule,
        RoutesModule
    ],
    providers: [
        StopService
    ]
})
export class AgencyModule {
}
