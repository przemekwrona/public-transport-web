import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {StopsDbComponent} from "./stops-db.component";
import {MatIconModule} from "@angular/material/icon";
import {SharedModule} from "../shared/shared.module";
import {LoginService} from "../../auth/login.service";
import {BusStopModalEditorComponent} from "../shared/bus-stop-modal-editor/bus-stop-modal-editor.component";
import {StopsService} from "../../generated/public-transport-api";


@NgModule({
    imports: [
        StopsDbComponent,
        CommonModule,
        SharedModule,
        MatIconModule,
        BusStopModalEditorComponent
    ],
    declarations: [
    ],
    exports: [
        StopsDbComponent
    ], providers: [
        StopsService,
        LoginService
    ]
})
export class StopsDbModule {
}
