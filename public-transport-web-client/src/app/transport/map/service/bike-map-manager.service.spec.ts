import { TestBed } from '@angular/core/testing';

import { BikeMapManagerService } from './bike-map-manager.service';

describe('BikeMapManagerService', () => {
  let service: BikeMapManagerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BikeMapManagerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
