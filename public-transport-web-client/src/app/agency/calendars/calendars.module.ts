import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CalendarsEditorComponent} from "./calendar-editor/calendars-editor.component";
import {SharedModule} from "../shared/shared.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {CalendarListComponent} from "./calendar-list/calendar-list.component";
import {RouterModule} from "@angular/router";
import {CalendarService} from "../../generated/public-transport-api";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {FaIconComponent} from "@fortawesome/angular-fontawesome";
import {MatCheckbox} from "@angular/material/checkbox";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {provideLuxonDateAdapter} from "@angular/material-luxon-adapter";
import {CalendarPickerComponent} from "./calendar-picker/calendar-picker.component";

export const MY_FORMATS = {
    parse: {
        dateInput: 'dd-MM-yyyy',
    },
    display: {
        dateInput: 'dd-MM-yyyy',
        monthYearLabel: 'yyyy MMM',
        dateA11yLabel: 'DD',
        monthYearA11yLabel: 'yyyy MMMM'
    },
};

@NgModule({
    imports: [
        CommonModule,
        RouterModule,
        SharedModule,
        FormsModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatCheckbox,
        MatDatepickerModule,
        FaIconComponent,
        CalendarPickerComponent
    ],
    declarations: [
        CalendarListComponent,
        CalendarsEditorComponent
    ],
    providers: [
        CalendarService,
        provideLuxonDateAdapter(MY_FORMATS)
    ]
})
export class CalendarsModule {
}
