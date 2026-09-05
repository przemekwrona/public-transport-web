import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormArray, FormGroup } from '@angular/forms';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';
import { of } from 'rxjs';

import { TimetableWizardComponent } from './timetable-wizard.component';
import {
  BrigadeService,
  GetAllTripsResponse,
  GetBrigadeDetailsResponse,
  ResourceService,
  TrafficMode,
  TripMode
} from '../../../generated/public-transport-api';
import { AgencyStorageService } from '../../../auth/agency-storage.service';

describe('TimetableWizardComponent', () => {
  let component: TimetableWizardComponent;
  let fixture: ComponentFixture<TimetableWizardComponent>;
  let deleteResource: jasmine.Spy;
  let getCalendarSymbolBrigadeResources: jasmine.Spy;
  let getNextBrigadeEventSequence: jasmine.Spy;
  let putBrigadeEvent: jasmine.Spy;
  let router: Router;

  const brigadeDetails: GetBrigadeDetailsResponse = {
    brigade: {
      brigades: [{
        calendarSymbolId: {
          symbol: 'C',
          calendarItemId: {code: 'CAL-1'}
        }
      }]
    }
  };

  const defaultRoute: GetAllTripsResponse = {
    lines: [{
      route: {routeCode: 'R1'},
      trips: [
        {
          tripId: {tripCode: 'T-FRONT', variantMode: TripMode.Front, routeId: {routeCode: 'R1', line: '1', name: 'Centrum'}},
          line: '1',
          name: 'Centrum',
          mode: TripMode.Front,
          isMainVariant: true,
          travelTimeInSeconds: 600,
          profile: [
            {trafficMode: TrafficMode.Normal, travelTime: 600},
            {trafficMode: TrafficMode.Traffic, travelTime: 900}
          ]
        },
        {
          tripId: {tripCode: 'T-BACK', variantMode: TripMode.Back, routeId: {routeCode: 'R1', line: '1', name: 'Dworzec'}},
          line: '1',
          name: 'Dworzec',
          mode: TripMode.Back,
          isMainVariant: false,
          travelTimeInSeconds: 540,
          profile: [{trafficMode: TrafficMode.Normal, travelTime: 540}]
        }
      ]
    }]
  };

  beforeEach(async () => {
    deleteResource = jasmine.createSpy('deleteResource').and.returnValue(of({}));
    getCalendarSymbolBrigadeResources = jasmine.createSpy('getCalendarSymbolBrigadeResources')
      .and.returnValue(of({brigadeResources: [{sequenceHex: 'RES-1', sequence: 1}]}));
    getNextBrigadeEventSequence = jasmine.createSpy('getNextBrigadeEventSequence')
      .and.returnValues(of({sequence: 1, sequenceHex: 'EVT-1'}), of({sequence: 2, sequenceHex: 'EVT-2'}));
    putBrigadeEvent = jasmine.createSpy('putBrigadeEvent').and.returnValue(of({}));

    await TestBed.configureTestingModule({
      imports: [TimetableWizardComponent],
      providers: [
        provideRouter([]),
        {provide: AgencyStorageService, useValue: {getInstance: () => 'test-agency'}},
        {provide: ResourceService, useValue: {deleteResource}},
        {
          provide: BrigadeService,
          useValue: {getCalendarSymbolBrigadeResources, getNextBrigadeEventSequence, putBrigadeEvent}
        },
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of(convertToParamMap({
              brigadeCode: 'B1',
              calendarSymbol: 'C'
            })),
            data: of({defaultRoute, brigade: brigadeDetails})
          }
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TimetableWizardComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));
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

  it('should collect filled departures from both timetable boards', () => {
    setDepartureMinutes(component.getFrontTimetable(), 6, 15);
    setDepartureMinutes(component.getBackTimetable(), 7, 30);

    const button: HTMLButtonElement | null = fixture.nativeElement.querySelector('.card.min-w-full button.btn-success');
    expect(button?.textContent?.trim()).toBe('Generuj');

    button?.click();

    expect(component.frontDepartures).toEqual([
      {time: '06:15', designation: undefined, routeCode: 'R1', tripCode: 'T-FRONT', trafficMode: TrafficMode.Normal}
    ]);
    expect(component.backDepartures).toEqual([
      {time: '07:30', designation: undefined, routeCode: 'R1', tripCode: 'T-BACK', trafficMode: TrafficMode.Normal}
    ]);
    expect(deleteResource).toHaveBeenCalledWith('test-agency', 'B1', 'CAL-1', 'C');
    expect(getCalendarSymbolBrigadeResources).toHaveBeenCalledWith('test-agency', 'B1', 'CAL-1', 'C');
    expect(getNextBrigadeEventSequence).toHaveBeenCalledTimes(2);
    expect(putBrigadeEvent).toHaveBeenCalledTimes(2);
    expect(putBrigadeEvent).toHaveBeenCalledWith('test-agency', 'B1', 'CAL-1', 'C', 'RES-1', {
      startSecond: 6 * 3600 + 15 * 60,
      endSecond: 6 * 3600 + 15 * 60 + 600,
      line: '1',
      name: 'Centrum',
      sequence: 1,
      sequenceHex: 'EVT-1',
      tripId: {
        routeId: {routeCode: 'R1', line: '1', name: 'Centrum', version: undefined},
        variantName: undefined,
        variantMode: TripMode.Front,
        trafficMode: TrafficMode.Normal,
        tripCode: 'T-FRONT'
      }
    });
    expect(putBrigadeEvent).toHaveBeenCalledWith('test-agency', 'B1', 'CAL-1', 'C', 'RES-1', {
      startSecond: 7 * 3600 + 30 * 60,
      endSecond: 7 * 3600 + 30 * 60 + 540,
      line: '1',
      name: 'Dworzec',
      sequence: 2,
      sequenceHex: 'EVT-2',
      tripId: {
        routeId: {routeCode: 'R1', line: '1', name: 'Dworzec', version: undefined},
        variantName: undefined,
        variantMode: TripMode.Back,
        trafficMode: TrafficMode.Normal,
        tripCode: 'T-BACK'
      }
    });
    expect(router.navigate).toHaveBeenCalledWith(['/agency/brigades', 'B1', 'edit'], {
      queryParams: {symbol: 'C'}
    });
  });
});

function setDepartureMinutes(directionGroup: FormGroup, hour: number, minutes: number): void {
  const departures = directionGroup.get('departures') as FormArray<FormGroup>;
  const departure = departures.controls.find(group => group.get('hour')?.value === hour);
  departure?.get('minutes')?.setValue(minutes);
}
