import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CalendarPickerComponent} from "./calendar-picker/calendar-picker.component";
import {CalendarInputComponent} from "./calendar-input/calendar-input.component";
import {FormsModule} from "@angular/forms";
import {BusStopSelectorComponent} from "./bus-stop-selector/bus-stop-selector.component";
import {FaIconComponent} from "@fortawesome/angular-fontawesome";
import {BusStopModalSelectorComponent} from "./bus-stop-modal-selector/bus-stop-modal-selector.component";
import {
    MatDialogModule,
} from "@angular/material/dialog";
import {BusStopEditorComponent} from "./bus-stop-editor/bus-stop-editor.component";
import {BusStopModalEditorComponent} from "./bus-stop-modal-editor/bus-stop-modal-editor.component";


@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        MatDialogModule,
        FaIconComponent
    ],
    declarations: [
        BusStopEditorComponent,
        BusStopModalEditorComponent,
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
    ]
})
export class SharedModule {
}
