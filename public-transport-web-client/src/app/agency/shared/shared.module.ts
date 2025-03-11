import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CalendarPickerComponent} from "./calendar-picker/calendar-picker.component";
import {CalendarInputComponent} from "./calendar-input/calendar-input.component";
import {FormsModule} from "@angular/forms";


@NgModule({
    imports: [
        CommonModule,
        FormsModule
    ],
    declarations: [
        CalendarInputComponent,
        CalendarPickerComponent
    ],
    exports: [
        CalendarInputComponent,
        CalendarPickerComponent
    ]
})
export class SharedModule {
}
