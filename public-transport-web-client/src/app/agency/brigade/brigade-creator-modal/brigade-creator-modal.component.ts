import {Component, OnInit} from '@angular/core';
import {MatDialogModule, MatDialogRef} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";
import {FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatFormFieldModule} from "@angular/material/form-field";
import {CommonModule} from "@angular/common";
import {
    BrigadeService, CalendarItemId,
    CalendarService,
    CalendarSymbolId, CreateBrigadeBody, CreateCalendarSymbolBrigadeRequest, CreateCalendarSymbolBrigadeResponse,
    GetCalendarItemResponse, RouteId1, Status
} from "../../../generated/public-transport-api";
import {AgencyStorageService} from "../../../auth/agency-storage.service";
import {MatInput} from "@angular/material/input";
import {MatSelectModule} from "@angular/material/select";
import {RouteSelectComponent} from "../../routes/route-select/route-select.component";
import moment from "moment";

@Component({
    selector: 'app-brigade-creator-modal',
    imports: [
        CommonModule,
        MatDialogModule,
        MatButtonModule,
        MatFormFieldModule,
        ReactiveFormsModule,
        MatInput,
        MatSelectModule,
        RouteSelectComponent
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
    public calendarsResponse: GetCalendarItemResponse = {};

    constructor(
        private formBuilder: FormBuilder,
        private dialogRef: MatDialogRef<BrigadeCreatorModalComponent>,
        private agencyStorageService: AgencyStorageService,
        private calendarService: CalendarService,
        private brigadeService: BrigadeService) {
        this.modelForm = this.formBuilder.group({
            brigadeName: ['', [Validators.required]],
            defaultRouteCode: ['', [Validators.required]],
            calendarId: [null, [Validators.required]]
        });
    }

    ngOnInit(): void {
        this.calendarService.getCalendarItems(this.agencyStorageService.getInstance())
            .subscribe((response: GetCalendarItemResponse) => this.calendarsResponse = response ?? {});
    }

    public createBrigade(): void {

        console.log(this.modelForm.valid);
        if (!this.modelForm.valid) {
            return;
        }

        const brigadeName: string = this.brigadeNameControl.value;
        const calendarSymbolId: CalendarItemId = this.modelForm.get('calendarId').value;

        const instance: string = this.agencyStorageService.getInstance();

        const brigadeBody: CreateBrigadeBody = {} as CreateBrigadeBody;
        brigadeBody.brigadeName = brigadeName;
        brigadeBody.calendarCode = calendarSymbolId.code;
        brigadeBody.selectedRouteCode = (this.defaultRouteCodeControl.value as RouteId1).routeCode;

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

    public isCalendarActive(startDate?: string, endDate?: string): boolean {
        if (!startDate || !endDate) {
            return false;
        }
        const today = moment().startOf('day');
        return today.isSameOrAfter(moment(startDate).startOf('day'))
            && today.isSameOrBefore(moment(endDate).startOf('day'));
    }

    public isCalendarUpcoming(startDate?: string): boolean {
        return this.daysUntil(startDate) > 0;
    }

    public isCalendarExpired(endDate?: string): boolean {
        return this.daysUntil(endDate) < 0;
    }

    public calendarUpcomingLabel(startDate?: string): string {
        const days: number = this.daysUntil(startDate);
        return `aktywny za ${this.formatDays(days)}`;
    }

    public calendarExpiredLabel(endDate?: string): string {
        const days: number = Math.abs(this.daysUntil(endDate));
        return `nieaktywny od ${this.formatDays(days)}`;
    }

    private daysUntil(date?: string): number {
        if (!date) {
            return 0;
        }
        return moment(date).startOf('day').diff(moment().startOf('day'), 'days');
    }

    private formatDays(days: number): string {
        return days === 1 ? '1 dzień' : `${days} dni`;
    }

    get brigadeNameControl(): FormControl {
        return this.modelForm.get('brigadeName') as FormControl;
    }

    get defaultRouteCodeControl(): FormControl {
        return this.modelForm.get('defaultRouteCode') as FormControl;
    }

}
