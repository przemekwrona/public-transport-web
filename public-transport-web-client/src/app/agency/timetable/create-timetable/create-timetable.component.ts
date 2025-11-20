import {Component} from '@angular/core';
import {TimetableBoardComponent} from "./timetable-board/timetable-board.component";

@Component({
    selector: 'app-create-timetable',
    standalone: true,
    imports: [
        TimetableBoardComponent
    ],
    templateUrl: './create-timetable.component.html',
    styleUrl: './create-timetable.component.scss'
})
export class CreateTimetableComponent {

}
