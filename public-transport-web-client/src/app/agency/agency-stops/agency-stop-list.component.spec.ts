import {AgencyStopListComponent} from './agency-stop-list.component';
import {AgencyStops} from '../../generated/public-transport-api';
import {ActivatedRoute, Router} from '@angular/router';
import {GoogleAnalyticsService} from '../../google-analytics.service';

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

    function component(): AgencyStopListComponent {
        const route = {snapshot: {data: {stops}}} as unknown as ActivatedRoute;
        const router = {url: '/agency/stops/used'} as Router;
        const analytics = {pageView: () => undefined} as unknown as GoogleAnalyticsService;
        const list = new AgencyStopListComponent(route, router, analytics);
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
});
