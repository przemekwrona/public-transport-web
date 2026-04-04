import {Component, OnInit} from '@angular/core';
import {Router, RouterModule} from "@angular/router";
import {CommonModule} from "@angular/common";
import {
    AgencyAdminCreateAccountRequest,
    AgencyService, AppUser,
    UsersService
} from "../../../generated/public-transport-api";
import {FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {NotificationService} from "../../../shared/notification.service";
import {MatInputModule} from "@angular/material/input";
import {debounceTime, distinctUntilChanged, map, Observable, of, startWith, switchAll, switchMap} from "rxjs";
import {MatSelectModule} from "@angular/material/select";

@Component({
    selector: 'app-create-profile',
    imports: [
        CommonModule,
        ReactiveFormsModule,
        RouterModule,
        FormsModule,
        MatInputModule,
        MatSelectModule
    ],
    providers: [],
    templateUrl: './create-profile.component.html',
    styleUrl: './create-profile.component.scss'
})
export class CreateProfileComponent implements OnInit {

    public searchAppUser: FormControl<string> = new FormControl('');

    public filteredAppUsers: AppUser[] = [];

    public createProfileForm: FormGroup;

    public agency: AgencyAdminCreateAccountRequest = {} as AgencyAdminCreateAccountRequest;

    constructor(private fb: FormBuilder, private agencyService: AgencyService, private notificationService: NotificationService, private router: Router, private usersService: UsersService) {
        this.createProfileForm = this.fb.group({
            companyName: ['', [Validators.required, Validators.minLength(3)]],
            companyCode: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(15)]],
            taxIdentificationNumber: ['', [Validators.required, Validators.minLength(3)]],
            accountName: ['', [Validators.required, Validators.minLength(3)]],
            street: ['', [Validators.required]],
            houseNumber: ['', [Validators.required]],
            flatNumber: ['', []],
            postalCode: ['', [Validators.required, Validators.minLength(4)]],
            postalCity: ['', [Validators.required]]
        });
    }

    ngOnInit(): void {
        this.searchAppUser.valueChanges.pipe(
            debounceTime(300),
            distinctUntilChanged(),
            switchMap(value => this._filter(value || ''))).subscribe({
            next: (users) => this.filteredAppUsers = users,
            error: (err) => {
                console.error('Filtering error:', err);
                this.filteredAppUsers = []; // Reset on error
            }
        });
    }

    private _filter(value: string): Observable<AppUser[]> {
        const filterValue = value.toLowerCase();
        return this.usersService.getAppUsers().pipe(
            map(response => response.users)
        );
    }

    public createProfile(): void {
        if (this.createProfileForm.valid) {
            const agency: AgencyAdminCreateAccountRequest = {} as AgencyAdminCreateAccountRequest;
            agency.companyName = this.createProfileForm.get('companyName').value;
            agency.companyCode = this.createProfileForm.get('companyCode').value;
            agency.taxNumber = this.createProfileForm.get('taxIdentificationNumber').value;

            agency.accountName = this.createProfileForm.get('accountName').value;

            agency.street = this.createProfileForm.get('street').value;
            agency.houseNumber = this.createProfileForm.get('houseNumber').value;
            agency.flatNumber = this.createProfileForm.get('flatNumber').value;
            agency.postalCode = this.createProfileForm.get('postalCode').value;
            agency.postalCity = this.createProfileForm.get('postalCity').value;

            this.agencyService.createNewAccount(agency).subscribe(response => {
                this.notificationService.showSuccess('Konto zostało utworzone');
                this.router.navigate(['/admin/agency/profiles']).then(() => {});
            });
        } else {
            this.notificationService.showError('Formularz został błędnie uzupełniony');
            this.createProfileForm.markAllAsTouched();
        }
    }

    public findAppUserByUsername(username: string): AppUser | null {
        return this.filteredAppUsers.find(user => user.username === username) ?? null;
    }

    public getCompanyNameControl(): FormControl<string> {
        return this.createProfileForm.get("companyName") as FormControl<string>;
    }

    public getCompanyCodeControl(): FormControl<string> {
        return this.createProfileForm.get("companyCode") as FormControl<string>;
    }

    public getTaxIdentificationNumberControl(): FormControl<string> {
        return this.createProfileForm.get("taxIdentificationNumber") as FormControl<string>;
    }

    public getAccountNameControl(): FormControl<string> {
        return this.createProfileForm.get("accountName") as FormControl<string>;
    }

    public getStreetNumberControl(): FormControl<string> {
        return this.createProfileForm.get("street") as FormControl<string>;
    }

    public getHouseNumberControl(): FormControl<string> {
        return this.createProfileForm.get("houseNumber") as FormControl<string>;
    }

    public getFlatNumberControl(): FormControl<string> {
        return this.createProfileForm.get("flatNumber") as FormControl<string>;
    }

    public getPostalCodeControl(): FormControl<string> {
        return this.createProfileForm.get("postalCode") as FormControl<string>;
    }

    public getPostalCityControl(): FormControl<string> {
        return this.createProfileForm.get("postalCity") as FormControl<string>;
    }

}
