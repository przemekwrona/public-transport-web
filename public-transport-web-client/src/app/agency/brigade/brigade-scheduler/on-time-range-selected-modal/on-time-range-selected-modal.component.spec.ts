import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OnTimeRangeSelectedModalComponent } from './on-time-range-selected-modal.component';

describe('OnTimeRangeSelectedModalComponent', () => {
  let component: OnTimeRangeSelectedModalComponent;
  let fixture: ComponentFixture<OnTimeRangeSelectedModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OnTimeRangeSelectedModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OnTimeRangeSelectedModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
