import {Component} from '@angular/core';
import {Router, RouterModule} from "@angular/router";
import {CommonModule} from "@angular/common";
import {AgencyAdminCreateAccountRequest, AgencyService} from "../../../generated/public-transport";
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {NotificationService} from "../../../shared/notification.service";

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

    constructor(private fb: FormBuilder, private agencyService: AgencyService, private notificationService: NotificationService, private router: Router) {
        this.createProfileForm = this.fb.group({
            companyName: ['', [Validators.required, Validators.minLength(3)]],
            companyCode: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(15)]],
            taxIdentificationNumber: ['', [Validators.required, Validators.minLength(3)]],
            accountName: ['', [Validators.required, Validators.minLength(3)]],
            street: ['', [Validators.required]],
            houseNumber: ['', [Validators.required]],
            flatNumber: ['', []],
            postalCode: ['', [Validators.required, Validators.minLength(4)]],
            postalCity: ['', [Validators.required]],
            accountPassword: ['', [Validators.required]],
            repeatedPassword: ['', [Validators.required]]
        });
    }

    public createProfile(): void {
        if (this.createProfileForm.valid) {
            const agency: AgencyAdminCreateAccountRequest = {} as AgencyAdminCreateAccountRequest;
            agency.companyName = this.createProfileForm.get('companyName').value;
            agency.companyCode = this.createProfileForm.get('companyCode').value;
            agency.taxNumber = this.createProfileForm.get('taxIdentificationNumber').value;

            agency.street = this.createProfileForm.get('street').value;
            agency.houseNumber = this.createProfileForm.get('houseNumber').value;
            agency.flatNumber = this.createProfileForm.get('flatNumber').value;
            agency.postalCode = this.createProfileForm.get('postalCode').value;
            agency.postalCity = this.createProfileForm.get('postalCity').value;

            agency.accountName = this.createProfileForm.get('accountName').value;
            agency.accountPassword = this.createProfileForm.get('accountPassword').value;

            this.agencyService.createNewAccount(agency).subscribe(response => {
                this.notificationService.showSuccess('Konto zostało utworzone');
                this.router.navigate(['/admin/agency/profiles']).then(() => {});
            });
        } else {
            this.notificationService.showError('Formularz został błędnie uzupełniony');
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

    public validityCheckMaxLength(fieldName: string): boolean {
        return this.validityCheck(fieldName)
            && this.createProfileForm.get(fieldName)?.errors?.['maxlength'];
    }

    public validityCheckMinLength(fieldName: string): boolean {
        return this.validityCheck(fieldName)
            && this.createProfileForm.get(fieldName)?.errors?.['minlength'];
    }

}
