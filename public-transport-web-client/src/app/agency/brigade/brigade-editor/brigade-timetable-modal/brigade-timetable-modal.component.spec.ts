import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BrigadeTimetableModalComponent } from './brigade-timetable-modal.component';

describe('BrigadeTimetableModalComponent', () => {
  let component: BrigadeTimetableModalComponent;
  let fixture: ComponentFixture<BrigadeTimetableModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BrigadeTimetableModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BrigadeTimetableModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
