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

  it('marks a clicked day before the parent list is written back', () => {
    const clicked = new Date(2026, 2, 16);
    preparePicker(clicked);
    const persisted: Date[] = [];
    component.includeDaysChange.subscribe((date: Date) => toggle(persisted, date));

    component.selectedChange(clicked);

    expect(persisted.length).toBe(1);
    expect(component.dateClass(clicked)).toEqual(['has-no-week-day', 'has-included-day']);

    component.selectedChange(clicked);

    expect(persisted.length).toBe(0);
    expect(component.dateClass(clicked)).toBe('has-no-week-day');
  });

  it('marks a weekday exception before the parent list is written back', () => {
    const clicked = new Date(2026, 2, 16);
    preparePicker(clicked);
    component.weekdays = [clicked.getDay()];
    const persisted: Date[] = [];
    component.excludeDaysChange.subscribe((date: Date) => toggle(persisted, date));

    component.selectedChange(clicked);

    expect(persisted.length).toBe(1);
    expect(component.dateClass(clicked)).toEqual(['has-week-day', 'has-excluded-day']);
  });

  it('highlights Sundays when the Sunday checkbox stores day 7', () => {
    const sunday = new Date(2026, 2, 1);
    while (sunday.getDay() !== 0) {
      sunday.setDate(sunday.getDate() + 1);
    }
    preparePicker(sunday);
    component.weekdays = [7];

    expect(component.dateClass(sunday)).toBe('has-week-day');
  });

  it('does not toggle a shared day list twice', () => {
    const clicked = new Date(2026, 2, 16);
    const shared: Date[] = [];
    preparePicker(clicked);
    component.includeDays = shared;
    component.includeDaysChange.subscribe((date: Date) => toggle(shared, date));

    component.selectedChange(clicked);

    expect(shared.length).toBe(1);
    expect(component.dateClass(clicked)).toEqual(['has-no-week-day', 'has-included-day']);
  });

  function preparePicker(clicked: Date): void {
    component.year = clicked.getFullYear();
    component.month = clicked.getMonth() + 1;
    component.ngOnInit();
    component.weekdays = [];
    component.includeDays = undefined;
    component.excludeDays = undefined;
    component.startDate = new Date(clicked.getFullYear(), clicked.getMonth(), 1);
    component.endDate = new Date(clicked.getFullYear(), clicked.getMonth() + 1, 0);
    component.calendar = {updateTodaysDate: jasmine.createSpy('updateTodaysDate')} as never;
  }

  function toggle(dates: Date[], clicked: Date): void {
    const index = dates.findIndex((date: Date) =>
      date.getFullYear() === clicked.getFullYear()
      && date.getMonth() === clicked.getMonth()
      && date.getDate() === clicked.getDate());
    if (index > -1) {
      dates.splice(index, 1);
    } else {
      dates.push(clicked);
    }
  }
});
