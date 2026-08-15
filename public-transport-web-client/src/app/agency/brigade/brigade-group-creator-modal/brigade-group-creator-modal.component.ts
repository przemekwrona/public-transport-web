import {Component, inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
    MAT_DIALOG_DATA,
    MatDialogModule,
    MatDialogRef
} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {
    BrigadeService,
    CalendarService,
    CalendarSymbolId,
    CreateCalendarSymbolBrigadeRequest,
    CreateCalendarSymbolBrigadeResponse,
    GetCalendarsResponse
} from '../../../generated/public-transport-api';
import {AgencyStorageService} from '../../../auth/agency-storage.service';

export interface BrigadeGroupCreatorModalData {
    brigadeCode: string;
    brigadeName: string;
}

@Component({
    selector: 'app-brigade-group-creator-modal',
    imports: [
        CommonModule,
        MatDialogModule,
        MatButtonModule,
        MatFormFieldModule,
        ReactiveFormsModule,
        MatSelectModule
    ],
    providers: [
        AgencyStorageService,
        CalendarService,
        BrigadeService
    ],
    templateUrl: './brigade-group-creator-modal.component.html',
    styleUrl: './brigade-group-creator-modal.component.scss'
})
export class BrigadeGroupCreatorModalComponent implements OnInit {

    readonly data = inject<BrigadeGroupCreatorModalData>(MAT_DIALOG_DATA);

    public modelForm: FormGroup;
    public calendarsResponse: GetCalendarsResponse = {};

    constructor(
        private formBuilder: FormBuilder,
        private dialogRef: MatDialogRef<BrigadeGroupCreatorModalComponent>,
        private agencyStorageService: AgencyStorageService,
        private calendarService: CalendarService,
        private brigadeService: BrigadeService) {
        this.modelForm = this.formBuilder.group({
            calendarId: [null, [Validators.required]]
        });
    }

    ngOnInit(): void {
        this.calendarService.getCalendars(this.agencyStorageService.getInstance())
            .subscribe(response => this.calendarsResponse = response ?? {});
    }

    public createBrigadeGroup(): void {
        if (!this.modelForm.valid) {
            return;
        }

        const calendarSymbolId: CalendarSymbolId = this.modelForm.get('calendarId').value;
        const calendarCode = calendarSymbolId.calendarItemId.code;
        const calendarSymbol = calendarSymbolId.symbol;

        const createCalendarSymbolBrigadeRequest: CreateCalendarSymbolBrigadeRequest = {
            calendarSymbolId: calendarSymbolId,
            brigadeName: this.data.brigadeName
        };

        const instance: string = this.agencyStorageService.getInstance();

        this.brigadeService.createCalendarSymbolBrigade(
            instance,
            this.data.brigadeCode,
            calendarCode,
            calendarSymbol,
            createCalendarSymbolBrigadeRequest
        ).subscribe((response: CreateCalendarSymbolBrigadeResponse) => {
            this.dialogRef.close(response);
        });
    }

    public hasError(controlName: string, error: string): boolean {
        return this.modelForm.get(controlName).hasError(error);
    }

    public compareByCalendarId(current: CalendarSymbolId, option: CalendarSymbolId): boolean {
        return current && option
            ? current.calendarItemId.code === option.calendarItemId.code
            && current.symbol === option.symbol
            : current === option;
    }

}
