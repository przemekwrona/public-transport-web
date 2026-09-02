import {Component, OnInit} from '@angular/core';
import {MatDialogModule, MatDialogRef} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";
import {FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatFormFieldModule} from "@angular/material/form-field";
import {CommonModule} from "@angular/common";
import {
    BrigadeService,
    CalendarService,
    CalendarSymbolId, CreateBrigadeBody, CreateCalendarSymbolBrigadeRequest, CreateCalendarSymbolBrigadeResponse,
    GetCalendarsResponse, RouteId, RouteId1, Status
} from "../../../generated/public-transport-api";
import {AgencyStorageService} from "../../../auth/agency-storage.service";
import {MatInput} from "@angular/material/input";
import {MatSelectModule} from "@angular/material/select";
import {RouteSelectComponent} from "../../routes/route-select/route-select.component";

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
    public calendarsResponse: GetCalendarsResponse = {};

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
        this.calendarService.getCalendars(this.agencyStorageService.getInstance())
            .subscribe(response => this.calendarsResponse = response ?? {});
    }

    public createBrigade(): void {

        console.log(this.modelForm.valid);
        if (!this.modelForm.valid) {
            return;
        }

        const brigadeName: string = this.brigadeNameControl.value;
        const calendarSymbolId: CalendarSymbolId = this.modelForm.get('calendarId').value;

        const createCalendarSymbolBrigadeRequest: CreateCalendarSymbolBrigadeRequest = {} as CreateCalendarSymbolBrigadeRequest;
        createCalendarSymbolBrigadeRequest.brigadeName = brigadeName;
        createCalendarSymbolBrigadeRequest.calendarSymbolId = calendarSymbolId;

        const instance: string = this.agencyStorageService.getInstance();

        const brigadeBody: CreateBrigadeBody = {} as CreateBrigadeBody;
        brigadeBody.brigadeName = brigadeName;
        brigadeBody.calendarCode = calendarSymbolId.calendarItemId.code;
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

    get brigadeNameControl(): FormControl {
        return this.modelForm.get('brigadeName') as FormControl;
    }

    get defaultRouteCodeControl(): FormControl {
        return this.modelForm.get('defaultRouteCode') as FormControl;
    }

}
