import { TestBed } from '@angular/core/testing';
import { ResolveFn } from '@angular/router';

import { brigadeGetAllTripsResolver } from './brigade-get-all-trips.resolver';

describe('brigadeGetAllTripsResolver', () => {
  const executeResolver: ResolveFn<boolean> = (...resolverParameters) => 
      TestBed.runInInjectionContext(() => brigadeGetAllTripsResolver(...resolverParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeResolver).toBeTruthy();
  });
});
