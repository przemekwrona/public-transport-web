import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {StopsComponent} from "./stops.component";
import {StopService} from "../../http/stop.service";
import {MatIconModule} from "@angular/material/icon";
import {SharedModule} from "../shared/shared.module";
import {AuthService} from "../../auth/auth.service";


@NgModule({
    imports: [
        CommonModule,
        SharedModule,
        MatIconModule
    ],
    declarations: [
        StopsComponent
    ],
    exports: [
        StopsComponent
    ], providers: [
        StopService,
        AuthService
    ]
})
export class StopsModule {
}
