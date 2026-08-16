import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TimetableBoardComponent } from './timetable-board.component';

describe('TimetableBoardComponent', () => {
  let component: TimetableBoardComponent;
  let fixture: ComponentFixture<TimetableBoardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TimetableBoardComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TimetableBoardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
