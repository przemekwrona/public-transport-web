import { TestBed } from '@angular/core/testing';

import { ItineraryManagerService } from './itinerary-manager.service';

describe('ItineraryManagerService', () => {
  let service: ItineraryManagerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ItineraryManagerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
