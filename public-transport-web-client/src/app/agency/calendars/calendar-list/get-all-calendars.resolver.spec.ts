import { TestBed } from '@angular/core/testing';
import { ResolveFn } from '@angular/router';

import { getAllCalendarsResolver } from './get-all-calendars.resolver';
import {GetCalendarsResponse} from "../../../generated/public-transport-api";

describe('getAllCalendarsResolver', () => {
  const executeResolver: ResolveFn<GetCalendarsResponse> = (...resolverParameters) =>
      TestBed.runInInjectionContext(() => getAllCalendarsResolver(...resolverParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeResolver).toBeTruthy();
  });
});
