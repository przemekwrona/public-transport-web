import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { TimetableWizardComponent } from './timetable-wizard.component';
import { GetAllTripsResponse, TrafficMode, TripMode } from '../../../generated/public-transport-api';

describe('TimetableWizardComponent', () => {
  let component: TimetableWizardComponent;
  let fixture: ComponentFixture<TimetableWizardComponent>;

  const defaultRoute: GetAllTripsResponse = {
    lines: [{
      route: {routeCode: 'R1'},
      trips: [
        {
          tripId: {tripCode: 'T-FRONT', variantMode: TripMode.Front, routeId: {routeCode: 'R1'}},
          mode: TripMode.Front,
          isMainVariant: true,
          travelTimeInSeconds: 600,
          profile: [
            {trafficMode: TrafficMode.Normal, travelTime: 600},
            {trafficMode: TrafficMode.Traffic, travelTime: 900}
          ]
        },
        {
          tripId: {tripCode: 'T-BACK', variantMode: TripMode.Back, routeId: {routeCode: 'R1'}},
          mode: TripMode.Back,
          isMainVariant: false,
          travelTimeInSeconds: 540,
          profile: [{trafficMode: TrafficMode.Normal, travelTime: 540}]
        }
      ]
    }]
  };

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
            data: of({defaultRoute})
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

  it('should map defaultRoute profiles into front and back tripProfiles', () => {
    expect(component.tripProfiles.front).toEqual([
      {routeCode: 'R1', tripCode: 'T-FRONT', trafficMode: TrafficMode.Normal, isMainVariant: true, variantDesignation: undefined, variantDescription: undefined, travelTimeInSeconds: 600},
      {routeCode: 'R1', tripCode: 'T-FRONT', trafficMode: TrafficMode.Traffic, isMainVariant: true, variantDesignation: undefined, variantDescription: undefined, travelTimeInSeconds: 900}
    ]);
    expect(component.tripProfiles.back).toEqual([
      {routeCode: 'R1', tripCode: 'T-BACK', trafficMode: TrafficMode.Normal, isMainVariant: false, variantDesignation: undefined, variantDescription: undefined, travelTimeInSeconds: 540}
    ]);
  });

  it('should render a generate button that calls getFirstProfile', () => {
    const getFirstProfile = spyOn(component, 'getFirstProfile').and.callThrough();

    const button: HTMLButtonElement | null = fixture.nativeElement.querySelector('.card.min-w-full button.btn-success');
    expect(button?.textContent?.trim()).toBe('Generuj');

    button?.click();

    expect(getFirstProfile).toHaveBeenCalledWith(component.tripProfiles.front);
    expect(getFirstProfile).toHaveBeenCalledWith(component.tripProfiles.back);
  });
});
