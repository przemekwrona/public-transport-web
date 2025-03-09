import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CalendarsEditorComponent} from "./calendars-editor/calendars-editor.component";
import {CalendarsComponent} from "./calendars/calendars.component";
import {SharedModule} from "../shared/shared.module";
import {FormsModule} from "@angular/forms";

@NgModule({
    declarations: [
        CalendarsComponent,
        CalendarsEditorComponent
    ],
    imports: [
        CommonModule,
        SharedModule,
        FormsModule
    ]
})
export class CalendarsModule {
}
