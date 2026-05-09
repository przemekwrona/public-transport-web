import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CalendarItemModalComponent } from './calendar-item-modal.component';

describe('CalendarItemModalComponent', () => {
  let component: CalendarItemModalComponent;
  let fixture: ComponentFixture<CalendarItemModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CalendarItemModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CalendarItemModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
