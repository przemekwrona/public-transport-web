import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TimetableWizardComponent } from './timetable-wizard.component';

describe('TimetableWizardComponent', () => {
  let component: TimetableWizardComponent;
  let fixture: ComponentFixture<TimetableWizardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TimetableWizardComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TimetableWizardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
