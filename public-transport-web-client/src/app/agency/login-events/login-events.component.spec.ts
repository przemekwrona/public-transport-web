import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideNoopAnimations} from '@angular/platform-browser/animations';
import {TranslocoTestingModule} from '@jsverse/transloco';
import {of, throwError} from 'rxjs';

import {LoginEventsComponent} from './login-events.component';
import {LoginEventsService} from '../../generated/public-transport-api';
import {NotificationService} from '../../shared/notification.service';

describe('LoginEventsComponent', () => {
    let component: LoginEventsComponent;
    let fixture: ComponentFixture<LoginEventsComponent>;
    let getLoginEvents: jasmine.Spy;
    let showError: jasmine.Spy;

    beforeEach(async () => {
        getLoginEvents = jasmine.createSpy('getLoginEvents').and.returnValue(of({
            events: [{username: 'pwrona', agencyCode: 'NEXTSTOP', success: true, login: true, refresh: false}],
            totalElements: 21
        }));
        showError = jasmine.createSpy('showError');

        await TestBed.configureTestingModule({
            imports: [
                LoginEventsComponent,
                TranslocoTestingModule.forRoot({
                    langs: {
                        pl: {
                            'admin.logins.title': 'Logowania',
                            'admin.logins.hint': 'Ostatnie próby',
                            'admin.logins.column.time': 'Czas',
                            'admin.logins.column.agency': 'Przewoźnik',
                            'admin.logins.column.username': 'Użytkownik',
                            'admin.logins.column.result': 'Wynik',
                            'admin.logins.column.login': 'Logowanie',
                            'admin.logins.column.refresh': 'Odświeżenie',
                            'admin.logins.success': 'Udane',
                            'admin.logins.failure': 'Nieudane',
                            'admin.logins.yes': 'Tak',
                            'admin.logins.no': 'Nie',
                            'admin.logins.loadFailed': 'Nie udało się pobrać logowań.'
                        }
                    },
                    translocoConfig: {availableLangs: ['pl'], defaultLang: 'pl'}
                })
            ],
            providers: [
                provideNoopAnimations(),
                {provide: LoginEventsService, useValue: {getLoginEvents}},
                {provide: NotificationService, useValue: {showError}}
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(LoginEventsComponent);
        component = fixture.componentInstance;
    });

    it('loads the requested page of login events', () => {
        component.load({first: 20, rows: 20});

        expect(getLoginEvents).toHaveBeenCalledWith(1, 20);
        expect(component.events.length).toBe(1);
        expect(component.totalRecords).toBe(21);
        expect(component.loading).toBeFalse();
    });

    it('reports a failed load', () => {
        getLoginEvents.and.returnValue(throwError(() => new Error('unavailable')));

        component.load({first: 0, rows: 20});

        expect(component.events).toEqual([]);
        expect(showError).toHaveBeenCalled();
        expect(component.loading).toBeFalse();
    });
});
