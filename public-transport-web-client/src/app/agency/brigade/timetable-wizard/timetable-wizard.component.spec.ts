import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { TimetableWizardComponent } from './timetable-wizard.component';

describe('TimetableWizardComponent', () => {
  let component: TimetableWizardComponent;
  let fixture: ComponentFixture<TimetableWizardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TimetableWizardComponent],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of(convertToParamMap({
              brigadeCode: 'B1',
              calendarSymbol: 'C'
            }))
          }
        }
      ]
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
