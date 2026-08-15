import {Component, OnInit} from '@angular/core';
import {MatDialogModule, MatDialogRef} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";
import {FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatFormFieldModule} from "@angular/material/form-field";
import {CommonModule} from "@angular/common";
import {
    BrigadeBody,
    BrigadeService,
    CalendarService,
    CalendarSymbolId, CreateCalendarSymbolBrigadeRequest, CreateCalendarSymbolBrigadeResponse,
    GetCalendarsResponse, Status
} from "../../../generated/public-transport-api";
import {AgencyStorageService} from "../../../auth/agency-storage.service";
import {MatInput} from "@angular/material/input";
import {MatSelectModule} from "@angular/material/select";

@Component({
    selector: 'app-brigade-creator-modal',
    imports: [
        CommonModule,
        MatDialogModule,
        MatButtonModule,
        MatFormFieldModule,
        ReactiveFormsModule,
        MatInput,
        MatSelectModule
    ],
    providers: [
        AgencyStorageService,
        CalendarService,
        BrigadeService
    ],
    templateUrl: './brigade-creator-modal.component.html',
    styleUrl: './brigade-creator-modal.component.scss'
})
export class BrigadeCreatorModalComponent implements OnInit {

    public modelForm: FormGroup;
    public calendarsResponse: GetCalendarsResponse = {};

    get brigadeNameControl(): FormControl {
        return this.modelForm.get('brigadeName') as FormControl;
    }

    constructor(
        private formBuilder: FormBuilder,
        private dialogRef: MatDialogRef<BrigadeCreatorModalComponent>,
        private agencyStorageService: AgencyStorageService,
        private calendarService: CalendarService,
        private brigadeService: BrigadeService) {
        this.modelForm = this.formBuilder.group({
            brigadeName: ['', [Validators.required]],
            calendarId: [null, [Validators.required]]
        });
    }

    ngOnInit(): void {
        this.calendarService.getCalendars(this.agencyStorageService.getInstance())
            .subscribe(response => this.calendarsResponse = response ?? {});
    }

    public createBrigade(): void {
        if (!this.modelForm.valid) {
            return;
        }

        const brigadeName: string = this.brigadeNameControl.value;
        const calendarSymbolId: CalendarSymbolId = this.modelForm.get('calendarId').value;

        const createCalendarSymbolBrigadeRequest: CreateCalendarSymbolBrigadeRequest = {} as CreateCalendarSymbolBrigadeRequest;
        createCalendarSymbolBrigadeRequest.brigadeName = brigadeName;
        createCalendarSymbolBrigadeRequest.calendarSymbolId = calendarSymbolId;

        const instance: string = this.agencyStorageService.getInstance();

        const brigadeBody: BrigadeBody = {} as BrigadeBody;
        brigadeBody.brigadeName = brigadeName;
        brigadeBody.calendarSymbolId = calendarSymbolId;

        this.brigadeService.createBrigade(instance, brigadeBody).subscribe((response: Status) => {
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
