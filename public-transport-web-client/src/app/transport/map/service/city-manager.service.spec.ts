import { TestBed } from '@angular/core/testing';

import { CityManagerService } from './city-manager.service';

describe('CityManagerService', () => {
  let service: CityManagerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CityManagerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
