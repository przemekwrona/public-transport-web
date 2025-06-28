import { TestBed } from '@angular/core/testing';
import { ResolveFn } from '@angular/router';

import { centerPointResolver } from './center-point.resolver';
import {CenterPoint} from "../../generated/public-transport";

describe('centerPointResolver', () => {
  const executeResolver: ResolveFn<CenterPoint> = (...resolverParameters) =>
      TestBed.runInInjectionContext(() => centerPointResolver(...resolverParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeResolver).toBeTruthy();
  });
});
