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
            })),
            data: of({defaultTrip: {}})
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

  it('should render two timetable boards', () => {
    const boards = fixture.nativeElement.querySelectorAll('app-timetable-board');
    expect(boards.length).toBe(2);
  });
});
