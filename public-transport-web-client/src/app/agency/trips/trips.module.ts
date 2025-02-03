import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TripEditorComponent} from "./trip-editor/trip-editor.component";
import {TripListComponent} from "./trip-list/trip-list.component";
import {StopService} from "../stops/stop.service";
import {DistancePipe} from "./trip-editor/distance.pipe";
import {TimePipe} from "./trip-editor/time.pipe";
import {NgxSortableModule} from "ngx-sortable";
import {
    DndDraggableDirective,
    DndDropzoneDirective,
    DndHandleDirective,
    DndModule,
    DndPlaceholderRefDirective
} from "ngx-drag-drop";
import {CdkDrag, CdkDropList} from "@angular/cdk/drag-drop";
import {FormsModule} from "@angular/forms";

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        NgxSortableModule,
        DndModule,

        CdkDropList,
        CdkDrag,

        DndDropzoneDirective,
        DndPlaceholderRefDirective,
        DndDraggableDirective,
        DndHandleDirective
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
