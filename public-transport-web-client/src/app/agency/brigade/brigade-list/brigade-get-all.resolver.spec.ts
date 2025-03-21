import { TestBed } from '@angular/core/testing';
import { ResolveFn } from '@angular/router';

import { brigadeGetAllResolver } from './brigade-get-all.resolver';

describe('brigadeGetAllResolver', () => {
  const executeResolver: ResolveFn<boolean> = (...resolverParameters) => 
      TestBed.runInInjectionContext(() => brigadeGetAllResolver(...resolverParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeResolver).toBeTruthy();
  });
});
