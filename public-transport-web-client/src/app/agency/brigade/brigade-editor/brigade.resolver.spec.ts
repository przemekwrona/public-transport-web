import { TestBed } from '@angular/core/testing';
import { ResolveFn } from '@angular/router';

import { brigadeResolver } from './brigade.resolver';
import {Observable} from "rxjs";
import {BrigadeBody} from "../../../generated/public-transport-api";

describe('brigadeResolver', () => {
  const executeResolver: ResolveFn<Observable<BrigadeBody>> = (...resolverParameters) =>
      TestBed.runInInjectionContext(() => brigadeResolver(...resolverParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeResolver).toBeTruthy();
  });
});
