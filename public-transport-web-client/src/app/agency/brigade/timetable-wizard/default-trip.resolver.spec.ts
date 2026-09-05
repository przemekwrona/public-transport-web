import {TestBed} from '@angular/core/testing';
import {ResolveFn} from '@angular/router';
import {Observable} from 'rxjs';

import {defaultTripResolver} from './default-trip.resolver';
import {TripsDetails} from '../../../generated/public-transport-api';

describe('defaultTripResolver', () => {
    const executeResolver: ResolveFn<Observable<TripsDetails>> = (...resolverParameters) =>
        TestBed.runInInjectionContext(() => defaultTripResolver(...resolverParameters));

    beforeEach(() => {
        TestBed.configureTestingModule({});
    });

    it('should be created', () => {
        expect(executeResolver).toBeTruthy();
    });
});
