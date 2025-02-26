import {NgModule} from '@angular/core';
import {CommonModule, JsonPipe} from '@angular/common';
import {BrigadeListComponent} from "./brigade-list/brigade-list.component";
import {BrigadeEditorComponent} from "./brigade-editor/brigade-editor.component";
import {FormsModule} from "@angular/forms";

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        JsonPipe
    ],
    declarations: [
        BrigadeListComponent,
        BrigadeEditorComponent
    ],
    exports: [
        BrigadeListComponent,
        BrigadeEditorComponent
    ]
})
export class BrigadeModule {
}
