import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {HttpErrorResponse} from '@angular/common/http';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgxMaskDirective, provideNgxMask} from 'ngx-mask';
import {ButtonDirective} from 'primeng/button';
import {Fluid} from 'primeng/fluid';
import {InputText} from 'primeng/inputtext';
import {Message} from 'primeng/message';
import {AgencyAddress, AgencyService} from '../../../generated/public-transport-api';
import {LoginService} from '../../../auth/login.service';
import {NotificationService} from '../../../shared/notification.service';

@Component({
    selector: 'app-agency-address',
    imports: [
        ReactiveFormsModule,
        TranslocoPipe,
        NgxMaskDirective,
        ButtonDirective,
        Fluid,
        InputText,
        Message
    ],
    templateUrl: './agency-address.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    providers: [
        AgencyService,
        provideNgxMask()
    ]
})
export class AgencyAddressComponent {

    public saving = false;
    public submitted = false;
    public readonly form;

    constructor(
        formBuilder: FormBuilder,
        private agencyService: AgencyService,
        private loginService: LoginService,
        private notificationService: NotificationService,
        private transloco: TranslocoService
    ) {
        this.form = formBuilder.group({
            street: ['', Validators.required],
            houseNumber: ['', Validators.required],
            flatNumber: ['', Validators.maxLength(5)],
            postalCode: ['', Validators.required],
            postalCity: ['', Validators.required]
        });
    }

    @Input()
    set address(value: AgencyAddress | null) {
        if (!value) {
            return;
        }
        this.form.patchValue({
            street: value.street ?? '',
            houseNumber: value.houseNumber ?? '',
            flatNumber: value.flatNumber ?? '',
            postalCode: value.postalCode ?? '',
            postalCity: value.postalCity ?? ''
        });
    }

    public save(): void {
        this.submitted = true;
        if (this.form.invalid) {
            this.notificationService.showError(this.transloco.translate('agency.profile.address.invalid'));
            return;
        }
        if (this.saving) {
            return;
        }
        const agencyAddress: AgencyAddress = {
            street: this.form.controls.street.value ?? '',
            houseNumber: this.form.controls.houseNumber.value ?? '',
            flatNumber: this.form.controls.flatNumber.value ?? '',
            postalCode: this.form.controls.postalCode.value ?? '',
            postalCity: this.form.controls.postalCity.value ?? ''
        };
        this.saving = true;
        this.agencyService.updateAgencyAddress(this.loginService.getInstance(), agencyAddress).subscribe({
            next: () => {
                this.saving = false;
                this.notificationService.showSuccess(this.transloco.translate('agency.profile.address.saved'));
            },
            error: (_response: HttpErrorResponse) => {
                this.saving = false;
                this.notificationService.showError(this.transloco.translate('agency.profile.address.failed'));
            }
        });
    }

    public invalid(controlName: 'street' | 'houseNumber' | 'flatNumber' | 'postalCode' | 'postalCity'): boolean {
        const control = this.form.controls[controlName];
        return this.submitted && control.invalid;
    }

    public hasError(controlName: 'street' | 'houseNumber' | 'flatNumber' | 'postalCode' | 'postalCity', errorName: string): boolean {
        return this.invalid(controlName) && !!this.form.controls[controlName].errors?.[errorName];
    }
}
