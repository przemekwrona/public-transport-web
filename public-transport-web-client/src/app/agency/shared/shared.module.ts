import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CalendarPickerComponent} from "./calendar-picker/calendar-picker.component";
import {CalendarInputComponent} from "./calendar-input/calendar-input.component";
import {FormsModule} from "@angular/forms";
import {BusStopSelectorComponent} from "./bus-stop-selector/bus-stop-selector.component";


@NgModule({
    imports: [
        CommonModule,
        FormsModule
    ],
    declarations: [
        BusStopSelectorComponent,
        CalendarInputComponent,
        CalendarPickerComponent
    ],
    exports: [
        BusStopSelectorComponent,
        CalendarInputComponent,
        CalendarPickerComponent
    ]
})
export class SharedModule {
}
