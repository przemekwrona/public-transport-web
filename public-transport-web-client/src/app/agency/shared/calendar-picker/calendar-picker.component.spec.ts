import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CalendarPickerComponent } from './calendar-picker.component';

describe('CalendarPickerComponent', () => {
  let component: CalendarPickerComponent;
  let fixture: ComponentFixture<CalendarPickerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CalendarPickerComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CalendarPickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
