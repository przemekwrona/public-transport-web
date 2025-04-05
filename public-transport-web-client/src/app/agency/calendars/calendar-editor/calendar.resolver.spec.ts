import { TestBed } from '@angular/core/testing';
import { ResolveFn } from '@angular/router';

import { calendarResolver } from './calendar.resolver';
import {Observable} from "rxjs";
import {CalendarBody} from "../../../generated/public-transport";

describe('calendarResolver', () => {
  const executeResolver: ResolveFn<Observable<CalendarBody>> = (...resolverParameters) =>
      TestBed.runInInjectionContext(() => calendarResolver(...resolverParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeResolver).toBeTruthy();
  });
});
