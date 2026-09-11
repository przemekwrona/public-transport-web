import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ActivatedRoute} from '@angular/router';
import {of} from 'rxjs';
import {provideNoopAnimations} from '@angular/platform-browser/animations';

import {TripListTimetableComponent} from './trip-list-timetable.component';
import {LoginService} from '../../../../auth/login.service';
import {RouteStopTimetableService, TripMode} from '../../../../generated/public-transport-api';

describe('TripListTimetableComponent', () => {
    let component: TripListTimetableComponent;
    let fixture: ComponentFixture<TripListTimetableComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [TripListTimetableComponent],
            providers: [
                provideNoopAnimations(),
                {
                    provide: ActivatedRoute,
                    useValue: {
                        snapshot: {paramMap: {get: () => 'R1'}, parent: null},
                        data: of({
                            response: {
                                front: {
                                    spineStopIds: [1],
                                    nodes: [{stopId: 1, name: 'Front Stop'}],
                                    branches: []
                                },
                                back: {
                                    spineStopIds: [2],
                                    nodes: [{stopId: 2, name: 'Back Stop'}],
                                    branches: []
                                }
                            }
                        })
                    }
                },
                {provide: LoginService, useValue: {getInstance: () => 'agency'}},
                {provide: RouteStopTimetableService, useValue: {getRouteStopTimetable: () => of({departures: []})}}
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(TripListTimetableComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('renders FRONT and BACK tabs and selects FRONT by default', () => {
        const labels = Array.from(fixture.nativeElement.querySelectorAll('a[mat-tab-link]'))
            .map((link: HTMLElement) => link.textContent?.trim());

        expect(labels).toEqual(['FRONT', 'BACK']);
        expect(component.activeTab.tripMode).toBe(TripMode.Front);
        expect(fixture.nativeElement.textContent).toContain('Front Stop');
        expect(fixture.nativeElement.textContent).not.toContain('Back Stop');
    });

    it('switches to BACK stops when the BACK tab is selected', () => {
        component.selectTab(component.tabs[1]);
        fixture.detectChanges();

        expect(component.activeTab.tripMode).toBe(TripMode.Back);
        expect(fixture.nativeElement.textContent).toContain('Back Stop');
        expect(fixture.nativeElement.textContent).not.toContain('Front Stop');
    });
});
