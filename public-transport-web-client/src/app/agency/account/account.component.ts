import {ChangeDetectionStrategy, Component} from '@angular/core';
import {HttpErrorResponse} from '@angular/common/http';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {ButtonDirective} from 'primeng/button';
import {Card} from 'primeng/card';
import {Fluid} from 'primeng/fluid';
import {Message} from 'primeng/message';
import {Password} from 'primeng/password';
import {AuthService, ChangePasswordRequest, ErrorResponse} from '../../generated/public-transport-api';
import {NotificationService} from '../../shared/notification.service';
import {passwordChangeValidator} from './password-change.validator';

@Component({
    selector: 'app-account',
    imports: [
        ReactiveFormsModule,
        TranslocoPipe,
        Card,
        Fluid,
        Password,
        Message,
        ButtonDirective
    ],
    templateUrl: './account.component.html',
    styleUrl: './account.component.scss',
    changeDetection: ChangeDetectionStrategy.Eager
})
export class AccountComponent {

    public saving = false;
    public readonly form;

    constructor(
        formBuilder: FormBuilder,
        private authService: AuthService,
        private notificationService: NotificationService,
        private transloco: TranslocoService
    ) {
        this.form = formBuilder.group({
            currentPassword: ['', Validators.required],
            newPassword: ['', Validators.required],
            repeatedPassword: ['', Validators.required]
        }, {
            validators: passwordChangeValidator()
        });
    }

    public invalid(fieldName: 'currentPassword' | 'newPassword' | 'repeatedPassword'): boolean {
        const control = this.form.get(fieldName);
        return !!control && control.touched && control.invalid;
    }

    public required(fieldName: 'currentPassword' | 'newPassword' | 'repeatedPassword'): boolean {
        return this.invalid(fieldName) && !!this.form.get(fieldName)?.errors?.['required'];
    }

    public mismatch(): boolean {
        const next = this.form.get('newPassword');
        const repeat = this.form.get('repeatedPassword');
        return !!((next?.touched || repeat?.touched) && this.form.errors?.['passwordMismatch']);
    }

    public samePassword(): boolean {
        const next = this.form.get('newPassword');
        return !!(next?.touched && this.form.errors?.['samePassword']);
    }

    public changePassword(): void {
        this.form.markAllAsTouched();
        if (this.form.invalid || this.saving) {
            return;
        }
        const request: ChangePasswordRequest = {
            currentPassword: this.form.controls.currentPassword.value ?? '',
            newPassword: this.form.controls.newPassword.value ?? ''
        };
        this.saving = true;
        this.authService.changePassword(request).subscribe({
            next: () => {
                this.saving = false;
                this.form.reset();
                this.notificationService.showSuccess(this.transloco.translate('agency.account.password.success'));
            },
            error: (response: HttpErrorResponse) => {
                this.saving = false;
                this.notificationService.showError(this.passwordError(response));
            }
        });
    }

    private passwordError(response: HttpErrorResponse): string {
        const payload = response.error as ErrorResponse | undefined;
        if (payload?.errorCode === 'ERROR:202611060001') {
            return this.transloco.translate('agency.account.password.current.invalid');
        }
        if (payload?.errorCode === 'ERROR:202611060003') {
            return this.transloco.translate('agency.account.password.same');
        }
        if (payload?.errorCode === 'ERROR:202611060004') {
            return this.transloco.translate('agency.account.password.rejected');
        }
        return this.transloco.translate('agency.account.password.failed');
    }

}
