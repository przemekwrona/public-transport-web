import {Component} from '@angular/core';
import {MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";

@Component({
    selector: 'app-calendar-item-modal',
    imports: [
        MatDialogModule,
        MatButtonModule
    ],
    templateUrl: './calendar-item-modal.component.html',
    styleUrl: './calendar-item-modal.component.scss'
})
export class CalendarItemModalComponent {

}
