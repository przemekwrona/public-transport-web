import {Component} from '@angular/core';
import {RouterModule} from "@angular/router";
import {CommonModule} from "@angular/common";
import {AgencyAdminCreateAccountRequest, AgencyService} from "../../../generated/public-transport";
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";

@Component({
    selector: 'app-create-profile',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        RouterModule,
        FormsModule
    ],
    providers: [],
    templateUrl: './create-profile.component.html',
    styleUrl: './create-profile.component.scss'
})
export class CreateProfileComponent {

    public createProfileForm: FormGroup;

    public agency: AgencyAdminCreateAccountRequest = {} as AgencyAdminCreateAccountRequest;

    public repeatedPassword: string = '';

    constructor(private fb: FormBuilder, private agencyService: AgencyService) {
        this.createProfileForm = this.fb.group({
            companyName: ['', [Validators.required, Validators.minLength(3)]],
            companyCode: ['', [Validators.required, Validators.minLength(3)]],
            taxIdentificationNumber: ['', [Validators.required, Validators.minLength(3)]],
            accountName: ['', [Validators.required, Validators.minLength(3)]],
            accountPassword: ['', [Validators.required]],
            repeatedPassword: ['', [Validators.required]]
        });
    }

    public createProfile(): void {
        if (this.createProfileForm.valid) {
            this.agencyService.createNewAccount(this.agency).subscribe(response => {
            });
        } else {
            this.createProfileForm.markAllAsTouched();
        }
    }

    public validityCheck(fieldName: string): boolean {
        return this.createProfileForm.get(fieldName)?.touched
            && this.createProfileForm.get(fieldName)?.invalid;
    }

    public validityCheckRequired(fieldName: string): boolean {
        return this.validityCheck(fieldName)
            && this.createProfileForm.get(fieldName)?.errors?.['required'];
    }

    public validityCheckMinLength(fieldName: string): boolean {
        return this.validityCheck(fieldName)
            && this.createProfileForm.get(fieldName)?.errors?.['minlength'];
    }

}
