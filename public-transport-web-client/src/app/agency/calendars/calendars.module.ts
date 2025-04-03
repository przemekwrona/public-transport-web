import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CalendarsEditorComponent} from "./calendars-editor/calendars-editor.component";
import {SharedModule} from "../shared/shared.module";
import {FormsModule} from "@angular/forms";
import {CalendarListComponent} from "./calendar-list/calendar-list.component";
import {CalendarsService} from "./calendars.service";
import {RouterModule} from "@angular/router";

@NgModule({
    imports: [
        CommonModule,
        RouterModule,
        SharedModule,
        FormsModule
    ],
    declarations: [
        CalendarListComponent,
        CalendarsEditorComponent
    ],
    providers: [
        CalendarsService
    ]
})
export class CalendarsModule {
}
