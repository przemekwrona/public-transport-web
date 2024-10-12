import { TestBed } from '@angular/core/testing';

import { JourneySummaryService } from './journey-summary.service';

describe('JourneySummaryService', () => {
  let service: JourneySummaryService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(JourneySummaryService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
