import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CalendarsEditorComponent} from "./calendar-editor/calendars-editor.component";
import {SharedModule} from "../shared/shared.module";
import {FormsModule} from "@angular/forms";
import {CalendarListComponent} from "./calendar-list/calendar-list.component";
import {RouterModule} from "@angular/router";
import {CalendarService} from "../../generated/public-transport-api";

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
        CalendarService
    ]
})
export class CalendarsModule {
}
