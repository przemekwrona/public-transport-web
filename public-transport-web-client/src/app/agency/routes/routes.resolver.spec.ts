import { TestBed } from '@angular/core/testing';
import { ResolveFn } from '@angular/router';

import { routesResolver } from './routes.resolver';

describe('routesResolver', () => {
  const executeResolver: ResolveFn<boolean> = (...resolverParameters) => 
      TestBed.runInInjectionContext(() => routesResolver(...resolverParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeResolver).toBeTruthy();
  });
});
