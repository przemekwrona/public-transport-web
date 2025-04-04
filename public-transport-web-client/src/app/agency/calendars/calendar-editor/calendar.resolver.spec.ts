import { TestBed } from '@angular/core/testing';
import { ResolveFn } from '@angular/router';

import { calendarResolver } from './calendar.resolver';

describe('calendarResolver', () => {
  const executeResolver: ResolveFn<boolean> = (...resolverParameters) => 
      TestBed.runInInjectionContext(() => calendarResolver(...resolverParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeResolver).toBeTruthy();
  });
});
