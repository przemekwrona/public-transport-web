import { TestBed } from '@angular/core/testing';
import { ResolveFn } from '@angular/router';

import { brigadeGetAllResolver } from './brigade-get-all.resolver';
import {Observable} from "rxjs";
import {GetBrigadeResponse} from "../../../generated/public-transport";

describe('brigadeGetAllResolver', () => {
  const executeResolver: ResolveFn<Observable<GetBrigadeResponse>> = (...resolverParameters) =>
      TestBed.runInInjectionContext(() => brigadeGetAllResolver(...resolverParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeResolver).toBeTruthy();
  });
});
