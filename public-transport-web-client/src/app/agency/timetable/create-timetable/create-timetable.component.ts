import {Component, OnInit} from '@angular/core';
import {TimetableBoardComponent} from "./timetable-board/timetable-board.component";
import {FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {
    GetCalendarsResponse,
    TimetableGeneratorFilterByRoutes,
    TimetableGeneratorFilterByRoutesResponse
} from "../../../generated/public-transport-api";
import {ActivatedRoute} from "@angular/router";
import {FaIconComponent} from "@fortawesome/angular-fontawesome";
import {NgxMaterialTimepickerModule} from "ngx-material-timepicker";
import {faClock} from "@fortawesome/free-solid-svg-icons";
import {CommonModule} from "@angular/common";
import {NgxMatSelectSearchModule} from "ngx-mat-select-search";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {RouteNameNormPipe} from "./route-id-normalization.pipe";

@Component({
    selector: 'app-create-timetable',
    standalone: true,
    imports: [
        CommonModule,
        TimetableBoardComponent,
        ReactiveFormsModule,
        FormsModule,
        NgxMaterialTimepickerModule,
        NgxMatSelectSearchModule,
        MatFormFieldModule,
        MatSelectModule,
        RouteNameNormPipe
    ],
    templateUrl: './create-timetable.component.html',
    styleUrl: './create-timetable.component.scss'
})
export class CreateTimetableComponent implements OnInit {

    public formGroup: FormGroup;
    public calendarsResponse: GetCalendarsResponse = {};
    public routes: TimetableGeneratorFilterByRoutesResponse = {};
    public calendarName = '';

    /** control for the selected bank */
    public bankCtrl: FormControl<TimetableGeneratorFilterByRoutes> = new FormControl<TimetableGeneratorFilterByRoutes>(null);

    /** control for the MatSelect filter keyword */
    public bankFilterCtrl: FormControl<string> = new FormControl<string>('');


    constructor(private formBuilder: FormBuilder, private _route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.formGroup = this.formBuilder.group({
            workingDay: this.buildTimetable(),
            saturday: this.buildTimetable(),
            sunday: this.buildTimetable()
        });

        this._route.data.subscribe(data => this.calendarsResponse = data['calendars']);
        this._route.data.subscribe(data => this.routes = data['routes']);
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
