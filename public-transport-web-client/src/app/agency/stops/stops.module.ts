import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {StopsComponent} from "./stops.component";
import {MatIconModule} from "@angular/material/icon";
import {SharedModule} from "../shared/shared.module";
import {LoginService} from "../../auth/login.service";
import {BusStopModalEditorComponent} from "../shared/bus-stop-modal-editor/bus-stop-modal-editor.component";
import {StopsService} from "../../generated/public-transport-api";


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
        StopsService,
        LoginService
    ]
})
export class StopsModule {
}
