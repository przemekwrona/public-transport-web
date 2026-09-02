import {ComponentFixture, TestBed} from '@angular/core/testing';
import {of} from 'rxjs';
import {provideNoopAnimations} from '@angular/platform-browser/animations';
import {RouteService} from '../../../generated/public-transport-api';
import {AgencyStorageService} from '../../../auth/agency-storage.service';
import {RouteSelectComponent} from './route-select.component';

describe('RouteSelectComponent', () => {
    let component: RouteSelectComponent;
    let fixture: ComponentFixture<RouteSelectComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [RouteSelectComponent],
            providers: [
                provideNoopAnimations(),
                {provide: AgencyStorageService, useValue: {getInstance: () => 'test'}},
                {
                    provide: RouteService, useValue: {
                        getRoutes: () => of({
                            items: [
                                {routeId: {line: '1', name: 'Kielce_Warszawa', version: 1}},
                                {routeId: {line: 'L2', name: 'Krakow_Poznan', version: 1}}
                            ]
                        })
                    }
                }
            ]
        })
            .compileComponents();

        fixture = TestBed.createComponent(RouteSelectComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
