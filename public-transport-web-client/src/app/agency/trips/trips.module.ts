import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TripEditorComponent} from "./trip-editor/trip-editor.component";
import {TripListComponent} from "./trip-list/trip-list.component";
import {StopService} from "../stops/stop.service";
import {DistancePipe} from "./trip-editor/distance.pipe";
import {TimePipe} from "./trip-editor/time.pipe";

@NgModule({
    imports: [
        CommonModule
    ],
    declarations: [
        TripEditorComponent,
        TripListComponent,
        DistancePipe,
        TimePipe
    ],
    exports: [
        TripEditorComponent,
        TripListComponent
    ],
    providers: [
        StopService
    ]
})
export class TripsModule {
}
