import {Component} from '@angular/core';
import {MatDialogModule, MatDialogRef} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";
import {FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {SharedModule} from "../../shared/shared.module";
import {CommonModule} from "@angular/common";

@Component({
    selector: 'app-calendar-item-modal',
    imports: [
        CommonModule,
        MatDialogModule,
        MatButtonModule,
        MatFormFieldModule,
        MatDatepickerModule,
        FormsModule,
        ReactiveFormsModule,
        SharedModule
    ],
    templateUrl: './calendar-item-modal.component.html',
    styleUrl: './calendar-item-modal.component.scss'
})
export class CalendarItemModalComponent {

    public modelForm: FormGroup;
    public isSubmitted: boolean = false;

    get startDateControl(): FormControl {
        return this.modelForm.get('startDate') as FormControl;
    }

    get endDateControl(): FormControl {
        return this.modelForm.get('endDate') as FormControl;
    }

    constructor(private formBuilder: FormBuilder, private dialogRef: MatDialogRef<CalendarItemModalComponent>) {
        this.modelForm = this.formBuilder.group({
            startDate: ['', [Validators.required]],
            endDate: ['', [Validators.required]]
        });
    }

    public createCalendarItem(): void {
        this.isSubmitted = true;

        if (this.modelForm.valid) {
            this.dialogRef.close();
        }
    }

}
