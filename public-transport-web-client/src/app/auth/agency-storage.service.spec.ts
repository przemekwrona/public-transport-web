import { TestBed } from '@angular/core/testing';

import { AgencyStorageService } from './agency-storage.service';

describe('AgencyStorageService', () => {
  let service: AgencyStorageService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AgencyStorageService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
