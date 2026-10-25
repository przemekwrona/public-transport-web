import {Component, inject, OnInit, ChangeDetectionStrategy} from '@angular/core';

import {
    MAT_DIALOG_DATA,
    MatDialogActions,
    MatDialogClose,
    MatDialogContent,
    MatDialogTitle
} from '@angular/material/dialog';
import {MatButton} from '@angular/material/button';
import {
    BrigadeTimetableService,
    GetTimetableByBrigadeResponse
} from '../../../../generated/public-transport-api';
import {AgencyStorageService} from '../../../../auth/agency-storage.service';
import {TimetableBoardComponent} from './timetable-board/timetable-board.component';

export interface BrigadeTimetableModalData {
    brigadeCode: string;
    calendarCode: string;
    calendarSymbol: string;
}

@Component({
    selector: 'app-brigade-timetable-modal',
    imports: [
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatDialogClose,
    MatButton,
    TimetableBoardComponent
],
    providers: [
        AgencyStorageService,
        BrigadeTimetableService
    ],
    templateUrl: './brigade-timetable-modal.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './brigade-timetable-modal.component.scss'
})
export class BrigadeTimetableModalComponent implements OnInit {

    readonly data = inject<BrigadeTimetableModalData>(MAT_DIALOG_DATA);

    timetable: GetTimetableByBrigadeResponse = {};

    constructor(
        private agencyStorageService: AgencyStorageService,
        private brigadeTimetableService: BrigadeTimetableService) {
    }

    ngOnInit(): void {
        const instance = this.agencyStorageService.getInstance();

        this.brigadeTimetableService.getTimetableByBrigadeAndCalendarSymbol(
            instance,
            this.data.brigadeCode,
            this.data.calendarCode,
            this.data.calendarSymbol
        ).subscribe(response => this.timetable = response ?? {});
    }

}
