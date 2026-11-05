import {AgencyStopListComponent} from './agency-stop-list.component';
import {AgencyStopTimetableService, AgencyStops} from '../../generated/public-transport-api';
import {ActivatedRoute, Router} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {of, Subject, throwError} from 'rxjs';
import {TranslocoService} from '@jsverse/transloco';
import {LoginService} from '../../auth/login.service';
import {GoogleAnalyticsService} from '../../google-analytics.service';
import {NotificationService} from '../../shared/notification.service';
import {AgencyStopNameDialogResult} from './agency-stop-name-modal/agency-stop-name-modal.component';

describe('AgencyStopListComponent', () => {
    const stops: AgencyStops = {
        items: [
            {
                stop: {id: 1, name: 'Rynek'},
                routes: [{routeCode: 'A1', line: '12', name: 'Centrum'}],
                trips: [{routeCode: 'A1', line: '12', tripCode: '001', variantMode: 'FRONT', headsign: 'Dworzec'}]
            },
            {
                stop: {id: 2, name: 'Dworzec'},
                routes: [{routeCode: 'B2', line: '5', name: 'Osiedle'}],
                trips: [{routeCode: 'B2', line: '5', tripCode: '002', variantMode: 'BACK', headsign: 'Rynek'}]
            }
        ]
    };

    function component(
        dialog: MatDialog = {open: () => ({afterClosed: () => of(undefined)})} as unknown as MatDialog,
        timetable: AgencyStopTimetableService = {downloadAgencyStopTimetable: () => of(new Blob())} as unknown as AgencyStopTimetableService,
        notification: NotificationService = {showError: () => undefined} as unknown as NotificationService
    ): AgencyStopListComponent {
        const route = {snapshot: {data: {stops: structuredClone(stops)}}} as unknown as ActivatedRoute;
        const router = {url: '/agency/stops/used'} as Router;
        const analytics = {pageView: () => undefined} as unknown as GoogleAnalyticsService;
        const login = {getInstance: () => 'WAWA'} as LoginService;
        const transloco = {translate: (key: string) => key} as TranslocoService;
        const list = new AgencyStopListComponent(route, router, analytics, dialog, login, timetable, notification, transloco);
        list.ngOnInit();
        return list;
    }

    it('lists every stop used by the agency', () => {
        const list = component();

        expect(list.visibleStops.map(item => item.stop?.name)).toEqual(['Rynek', 'Dworzec']);
        const market = list.visibleStops[0];
        expect(list.tripsFor(market, market.routes![0]).map(trip => trip.tripCode)).toEqual(['001']);
    });

    it('filters stops by name, line and trip', () => {
        const list = component();

        list.query = 'osiedle';
        expect(list.visibleStops.map(item => item.stop?.name)).toEqual(['Dworzec']);

        list.query = '001';
        expect(list.visibleStops.map(item => item.stop?.name)).toEqual(['Rynek']);

        list.query = 'brak';
        expect(list.visibleStops).toEqual([]);
        expect(list.hasStops()).toBeTrue();
    });

    it('opens the name editor and keeps the agency name on the list', () => {
        const closed = new Subject<AgencyStopNameDialogResult | undefined>();
        const open = jasmine.createSpy('open').and.returnValue({afterClosed: () => closed.asObservable()});
        const list = component({open} as unknown as MatDialog);

        list.openNameEditor(list.visibleStops[0]);

        expect(open).toHaveBeenCalled();
        const config = open.calls.mostRecent().args[1];
        expect(config.data).toEqual({stopId: 1, name: 'Rynek', lat: undefined, lon: undefined});

        closed.next({stopId: 1, name: 'Plac'});
        expect(list.visibleStops[0].stop?.name).toBe('Plac');
    });

    it('downloads the timetable for the whole stop', () => {
        const download = jasmine.createSpy('download').and.returnValue(of(new Blob(['%PDF'], {type: 'application/pdf'})));
        const list = component(undefined as unknown as MatDialog, {downloadAgencyStopTimetable: download} as unknown as AgencyStopTimetableService);
        spyOn(URL, 'createObjectURL').and.returnValue('blob:timetable');
        spyOn(URL, 'revokeObjectURL');
        const click = spyOn(HTMLAnchorElement.prototype, 'click');

        list.downloadTimetable(list.visibleStops[0]);

        expect(download).toHaveBeenCalledWith('WAWA', 1, 'body', false, {httpHeaderAccept: 'application/pdf'});
        expect(click).toHaveBeenCalled();
        expect(list.downloadingStopId).toBeNull();
    });

    it('reports a failed timetable download', () => {
        const download = jasmine.createSpy('download').and.returnValue(throwError(() => new Error('pdf')));
        const showError = jasmine.createSpy('showError');
        const list = component(
            undefined as unknown as MatDialog,
            {downloadAgencyStopTimetable: download} as unknown as AgencyStopTimetableService,
            {showError} as unknown as NotificationService
        );

        list.downloadTimetable(list.visibleStops[0]);

        expect(showError).toHaveBeenCalledWith('agency.stops.used.download.failed');
        expect(list.downloadingStopId).toBeNull();
    });
});
