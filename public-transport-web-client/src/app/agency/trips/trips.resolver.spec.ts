import { TestBed } from '@angular/core/testing';
import { ResolveFn } from '@angular/router';

import { tripsResolver } from './trips.resolver';

describe('tripsResolver', () => {
  const executeResolver: ResolveFn<boolean> = (...resolverParameters) => 
      TestBed.runInInjectionContext(() => tripsResolver(...resolverParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeResolver).toBeTruthy();
  });
});
