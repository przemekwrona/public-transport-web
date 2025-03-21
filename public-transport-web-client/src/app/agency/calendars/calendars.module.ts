import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CalendarsEditorComponent} from "./calendars-editor/calendars-editor.component";
import {SharedModule} from "../shared/shared.module";
import {FormsModule} from "@angular/forms";
import {CalendarsComponent} from "./calendars/calendars.component";

@NgModule({
    imports: [
        CommonModule,
        SharedModule,
        FormsModule
    ],
    declarations: [
        CalendarsComponent,
        CalendarsEditorComponent
    ]
})
export class CalendarsModule {
}
