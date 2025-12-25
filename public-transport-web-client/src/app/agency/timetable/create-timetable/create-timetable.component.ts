import {Component, OnInit} from '@angular/core';
import {TimetableBoardComponent} from "./timetable-board/timetable-board.component";
import {FormBuilder, FormGroup} from "@angular/forms";

@Component({
    selector: 'app-create-timetable',
    standalone: true,
    imports: [
        TimetableBoardComponent
    ],
    templateUrl: './create-timetable.component.html',
    styleUrl: './create-timetable.component.scss'
})
export class CreateTimetableComponent implements OnInit {

    public formGroup: FormGroup;

    constructor(private formBuilder: FormBuilder) {
    }

    ngOnInit(): void {
        this.formGroup = this.formBuilder.group({
            workingDay: this.buildTimetable(),
            saturday: this.buildTimetable(),
            sunday: this.buildTimetable()
        })
    }

    private buildTimetable() {
        return this.formBuilder.group({
            startTime: [''],
            endTime: [''],
            interval: 5
        });
    }

    public getTimetableByName(name: string): FormGroup {
        return this.formGroup.get(name) as FormGroup;
    }
}
