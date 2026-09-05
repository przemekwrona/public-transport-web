import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, RouterModule} from '@angular/router';

@Component({
    selector: 'app-timetable-wizard',
    imports: [
        RouterModule
    ],
    templateUrl: './timetable-wizard.component.html',
    styleUrl: './timetable-wizard.component.scss'
})
export class TimetableWizardComponent implements OnInit {

    public brigadeCode: string | null = null;
    public calendarSymbol: string | null = null;

    constructor(private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.brigadeCode = params.get('brigadeCode');
            this.calendarSymbol = params.get('calendarSymbol');
        });
    }

}
