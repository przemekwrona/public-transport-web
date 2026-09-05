import {TestBed} from '@angular/core/testing';
import {ActivatedRouteSnapshot, ResolveFn, RouterStateSnapshot} from '@angular/router';
import {Observable, of} from 'rxjs';

import {defaultRouteResolver} from './default-route.resolver';
import {
    BrigadeService,
    GetAllTripsResponse,
    TripService
} from '../../../generated/public-transport-api';
import {AgencyStorageService} from '../../../auth/agency-storage.service';

describe('defaultTripResolver', () => {
    const executeResolver: ResolveFn<Observable<GetAllTripsResponse>> = (...resolverParameters) =>
        TestBed.runInInjectionContext(() => defaultRouteResolver(...resolverParameters));

    const tripsResponse: GetAllTripsResponse = {
        lines: [{
            trips: [
                {isMainVariant: true, tripId: {tripCode: 'T1'}},
                {isMainVariant: false, tripId: {tripCode: 'T2'}}
            ]
        }]
    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                {provide: AgencyStorageService, useValue: {getInstance: () => 'test-agency'}},
                {
                    provide: BrigadeService,
                    useValue: {
                        getBrigadeDetails: () => of({
                            brigade: {defaultRouteCode: 'R1'}
                        })
                    }
                },
                {
                    provide: TripService,
                    useValue: {
                        getTripsByRouteAndFilterLineOrName: jasmine.createSpy('getTripsByRouteAndFilterLineOrName')
                            .and.returnValue(of(tripsResponse))
                    }
                }
            ]
        });
    });

    it('should be created', () => {
        expect(executeResolver).toBeTruthy();
    });

    it('should return all trips for the brigade default route', (done) => {
        const route = {
            paramMap: {get: (key: string) => key === 'brigadeCode' ? 'B1' : null}
        } as ActivatedRouteSnapshot;
        const tripService = TestBed.inject(TripService);

        (executeResolver(route, {} as RouterStateSnapshot) as Observable<GetAllTripsResponse>)
            .subscribe(response => {
                expect(tripService.getTripsByRouteAndFilterLineOrName).toHaveBeenCalledWith('test-agency', 'R1');
                expect(response).toEqual(tripsResponse);
                expect(response.lines?.[0]?.trips?.length).toBe(2);
                done();
            });
    });
});
