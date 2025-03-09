import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CalendarPickerComponent} from "./calendar-picker/calendar-picker.component";


@NgModule({
    imports: [
        CommonModule
    ],
    declarations: [
        CalendarPickerComponent
    ],
    exports: [
        CalendarPickerComponent
    ]
})
export class SharedModule {
}
