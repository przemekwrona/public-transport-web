import { TestBed } from '@angular/core/testing';

import { GbfsService } from './gbfs.service';

describe('GbfsService', () => {
  let service: GbfsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(GbfsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
