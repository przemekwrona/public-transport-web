import {Component, inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
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

export interface BrigadeTimetableModalData {
    brigadeCode: string;
    calendarCode: string;
    calendarSymbol: string;
}

@Component({
    selector: 'app-brigade-timetable-modal',
    imports: [
        CommonModule,
        MatDialogTitle,
        MatDialogContent,
        MatDialogActions,
        MatDialogClose,
        MatButton
    ],
    providers: [
        AgencyStorageService,
        BrigadeTimetableService
    ],
    templateUrl: './brigade-timetable-modal.component.html',
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
