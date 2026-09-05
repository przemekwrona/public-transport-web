import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {BrigadeListComponent} from "./brigade-list/brigade-list.component";
import {BrigadeEditorComponent} from "./brigade-editor/brigade-editor.component";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {BrigadeTimePipe} from "./brigade-time.pipe";
import {CdkDrag, CdkDropList, CdkDropListGroup} from "@angular/cdk/drag-drop";
import {FaIconComponent} from "@fortawesome/angular-fontawesome";
import {RouterModule} from "@angular/router";
import {BrigadeSchedulerComponent} from "./brigade-scheduler/brigade-scheduler.component";
import {MatInputModule} from "@angular/material/input";
import {MatSelectModule} from "@angular/material/select";
import {MatDialogModule} from "@angular/material/dialog";
import {MatIconModule} from "@angular/material/icon";

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        RouterModule,
        CdkDropListGroup,
        CdkDropList,
        CdkDrag,
        FaIconComponent,
        BrigadeSchedulerComponent,
        ReactiveFormsModule,
        MatInputModule,
        MatSelectModule,
        MatDialogModule,
        MatIconModule
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
