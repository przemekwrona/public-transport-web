import { TestBed } from '@angular/core/testing';

import { BrigadeService } from './brigade.service';

describe('BrigadeService', () => {
  let service: BrigadeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BrigadeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
