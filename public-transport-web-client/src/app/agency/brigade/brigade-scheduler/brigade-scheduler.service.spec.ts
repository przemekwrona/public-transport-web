import { TestBed } from '@angular/core/testing';

import { BrigadeSchedulerService } from './brigade-scheduler.service';

describe('BrigadeSchedulerService', () => {
  let service: BrigadeSchedulerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BrigadeSchedulerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
