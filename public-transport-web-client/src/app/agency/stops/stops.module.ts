import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {StopsComponent} from "./stops.component";
import {StopService} from "../../http/stop.service";
import {MatIconModule} from "@angular/material/icon";
import {SharedModule} from "../shared/shared.module";
import {AuthService} from "../../auth/auth.service";
import {BusStopModalEditorComponent} from "../shared/bus-stop-modal-editor/bus-stop-modal-editor.component";


@NgModule({
    imports: [
        StopsComponent,
        CommonModule,
        SharedModule,
        MatIconModule,
        BusStopModalEditorComponent
    ],
    declarations: [
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
