import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {BrigadeListComponent} from "./brigade-list/brigade-list.component";
import {BrigadeEditorComponent} from "./brigade-editor/brigade-editor.component";
import {FormsModule} from "@angular/forms";

@NgModule({
    declarations: [
        BrigadeListComponent,
        BrigadeEditorComponent
    ],
    exports: [
        BrigadeListComponent,
        BrigadeEditorComponent
    ],
    imports: [
        CommonModule,
        FormsModule
    ]
})
export class BrigadeModule {
}
