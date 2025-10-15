import { TestBed } from '@angular/core/testing';
import { ResolveFn } from '@angular/router';

import { profileDetailsResolver } from './profile-details.resolver';
import {AgencyDetails} from "../../../generated/public-transport-api";

describe('profileDetailsResolver', () => {
  const executeResolver: ResolveFn<AgencyDetails> = (...resolverParameters) =>
      TestBed.runInInjectionContext(() => profileDetailsResolver(...resolverParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeResolver).toBeTruthy();
  });
});
