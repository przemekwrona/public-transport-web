import {ComponentFixture, TestBed} from '@angular/core/testing';
import {HttpErrorResponse} from '@angular/common/http';
import {provideNoopAnimations} from '@angular/platform-browser/animations';
import {TranslocoTestingModule} from '@jsverse/transloco';
import {of, throwError} from 'rxjs';
import {AuthService} from '../../generated/public-transport-api';
import {NotificationService} from '../../shared/notification.service';
import {AccountComponent} from './account.component';

describe('AccountComponent', () => {
    let component: AccountComponent;
    let fixture: ComponentFixture<AccountComponent>;
    let changePassword: jasmine.Spy;
    let showSuccess: jasmine.Spy;
    let showError: jasmine.Spy;

    beforeEach(async () => {
        changePassword = jasmine.createSpy('changePassword').and.returnValue(of({status: 'SUCCESS'}));
        showSuccess = jasmine.createSpy('showSuccess');
        showError = jasmine.createSpy('showError');

        await TestBed.configureTestingModule({
            imports: [
                AccountComponent,
                TranslocoTestingModule.forRoot({
                    langs: {
                        pl: {
                            'agency.account.title': 'Moje Konto',
                            'agency.account.subtitle': 'Konto',
                            'agency.account.password.title': 'Zmiana hasła',
                            'agency.account.password.current': 'Obecne hasło',
                            'agency.account.password.current.required': 'Podaj obecne hasło',
                            'agency.account.password.current.invalid': 'Obecne hasło jest nieprawidłowe',
                            'agency.account.password.new': 'Nowe hasło',
                            'agency.account.password.new.required': 'Podaj nowe hasło',
                            'agency.account.password.new.minLength': 'Hasło musi mieć co najmniej 5 znaków',
                            'agency.account.password.repeat': 'Powtórz hasło',
                            'agency.account.password.repeat.required': 'Powtórz nowe hasło',
                            'agency.account.password.mismatch': 'Hasła nie są takie same',
                            'agency.account.password.same': 'Nowe hasło musi różnić się od obecnego',
                            'agency.account.password.submit': 'Zmień hasło',
                            'agency.account.password.success': 'Hasło zostało zmienione',
                            'agency.account.password.failed': 'Nie udało się zmienić hasła',
                            'agency.account.password.rejected': 'Nowe hasło nie spełnia wymagań'
                        }
                    },
                    translocoConfig: {availableLangs: ['pl'], defaultLang: 'pl'}
                })
            ],
            providers: [
                provideNoopAnimations(),
                {provide: AuthService, useValue: {changePassword}},
                {provide: NotificationService, useValue: {showSuccess, showError}}
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(AccountComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('shows the repeat field only after five characters', () => {
        component.form.controls.newPassword.setValue('abcd');
        expect(component.showRepeat).toBeFalse();

        component.form.controls.newPassword.setValue('abcde');
        expect(component.showRepeat).toBeTrue();
    });

    it('does not open the current-password dialog until the new password is repeated', () => {
        component.form.controls.newPassword.setValue('welcome2');
        component.form.controls.repeatedPassword.setValue('other');
        component.requestCurrentPassword();

        expect(component.confirming).toBeFalse();
        expect(changePassword).not.toHaveBeenCalled();
        expect(component.mismatch()).toBeTrue();
    });

    it('asks for the current password after the new password is repeated', () => {
        component.form.controls.newPassword.setValue('welcome2');
        component.form.controls.repeatedPassword.setValue('welcome2');
        component.requestCurrentPassword();

        expect(component.confirming).toBeTrue();
        expect(changePassword).not.toHaveBeenCalled();
    });

    it('changes the password and clears the form', () => {
        component.form.setValue({
            currentPassword: 'welcome1',
            newPassword: 'welcome2',
            repeatedPassword: 'welcome2'
        });

        component.changePassword();

        expect(changePassword).toHaveBeenCalledWith({
            currentPassword: 'welcome1',
            newPassword: 'welcome2'
        });
        expect(showSuccess).toHaveBeenCalledWith('Hasło zostało zmienione');
        expect(component.confirming).toBeFalse();
        expect(component.form.controls.currentPassword.value).toBeNull();
        expect(component.saving).toBeFalse();
    });

    it('shows that the current password is wrong', () => {
        changePassword.and.returnValue(throwError(() => new HttpErrorResponse({
            status: 400,
            error: {errorCode: 'ERROR:202611060001', message: 'Current password is invalid'}
        })));
        component.form.setValue({
            currentPassword: 'wrong',
            newPassword: 'welcome2',
            repeatedPassword: 'welcome2'
        });

        component.changePassword();

        expect(showError).toHaveBeenCalledWith('Obecne hasło jest nieprawidłowe');
        expect(component.form.controls.currentPassword.value).toBe('wrong');
        expect(component.saving).toBeFalse();
    });
});
