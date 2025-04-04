import { TestBed } from '@angular/core/testing';
import { ResolveFn } from '@angular/router';

import { getAllCalendarsResolver } from './get-all-calendars.resolver';

describe('getAllCalendarsResolver', () => {
  const executeResolver: ResolveFn<boolean> = (...resolverParameters) => 
      TestBed.runInInjectionContext(() => getAllCalendarsResolver(...resolverParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeResolver).toBeTruthy();
  });
});
