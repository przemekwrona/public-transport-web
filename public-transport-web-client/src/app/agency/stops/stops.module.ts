import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {StopsComponent} from "./stops.component";
import {StopService} from "../../http/stop.service";


@NgModule({
    imports: [
        CommonModule
    ],
    declarations: [
        StopsComponent
    ],
    exports: [
        StopsComponent
    ], providers: [
        StopService
    ]
})
export class StopsModule {
}
