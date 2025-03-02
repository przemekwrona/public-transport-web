import {NgModule} from '@angular/core';
import {CommonModule, JsonPipe, NgClass} from '@angular/common';
import {BrigadeListComponent} from "./brigade-list/brigade-list.component";
import {BrigadeEditorComponent} from "./brigade-editor/brigade-editor.component";
import {FormsModule} from "@angular/forms";
import {BrigadeTimePipe} from "./brigade-time.pipe";
import {CdkDrag, CdkDropList, CdkDropListGroup} from "@angular/cdk/drag-drop";
import {FaIconComponent} from "@fortawesome/angular-fontawesome";

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        CdkDropListGroup,
        CdkDropList,
        CdkDrag,
        FaIconComponent
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
