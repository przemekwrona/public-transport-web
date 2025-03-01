import {NgModule} from '@angular/core';
import {CommonModule, JsonPipe, NgClass} from '@angular/common';
import {BrigadeListComponent} from "./brigade-list/brigade-list.component";
import {BrigadeEditorComponent} from "./brigade-editor/brigade-editor.component";
import {FormsModule} from "@angular/forms";
import {BrigadeTimePipe} from "./brigade-time.pipe";

@NgModule({
    imports: [
        CommonModule,
        FormsModule
    ],
    declarations: [
        BrigadeListComponent,
        BrigadeEditorComponent,
        BrigadeTimePipe
    ],
    exports: [
        BrigadeListComponent,
        BrigadeEditorComponent
    ]
})
export class BrigadeModule {
}
