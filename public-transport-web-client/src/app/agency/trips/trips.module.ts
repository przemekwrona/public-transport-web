import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TripEditorComponent} from "./trip-editor/trip-editor.component";
import {TripMapComponent} from "./trip-map/trip-map.component";
import {TripStopListComponent} from "./trip-map/trip-stop-list.component";
import {TripStopsMapComponent} from "./trip-map/trip-stops-map.component";
import {TripListComponent} from "./trip-list/trip-list.component";
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
import {CdkDrag, CdkDragHandle, CdkDropList} from "@angular/cdk/drag-drop";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {FaIconComponent} from "@fortawesome/angular-fontawesome";
import {RouterModule} from "@angular/router";
import {TripProfileService, TripService} from "../../generated/public-transport-api";
import {TranslocoPipe} from "@jsverse/transloco";
import {CountDownComponent} from "./trip-editor/count-down/count-down.component";
import {MatRadioModule} from "@angular/material/radio";
import {MatIconModule} from "@angular/material/icon";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatTabsModule} from '@angular/material/tabs';
import {MatButtonModule} from '@angular/material/button';
import {TownRoadSignComponent} from "./trip-editor/town-road-sign/town-road-sign.component";
import {Bars} from "@primeicons/angular/bars";
import {Pencil} from "@primeicons/angular/pencil";
import {Refresh} from "@primeicons/angular/refresh";
import {Trash} from "@primeicons/angular/trash";
import {ButtonModule} from "primeng/button";
import {InputNumberModule} from "primeng/inputnumber";
import {MessageModule} from "primeng/message";
import {SidebarModule} from "primeng/sidebar";
import {TagModule} from "primeng/tag";
import {ToggleSwitchModule} from "primeng/toggleswitch";

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        RouterModule,
        NgxSortableModule,
        DndModule,

        CdkDropList,
        CdkDrag,
        CdkDragHandle,

        DndDropzoneDirective,
        DndPlaceholderRefDirective,
        DndDraggableDirective,
        DndHandleDirective,
        FaIconComponent,

        TripListComponent,
        TranslocoPipe,
        CountDownComponent,
        ReactiveFormsModule,

        TownRoadSignComponent,

        MatRadioModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        MatTabsModule,
        MatButtonModule,
        Bars,
        Pencil,
        Refresh,
        Trash,
        ButtonModule,
        InputNumberModule,
        MessageModule,
        SidebarModule,
        TagModule,
        ToggleSwitchModule
    ],
    declarations: [
        TripEditorComponent,
        TripMapComponent,
        TripStopListComponent,
        TripStopsMapComponent,
        DistancePipe,
        TimePipe
    ],
    exports: [
        TripEditorComponent,
        TripListComponent
    ],
    providers: [
        TripService,
        TripProfileService
    ]
})
export class TripsModule {
}
