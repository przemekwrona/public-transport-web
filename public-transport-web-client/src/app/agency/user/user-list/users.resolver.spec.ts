import { TestBed } from '@angular/core/testing';
import { ResolveFn } from '@angular/router';

import { usersResolver } from './users.resolver';
import {AppUsersResponse} from "../../../generated/public-transport";

describe('usersResolver', () => {
  const executeResolver: ResolveFn<AppUsersResponse> = (...resolverParameters) =>
      TestBed.runInInjectionContext(() => usersResolver(...resolverParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeResolver).toBeTruthy();
  });
});
