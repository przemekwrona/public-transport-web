import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, RouterModule} from '@angular/router';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {TimetableBoardComponent} from '../../timetable/create-timetable/timetable-board/timetable-board.component';
import {GetAllTripsResponse, TripResponse} from '../../../generated/public-transport-api';

@Component({
    selector: 'app-timetable-wizard',
    imports: [
        CommonModule,
        RouterModule,
        ReactiveFormsModule,
        TimetableBoardComponent
    ],
    templateUrl: './timetable-wizard.component.html',
    styleUrl: './timetable-wizard.component.scss'
})
export class TimetableWizardComponent implements OnInit {

    public brigadeCode: string | null = null;
    public calendarSymbol: string | null = null;
    public isSubmitted: boolean = false;
    public tripResponse: TripResponse = {front: {}, back: {}};
    public defaultRoute: GetAllTripsResponse | null = null;
    public formGroup: FormGroup;

    constructor(private route: ActivatedRoute, private formBuilder: FormBuilder) {
        this.formGroup = this.formBuilder.group({
            front: this.buildDirectionGroup(15),
            back: this.buildDirectionGroup(18)
        });
    }

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.brigadeCode = params.get('brigadeCode');
            this.calendarSymbol = params.get('calendarSymbol');
        });
        this.route.data.subscribe(data => this.defaultRoute = data['defaultRoute'] ?? null);
    }

    public getFrontTimetable(): FormGroup {
        return this.formGroup.get('front') as FormGroup;
    }

    public getBackTimetable(): FormGroup {
        return this.formGroup.get('back') as FormGroup;
    }

    private buildDirectionGroup(interval: number): FormGroup {
        return this.formBuilder.group({
            startTime: ['06:00'],
            endTime: ['20:00'],
            interval,
            departures: this.formBuilder.array([])
        });
    }

}
