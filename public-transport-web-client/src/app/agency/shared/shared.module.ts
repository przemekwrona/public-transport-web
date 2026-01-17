import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CalendarPickerComponent} from "./calendar-picker/calendar-picker.component";
import {CalendarInputComponent} from "./calendar-input/calendar-input.component";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {BusStopSelectorComponent} from "./bus-stop-selector/bus-stop-selector.component";
import {FaIconComponent} from "@fortawesome/angular-fontawesome";
import {BusStopModalSelectorComponent} from "./bus-stop-modal-selector/bus-stop-modal-selector.component";
import {
    MatDialogModule,
} from "@angular/material/dialog";
import {BusStopEditorComponent} from "./bus-stop-editor/bus-stop-editor.component";
import {BusStopModalEditorComponent} from "./bus-stop-modal-editor/bus-stop-modal-editor.component";
import {StopsService} from "../../generated/public-transport-api";
import {MatFormField, MatLabel, MatPrefix, MatSuffix} from "@angular/material/form-field";
import {MatOption} from "@angular/material/autocomplete";
import {MatSelect} from "@angular/material/select";
import {MatSelectSearchComponent} from "ngx-mat-select-search";
import {RouteNameNormPipe} from "../timetable/create-timetable/route-id-normalization.pipe";
import {MatIcon} from "@angular/material/icon";


@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        MatDialogModule,
        FaIconComponent,

        BusStopEditorComponent,
        BusStopModalEditorComponent,
        MatFormField,
        MatOption,
        MatSelect,
        MatSelectSearchComponent,
        ReactiveFormsModule,
        RouteNameNormPipe,
        MatIcon,
        MatLabel,
        MatSuffix,
        MatPrefix
    ],
    declarations: [
        BusStopModalSelectorComponent,
        BusStopSelectorComponent,
        CalendarInputComponent,
        CalendarPickerComponent
    ],
    exports: [
        BusStopEditorComponent,
        BusStopModalEditorComponent,
        BusStopModalSelectorComponent,
        BusStopSelectorComponent,
        CalendarInputComponent,
        CalendarPickerComponent
    ],
    providers: [
        StopsService
    ]
})
export class SharedModule {
}
