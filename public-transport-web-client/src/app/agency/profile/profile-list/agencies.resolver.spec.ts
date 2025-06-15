import { TestBed } from '@angular/core/testing';
import { ResolveFn } from '@angular/router';

import { agenciesResolver } from './agencies.resolver';
import {AgenciesAdminResponse} from "../../../generated/public-transport";

describe('profilesResolver', () => {
  const executeResolver: ResolveFn<AgenciesAdminResponse> = (...resolverParameters) =>
      TestBed.runInInjectionContext(() => agenciesResolver(...resolverParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeResolver).toBeTruthy();
  });
});
