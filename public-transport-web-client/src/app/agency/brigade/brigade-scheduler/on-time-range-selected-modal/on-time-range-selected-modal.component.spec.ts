import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {provideNoopAnimations} from '@angular/platform-browser/animations';
import {TranslocoTestingModule} from '@jsverse/transloco';
import {of, throwError} from 'rxjs';
import {RouteDetails, RouteService, TripService} from '../../../../generated/public-transport-api';
import {AgencyStorageService} from '../../../../auth/agency-storage.service';
import {NotificationService} from '../../../../shared/notification.service';
import {OnTimeRangeSelectedModalComponent} from './on-time-range-selected-modal.component';

describe('OnTimeRangeSelectedModalComponent', () => {
    let component: OnTimeRangeSelectedModalComponent;
    let fixture: ComponentFixture<OnTimeRangeSelectedModalComponent>;
    let getTrips: jasmine.Spy;
    let showError: jasmine.Spy;

    const defaultLine = {
        route: {
            routeCode: 'R1',
            routeId: {line: '1', name: 'Kielce_Centrum', routeCode: 'R1'},
            originTerritory: {name: 'Kielce'},
            destinationTerritory: {name: 'Warszawa'}
        },
        trips: []
    };

    beforeEach(async () => {
        getTrips = jasmine.createSpy('getTripsByRouteAndFilterLineOrName').and.returnValue(of({lines: []}));
        showError = jasmine.createSpy('showError');

        await TestBed.configureTestingModule({
            imports: [
                OnTimeRangeSelectedModalComponent,
                TranslocoTestingModule.forRoot({
                    langs: {
                        pl: {
                            agency: {
                                brigade: {
                                    addTrip: {
                                        description: 'Wybierz kierunek i profil czasu przejazdu.'
                                    },
                                    direction: 'z {{from}} do {{to}}',
                                    loadMoreRoutes: {
                                        link: 'Załaduj więcej linii',
                                        label: 'Numer i nazwa linii',
                                        placeholder: 'Wybierz linię',
                                        search: 'Szukaj po numerze lub nazwie linii',
                                        empty: 'Nie znaleziono linii',
                                        loading: 'Wczytywanie linii…',
                                        failed: 'Nie udało się wczytać linii.'
                                    }
                                }
                            }
                        }
                    },
                    translocoConfig: {availableLangs: ['pl'], defaultLang: 'pl'}
                })
            ],
            providers: [
                provideNoopAnimations(),
                {provide: MatDialogRef, useValue: {close: () => undefined}},
                {provide: AgencyStorageService, useValue: {getInstance: () => 'agency'}},
                {provide: TripService, useValue: {getTripsByRouteAndFilterLineOrName: getTrips}},
                {provide: RouteService, useValue: {getRoutes: () => of({items: []})}},
                {provide: NotificationService, useValue: {showError}},
                {
                    provide: MAT_DIALOG_DATA,
                    useValue: {
                        start: '2026-11-06T08:00:00',
                        end: '2026-11-06T09:00:00',
                        resourceId: 'brigade-1',
                        defaultRoutes: {lines: [defaultLine]}
                    }
                }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(OnTimeRangeSelectedModalComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('shows the default line and a link to load one more line', () => {
        const text = fixture.nativeElement.textContent as string;
        expect(text).toContain('1');
        expect(text).toContain('Kielce Centrum');
        expect(text).toContain('z Kielce do Warszawa');
        expect(text).toContain('z Warszawa do Kielce');
        expect(text).not.toContain('FRONT');
        expect(text).toContain('Wybierz kierunek i profil czasu przejazdu.');
        expect(text).not.toContain('oficjalne pozwolenie');
        expect(text).toContain('Załaduj więcej linii');
        expect(fixture.nativeElement.querySelector('app-route-select')).toBeNull();
    });

    it('loads profile tiles for a single extra line', () => {
        const extraLine: RouteDetails = {
            route: {routeCode: 'R2', routeId: {line: '12', name: 'Dworzec_Osiedle', routeCode: 'R2'}},
            trips: [{
                tripId: {tripCode: 'T2', variantMode: 'FRONT'},
                isMainVariant: true,
                origin: 'Dworzec',
                destination: 'Osiedle',
                profile: [{trafficMode: 'NORMAL', travelTime: 600}]
            }]
        };
        getTrips.and.returnValue(of({lines: [extraLine]}));

        component.openRoutePicker();
        fixture.detectChanges();
        component.modelForm.controls.extraRouteId.setValue({line: '12', name: 'Dworzec_Osiedle', routeCode: 'R2'});
        fixture.detectChanges();

        expect(getTrips).toHaveBeenCalledOnceWith('agency', 'R2');
        expect(component.extraLines).toEqual([extraLine]);
        expect(fixture.nativeElement.textContent).toContain('Normalny ruch');
    });

    it('does not request the default line again', () => {
        component.modelForm.controls.extraRouteId.setValue({line: '1', name: 'Kielce_Centrum', routeCode: 'R1'});

        expect(getTrips).not.toHaveBeenCalled();
        expect(component.extraLines).toEqual([]);
    });

    it('reports a failure when the extra line cannot be loaded', () => {
        getTrips.and.returnValue(throwError(() => new Error('missing')));

        component.modelForm.controls.extraRouteId.setValue({line: '9', name: 'Nowa', routeCode: 'R9'});

        expect(showError).toHaveBeenCalledOnceWith('Nie udało się wczytać linii.');
        expect(component.extraLines).toEqual([]);
        expect(component.loadingExtraRoute).toBeFalse();
    });
});
