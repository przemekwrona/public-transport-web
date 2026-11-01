import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SimpleChange } from '@angular/core';

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

  it('does not throw when include or exclude days are missing', () => {
    component.year = 2026;
    component.month = 3;
    component.ngOnInit();
    component.includeDays = undefined;
    component.excludeDays = undefined;
    component.weekdays = [1];

    expect(() => component.ngOnChanges({
      weekdays: new SimpleChange([], [1], false)
    })).not.toThrow();
  });
});
