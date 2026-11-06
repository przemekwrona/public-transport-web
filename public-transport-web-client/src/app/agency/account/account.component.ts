import {animate, style, transition, trigger} from '@angular/animations';
import {ChangeDetectionStrategy, Component} from '@angular/core';
import {HttpErrorResponse} from '@angular/common/http';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {ButtonDirective} from 'primeng/button';
import {Dialog} from 'primeng/dialog';
import {Fluid} from 'primeng/fluid';
import {Message} from 'primeng/message';
import {Password} from 'primeng/password';
import {AuthService, ChangePasswordRequest, ErrorResponse} from '../../generated/public-transport-api';
import {NotificationService} from '../../shared/notification.service';
import {passwordChangeValidator} from './password-change.validator';

const MIN_PASSWORD_LENGTH = 5;

@Component({
    selector: 'app-account',
    imports: [
        ReactiveFormsModule,
        TranslocoPipe,
        Dialog,
        Fluid,
        Password,
        Message,
        ButtonDirective
    ],
    templateUrl: './account.component.html',
    styleUrl: './account.component.scss',
    changeDetection: ChangeDetectionStrategy.Eager,
    animations: [
        trigger('repeatPassword', [
            transition(':enter', [
                style({opacity: 0, height: 0, overflow: 'hidden'}),
                animate('280ms ease-out', style({opacity: 1, height: '*'}))
            ]),
            transition(':leave', [
                style({overflow: 'hidden'}),
                animate('200ms ease-in', style({opacity: 0, height: 0}))
            ])
        ])
    ]
})
export class AccountComponent {

    public saving = false;
    public confirming = false;
    public readonly form;

    constructor(
        formBuilder: FormBuilder,
        private authService: AuthService,
        private notificationService: NotificationService,
        private transloco: TranslocoService
    ) {
        this.form = formBuilder.group({
            currentPassword: ['', Validators.required],
            newPassword: ['', [Validators.required, Validators.minLength(MIN_PASSWORD_LENGTH)]],
            repeatedPassword: ['', Validators.required]
        }, {
            validators: passwordChangeValidator()
        });
        this.form.controls.newPassword.valueChanges.pipe(takeUntilDestroyed()).subscribe(value => {
            if ((value?.length ?? 0) >= MIN_PASSWORD_LENGTH) {
                return;
            }
            if (this.form.controls.repeatedPassword.value) {
                this.form.controls.repeatedPassword.setValue('');
            }
        });
    }

    public get showRepeat(): boolean {
        return (this.form.controls.newPassword.value?.length ?? 0) >= MIN_PASSWORD_LENGTH;
    }

    public invalid(fieldName: 'currentPassword' | 'newPassword' | 'repeatedPassword'): boolean {
        const control = this.form.get(fieldName);
        return !!control && control.touched && control.invalid;
    }

    public required(fieldName: 'currentPassword' | 'newPassword' | 'repeatedPassword'): boolean {
        return this.invalid(fieldName) && !!this.form.get(fieldName)?.errors?.['required'];
    }

    public tooShort(): boolean {
        const control = this.form.controls.newPassword;
        return !!(control.touched && control.hasError('minlength'));
    }

    public mismatch(): boolean {
        const repeat = this.form.controls.repeatedPassword;
        return !!(repeat.touched && this.form.errors?.['passwordMismatch']);
    }

    public samePassword(): boolean {
        const current = this.form.controls.currentPassword;
        return !!(current.touched && this.form.errors?.['samePassword']);
    }

    public requestCurrentPassword(): void {
        const next = this.form.controls.newPassword;
        const repeat = this.form.controls.repeatedPassword;
        next.markAsTouched();
        if (this.showRepeat) {
            repeat.markAsTouched();
        }
        const repeated = this.showRepeat && repeat.valid && !this.form.hasError('passwordMismatch');
        if (this.saving || next.invalid || !repeated) {
            return;
        }
        this.form.controls.currentPassword.reset('');
        this.confirming = true;
    }

    public changePassword(): void {
        const current = this.form.controls.currentPassword;
        current.markAsTouched();
        if (this.saving || current.invalid || this.form.hasError('samePassword') || this.form.hasError('passwordMismatch')) {
            return;
        }
        const request: ChangePasswordRequest = {
            currentPassword: current.value ?? '',
            newPassword: this.form.controls.newPassword.value ?? ''
        };
        this.saving = true;
        this.authService.changePassword(request).subscribe({
            next: () => {
                this.saving = false;
                this.confirming = false;
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
