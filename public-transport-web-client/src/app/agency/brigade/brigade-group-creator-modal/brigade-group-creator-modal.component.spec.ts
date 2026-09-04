import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';
import { BrigadeService, CalendarSymbolService } from '../../../generated/public-transport-api';
import { AgencyStorageService } from '../../../auth/agency-storage.service';

import { BrigadeGroupCreatorModalComponent } from './brigade-group-creator-modal.component';

describe('BrigadeGroupCreatorModalComponent', () => {
  let component: BrigadeGroupCreatorModalComponent;
  let fixture: ComponentFixture<BrigadeGroupCreatorModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BrigadeGroupCreatorModalComponent],
      providers: [
        { provide: MatDialogRef, useValue: { close: jasmine.createSpy('close') } },
        { provide: MAT_DIALOG_DATA, useValue: { calendarCode: 'cal-1', brigadeCode: '01', brigadeName: 'Test' } },
        { provide: AgencyStorageService, useValue: { getInstance: () => 'test' } },
        { provide: CalendarSymbolService, useValue: { getCalendarSymbolsByCalendarCode: () => of({ calendars: [] }) } },
        { provide: BrigadeService, useValue: { createCalendarSymbolBrigade: () => of({}) } }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BrigadeGroupCreatorModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
