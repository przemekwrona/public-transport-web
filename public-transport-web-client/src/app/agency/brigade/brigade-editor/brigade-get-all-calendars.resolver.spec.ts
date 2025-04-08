import { TestBed } from '@angular/core/testing';
import { ResolveFn } from '@angular/router';

import { brigadeGetAllCalendarsResolver } from './brigade-get-all-calendars.resolver';
import {GetCalendarsResponse} from "../../../generated/public-transport";

describe('brigadeGetAllCalendarsResolver', () => {
  const executeResolver: ResolveFn<GetCalendarsResponse> = (...resolverParameters) =>
      TestBed.runInInjectionContext(() => brigadeGetAllCalendarsResolver(...resolverParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeResolver).toBeTruthy();
  });
});
