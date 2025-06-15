import { TestBed } from '@angular/core/testing';
import { ResolveFn } from '@angular/router';

import { googleAgreementsResolver } from './google-agreements.resolver';
import {GoogleAgreementsResponse} from "../../generated/public-transport";

describe('googleAgreementsResolver', () => {
  const executeResolver: ResolveFn<GoogleAgreementsResponse> = (...resolverParameters) =>
      TestBed.runInInjectionContext(() => googleAgreementsResolver(...resolverParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeResolver).toBeTruthy();
  });
});
